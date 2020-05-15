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
import com.wujiuye.miniexcel.excel.annotation.ExcelMetaData;
import com.wujiuye.miniexcel.excel.util.DateUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/30 {描述：}
 */
class SXSSFWriter extends AbstractExcelWriter {

    SXSSFWriter(String filePath, ExcelFileType format) {
        super(filePath, format);
    }

    SXSSFWriter(OutputStream ot, ExcelFileType format) {
        super(ot, format);
    }

    @Override
    protected void doWrite(OutputStream ot, ExcelWriterListener<?> writerListener, List<ExcelMetaData> metaDatas) {
        boolean needWriter = writerListener.autoGenerateTitle();
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
        while ((currentPageSize = writerListener.getNetOutputDataRealSize(sheetNumber)) != 0) {
            List<?> data = writerListener.getOutputDataWithSheetNumber(sheetNumber, nextLimit, nextLimit + currentPageSize);
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
        try {
            wb.write(outputStream);
        } catch (Exception e) {
            writerListener.onError(e);
        } finally {
            // 释放磁盘上备份此工作簿的临时文件
            wb.dispose();
        }
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
                    ExcelMetaData metaData = titles.get(cellNumber);
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
                        cell.setCellValue(DateUtils.fromDate((Date) cellDateVlaue, metaData.getDatePattern(), metaData.getTimezone()));
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
