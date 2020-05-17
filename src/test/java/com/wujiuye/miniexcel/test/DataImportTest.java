package com.wujiuye.miniexcel.test;

import com.wujiuye.miniexcel.excel.ExcelFileType;
import com.wujiuye.miniexcel.excel.reader.*;
import com.wujiuye.miniexcel.test.model.DateModel;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * 导入测试
 *
 * @author wujiuye 2020/05/12
 */
public class DataImportTest {

    @Test
    public void testImport() {
        AbstractExcelReader excelReader = AbstractExcelReader.getReader("/tmp/miniexcel-test.xlsx");
        AnnotationExcelReaderListener<DateModel> readerListener = new AnnotationExcelReaderListener<>(DateModel.class);
        excelReader.read(readerListener);
        List<DateModel> dateModelList = readerListener.getRecords();
        for (DateModel model : dateModelList) {
            System.out.println(model);
        }
    }

    @Test
    public void testDefault() {
        AbstractExcelReader excelReader = AbstractExcelReader.getReader("/tmp/miniexcel-test.xlsx");
        DefaultExcelReaderListener readerListener = new DefaultExcelReaderListener("订单ID", "会员ID");
        excelReader.read(readerListener);
        List<Map<String, Object>> data = readerListener.getData();
    }

    @Test
    public void testDayRead() {
        AbstractExcelReader excelReader = AbstractExcelReader.getReader("/tmp/miniexcel-test.xlsx");
        ExcelReaderListener readerListener = new ExcelReaderListenerAdapter() {
            @Override
            public void onReadRow(Object data, int rowNumber, int cellNumber) {
                System.out.println("行号：" + rowNumber + ",列号：" + cellNumber + ",单元格内容：" + data);
                // 自己存储读取到的数据数据
            }
        };
        excelReader.read(readerListener);
    }

    @Test
    public void testImportCsv() {
        AbstractExcelReader excelReader = AbstractExcelReader.getReader("/tmp/miniexcel-test.csv");
        AnnotationExcelReaderListener<DateModel> readerListener = new AnnotationExcelReaderListener<>(DateModel.class);
        excelReader.read(readerListener);
        List<DateModel> dateModelList = readerListener.getRecords();
        for (DateModel model : dateModelList) {
            System.out.println(model);
        }
    }

    @Test
    public void testImportByIn() {
        AbstractExcelReader excelReader = AbstractExcelReader.getReader(System.in, ExcelFileType.XLS);
        AnnotationExcelReaderListener<DateModel> readerListener = new AnnotationExcelReaderListener<>(DateModel.class);
        excelReader.read(readerListener);
        List<DateModel> dateModelList = readerListener.getRecords();
        for (DateModel model : dateModelList) {
            System.out.println(model);
        }
    }

}
