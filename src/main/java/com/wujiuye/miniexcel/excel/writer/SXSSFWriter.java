package com.wujiuye.miniexcel.excel.writer;

import com.wujiuye.miniexcel.excel.base.ExcelMetaData;
import com.wujiuye.miniexcel.excel.base.ReflectionUtils;
import com.wujiuye.miniexcel.excel.utils.DateUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/30 {描述：}
 */
public class SXSSFWriter extends AbstractExcelWriter {

    private ExcelWriterListener writerListener;

    SXSSFWriter(String filePath, ExportFormatType format) {
        super(filePath, format);
    }

    @Override
    protected File doWrite(ExcelWriterListener writerListener) {
        this.writerListener = writerListener;
        if (writerListener == null) {
            return null;
        }
        try {
            this.realDoWrite();
            File file = new File(this.filePath + this.format.getFromat());
            return file;
        } catch (Exception e) {
            this.writerListener.onError(e);
            return null;
        }
    }

    private void realDoWrite() throws Exception {
        boolean needWriter = this.writerListener.autoGenerateTitle();
        Class targetClass = this.writerListener.getDataObjectClass();
        if (targetClass == null) {
            throw new RuntimeException("target class not null!!!");
        }
        List<ExcelMetaData> metaDatas = ReflectionUtils.getFieldWithTargetClass(targetClass);
        //排序
        metaDatas = ReflectionUtils.sortField(metaDatas);

        SXSSFWorkbook wb = new SXSSFWorkbook(this.brushPageSize);//SXSSFWorkbook会先在磁盘创建空间来写，此时并未输出到目标文件
        int sheetNumber = 1;
        int currentPageSize;//当前页输出的大小
        int nextLimit = 0;
        Sheet sh = wb.createSheet(this.sheetNameFromat.replace("{sn}", String.valueOf(sheetNumber)));
        int rowIndex = 0;
        //输出标题
        if (needWriter) {
            rowIndex += writeTitle(sh, metaDatas);
        }
        while ((currentPageSize = this.writerListener.getNetOutputDataRealSize(sheetNumber)) != 0) {
            List data = this.writerListener.getOutputDataWithSheetNumber(sheetNumber, nextLimit, nextLimit + currentPageSize);
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

        //输出到目标文件
        try (FileOutputStream fileOut = new FileOutputStream(this.filePath + this.format.fromat)) {
            wb.write(fileOut);
        } catch (Exception e) {
            this.writerListener.onError(e);
        } finally {
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
    private int writeData(Sheet sheet, List<ExcelMetaData> titles, List data, int rowIndex) {
        if (data == null || data.size() == 0) {
            throw new RuntimeException("data is null!");
        }
        //System.out.println("rowIndex=" + rowIndex);
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
                    Class cellClass = cellDateVlaue.getClass();
                    if (cellClass == Integer.class || cellClass == int.class) {
                        cell.setCellValue(Integer.valueOf(cellDateVlaue.toString()));
                    } else if (cellClass == Long.class || cellClass == long.class) {
                        cell.setCellValue(Long.valueOf(cellDateVlaue.toString()));
                    } else if (cellClass == Float.class || cellClass == float.class) {
                        cell.setCellValue(Float.valueOf(cellDateVlaue.toString()));
                    } else if (cellClass == Double.class || cellClass == double.class) {
                        cell.setCellValue(Double.valueOf(cellDateVlaue.toString()));
                    } else if (cellClass == Date.class) {
                        DateUtils.parsingDatetime((Date) cellDateVlaue);
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
