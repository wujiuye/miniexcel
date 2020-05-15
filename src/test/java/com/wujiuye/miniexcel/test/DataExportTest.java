package com.wujiuye.miniexcel.test;

import com.wujiuye.miniexcel.excel.ExcelFileType;
import com.wujiuye.miniexcel.excel.annotation.ColumnsDesignator;
import com.wujiuye.miniexcel.excel.writer.AbstractExcelWriter;
import com.wujiuye.miniexcel.excel.writer.ExcelWriteListenerAdapter;
import com.wujiuye.miniexcel.test.model.DateModel;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 导出测试
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
        excelWriter.write(new ExcelWriteListenerAdapter<DateModel>(DateModel.getTestData(), 1000) {
        });
    }

    @Test
    public void testExportOt() {
        AbstractExcelWriter excelWriter = AbstractExcelWriter.createExcelWriter(System.out, ExcelFileType.XLSX);
        excelWriter.setSheetSize(1000)
                .setBrushPageSize(1000)
                .setSheetNameFromat("Sheet{sn}");
        excelWriter.write(new ExcelWriteListenerAdapter<DateModel>(DateModel.getTestData(), 1000) {
        });
    }

    @Test
    public void testExportCD() {
        AbstractExcelWriter excelWriter = AbstractExcelWriter.createExcelWriter(System.out, ExcelFileType.XLSX);
        excelWriter.setSheetSize(1000)
                .setBrushPageSize(1000)
                .setSheetNameFromat("Sheet{sn}");
        // 指定忽略哪些列
        Set<String> ignoreColumn = new HashSet<>();
        excelWriter.write(new ExcelWriteListenerAdapter<DateModel>(DateModel.getTestData(), 1000) {
        }, new ColumnsDesignator() {

            @Override
            public boolean isIgnore(String column) {
                if (CollectionUtils.isEmpty(ignoreColumn)) {
                    return false;
                }
                return !ignoreColumn.contains(column);
            }

            @Override
            public String renameColumn(String fieldName, String currentColumnName) {
                if (currentColumnName.equalsIgnoreCase("SPU商品ID")) {
                    return "SKU商品ID";
                }
                return currentColumnName;
            }
        });
    }

    @Test
    public void testExportTemplate() {
        AbstractExcelWriter excelWriter = AbstractExcelWriter.createExcelWriter("/tmp/miniexcel-test", ExcelFileType.CSV);
        excelWriter.write(new ExcelWriteListenerAdapter<DateModel>(null, 1000) {
        });
    }

    @Test
    public void testExportCsvBigData() {
        AbstractExcelWriter excelWriter = AbstractExcelWriter.createExcelWriter("/tmp/miniexcel-test", ExcelFileType.CSV);
        List<DateModel> modelList = new ArrayList<>(1000000);
        for (int i = 0; i < 100000; i++) {
            modelList.addAll(DateModel.getTestData());
        }
        excelWriter.write(new ExcelWriteListenerAdapter<DateModel>(modelList, 1000) {
        });
    }

}
