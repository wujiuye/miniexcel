/**
 * Copyright [2019-2020] [wujiuye]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wujiuye.miniexcel.excel.writer;

import com.wujiuye.miniexcel.excel.ExcelFileType;
import com.wujiuye.miniexcel.excel.annotation.ColumnsDesignator;
import com.wujiuye.miniexcel.excel.annotation.ExcelMetaData;
import com.wujiuye.miniexcel.excel.annotation.CellAnnotationParser;
import com.wujiuye.miniexcel.excel.util.DateUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/30 {描述：}
 */
class SXSSFWriter extends AbstractExcelWriter {

    private ExcelWriterListener<?> writerListener;
    private ColumnsDesignator columnsDesignator;

    SXSSFWriter(String filePath, ExcelFileType format) {
        super(filePath, format);
    }

    SXSSFWriter(OutputStream ot, ExcelFileType format) {
        super(ot, format);
    }

    @Override
    protected File doWrite(ExcelWriterListener<?> writerListener, ColumnsDesignator columnsDesignator) {
        this.writerListener = writerListener;
        this.columnsDesignator = columnsDesignator;
        if (writerListener == null) {
            return null;
        }
        try {
            this.realDoWrite();
            if (outputStream != null) {
                return null;
            }
            return new File(this.filePath + this.format.getFromat());
        } catch (Exception e) {
            this.writerListener.onError(e);
            return null;
        }
    }

    private void realDoWrite() throws Exception {
        boolean needWriter = this.writerListener.autoGenerateTitle();
        Class<?> targetClass = this.writerListener.getDataObjectClass();
        if (targetClass == null) {
            throw new RuntimeException("target class not null!!!");
        }
        List<ExcelMetaData> metaDatas = this.analysisColumnsMetaDateByClass(targetClass);

        // SXSSFWorkbook会先在磁盘创建空间来写，此时并未输出到目标文件
        SXSSFWorkbook wb = new SXSSFWorkbook(this.brushPageSize);
        int sheetNumber = 1;
        // 当前页输出的大小
        int currentPageSize;
        int nextLimit = 0;
        Sheet sh = wb.createSheet(this.sheetNameFromat.replace("{sn}", String.valueOf(sheetNumber)));
        int rowIndex = 0;
        // 输出标题
        if (needWriter) {
            rowIndex += writeTitle(sh, metaDatas);
        }
        while ((currentPageSize = this.writerListener.getNetOutputDataRealSize(sheetNumber)) != 0) {
            List<?> data = this.writerListener.getOutputDataWithSheetNumber(sheetNumber, nextLimit, nextLimit + currentPageSize);
            rowIndex = writeData(sh, metaDatas, data, rowIndex);
            nextLimit += currentPageSize;

            //换sheet
            if (nextLimit >= this.sheetSize * sheetNumber) {
                sheetNumber++;
                sh = wb.createSheet(this.sheetNameFromat.replace("{sn}", String.valueOf(sheetNumber)));
                rowIndex = 0;
                //输出标题
                if (needWriter) {
                    rowIndex += writeTitle(sh, metaDatas);
                }
            }
        }

        // 输出到目标文件
        if (outputStream != null) {
            try {
                // 由外部负责关闭流
                wb.write(outputStream);
            } catch (Exception e) {
                this.writerListener.onError(e);
            } finally {
                // 释放磁盘上备份此工作簿的临时文件
                wb.dispose();
            }
        } else {
            try (FileOutputStream fileOut = new FileOutputStream(this.filePath + this.format.getFromat())) {
                wb.write(fileOut);
            } catch (Exception e) {
                this.writerListener.onError(e);
            } finally {
                // 释放磁盘上备份此工作簿的临时文件
                wb.dispose();
            }
        }
    }

    /**
     * 解析类型，获取字段的元数据，完成排序、过滤、给标题别名等操作
     *
     * @param targetClass
     * @return
     */
    private List<ExcelMetaData> analysisColumnsMetaDateByClass(Class<?> targetClass) {
        List<ExcelMetaData> metaDatas = CellAnnotationParser.getFieldWithTargetClass(targetClass);
        // 对列做排序操作
        metaDatas = CellAnnotationParser.sortField(metaDatas);
        if (columnsDesignator != null) {
            // 过滤不需要的字段
            metaDatas = metaDatas.stream()
                    .filter(metaData -> !columnsDesignator.isIgnore(metaData.getFieldName()))
                    .collect(Collectors.toList());
            // 重命名列标题名称（根据字段名重新赋予导出的文件的列标题名）
            metaDatas.forEach(metaData -> metaData.setCellName(
                    columnsDesignator.renameColumn(metaData.getFieldName(), metaData.getCellName())));
        }
        return metaDatas;
    }

    /**
     * 输出标题
     *
     * @param sheet
     * @param titles
     * @return
     */
    private int writeTitle(Sheet sheet, List<ExcelMetaData> titles) {
        Row row = sheet.createRow(0);
        for (int cellNumber = 0; cellNumber < titles.size(); cellNumber++) {
            Cell cell = row.createCell(cellNumber);
            cell.setCellValue(titles.get(cellNumber).getCellName());
        }
        return 1;
    }

    /**
     * 写数据
     *
     * @param sheet
     * @param titles
     * @param data
     * @param rowIndex
     * @return
     */
    private int writeData(Sheet sheet, List<ExcelMetaData> titles, List<?> data, int rowIndex) {
        if (data == null || data.isEmpty()) {
            throw new RuntimeException("data is null!");
        }
        for (int i = 0; i < data.size(); i++, rowIndex++) {
            Row row = sheet.createRow(rowIndex);
            Object obj = data.get(i);
            for (int cellNumber = 0; cellNumber < titles.size(); cellNumber++) {
                Cell cell = row.createCell(cellNumber);
                try {
                    titles.get(cellNumber).getField().setAccessible(true);
                    Object cellDateVlaue = titles.get(cellNumber).getField().get(obj);
                    if (cellDateVlaue == null) {
                        cell.setCellValue("");
                        continue;
                    }
                    Class<?> cellClass = cellDateVlaue.getClass();
                    if (cellClass == Integer.class) {
                        cell.setCellValue(Integer.parseInt(cellDateVlaue.toString()));
                    } else if (cellClass == Long.class) {
                        cell.setCellValue(Long.parseLong(cellDateVlaue.toString()));
                    } else if (cellClass == Float.class) {
                        cell.setCellValue(Float.parseFloat(cellDateVlaue.toString()));
                    } else if (cellClass == Double.class) {
                        cell.setCellValue(Double.parseDouble(cellDateVlaue.toString()));
                    } else if (cellClass == Date.class) {
                        cell.setCellValue(DateUtils.parsingDatetime((Date) cellDateVlaue));
                    } else {
                        cell.setCellValue(cellDateVlaue.toString());
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    cell.setCellValue(e.getLocalizedMessage());
                }
            }
        }
        return rowIndex;
    }

}
