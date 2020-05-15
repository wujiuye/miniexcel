package com.wujiuye.miniexcel.test;

import com.wujiuye.miniexcel.excel.ExcelFileType;
import com.wujiuye.miniexcel.excel.reader.AbstractExcelReader;
import com.wujiuye.miniexcel.excel.reader.AnnotationExcelReaderListener;
import com.wujiuye.miniexcel.excel.writer.AbstractExcelWriter;
import com.wujiuye.miniexcel.excel.writer.DefaultExcelWriteListenerAdapter;
import com.wujiuye.miniexcel.excel.writer.ExcelWriteListenerAdapter;
import com.wujiuye.miniexcel.test.model.DateModel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入导出测试
 *
 * @author wujiuye 2020/05/12
 */
public class DataExportTest {

    @Test
    public void testExport() {
        AbstractExcelWriter excelWriter = AbstractExcelWriter.createExcelWriter("/tmp/miniexcel-test", ExcelFileType.XLSX);
        excelWriter.setSheetSize(1000)
                .setBrushPageSize(1000)
                .setSheetNameFromat("Sheet{sn}");
        excelWriter.write(new DefaultExcelWriteListenerAdapter(DateModel.getTestData(), 1000));
    }

    @Test
    public void testExportCsv() {
        AbstractExcelWriter excelWriter = AbstractExcelWriter.createExcelWriter("/tmp/miniexcel-test", ExcelFileType.CSV);
//        excelWriter.write(new ExcelWriteListenerAdapter<DateModel>(new ArrayList<>(), 1000) {
//        });
//        AbstractExcelWriter excelWriter = AbstractExcelWriter.createExcelWriter(System.out, ExcelFileType.CSV);
//        excelWriter.write(new ExcelWriteListenerAdapter<DateModel>(null, 1000) {
//        });
//        excelWriter.write(new ExcelWriteListenerAdapter<DateModel>(DateModel.getTestData(), 1000) {
//        });
        List<DateModel> modelList = new ArrayList<>(1000000);
        for (int i = 0; i < 100000; i++) {
            modelList.addAll(DateModel.getTestData());
        }
        excelWriter.write(new ExcelWriteListenerAdapter<DateModel>(modelList, 1000) {
        });
    }

    @Test
    public void testImport() {
        AbstractExcelReader excelReader = AbstractExcelReader.getReader("/tmp/miniexcel-test.xlsx", true);
        AnnotationExcelReaderListener<DateModel> readerListener = new AnnotationExcelReaderListener<>(DateModel.class);
        excelReader.read(readerListener);
        List<DateModel> dateModelList = readerListener.getRecords();
        for (DateModel model : dateModelList) {
            System.out.println(model);
        }
    }

    @Test
    public void testImportCsv() {
        AbstractExcelReader excelReader = AbstractExcelReader.getReader("/tmp/miniexcel-test.csv", true);
        AnnotationExcelReaderListener<DateModel> readerListener = new AnnotationExcelReaderListener<>(DateModel.class);
        excelReader.read(readerListener);
        List<DateModel> dateModelList = readerListener.getRecords();
        for (DateModel model : dateModelList) {
            System.out.println(model);
        }
    }

}
