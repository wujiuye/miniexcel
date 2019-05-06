package com.wujiuye.miniexcel.excel.writer;

import java.io.File;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/30 {描述：}
 */
public abstract class AbstractExcelWriter {

    //导出格式
    public enum ExportFormatType {
        CSV(".csv"),
        XLS(".xls"),
        XLSX(".xlsx");
        String fromat;

        ExportFormatType(String fromat) {
            this.fromat = fromat;
        }

        public String getFromat(){
            return this.fromat;
        }
    }

    protected String filePath;//文件刷出的路径
    protected ExportFormatType format;
    protected int brushPageSize = 1000;//刷盘记录的大小，即一次导出的记录数，应尽量的少，避免占用太多内存
    // 每个sheet最大支持1048576行：超出会抛出IllegalArgumentException: Invalid row number (1048576) outside allowable range (0..1048575)
    protected int sheetSize = 100_000; //每个sheet最多保持多少调记录就自动切换到一个新的sheet
    protected String sheetNameFromat;//格式中必须包含{sn}

    //sm是第几个sheet，从1开始
    private final String DEFAULT_SHEETNAME_FROMAT = "sheet_{sn}";

    public static AbstractExcelWriter createExcelWriter(String filePath, ExportFormatType format) {
        AbstractExcelWriter excelWriter = null;
        switch (format) {
            case CSV:
                break;
            case XLS:
            case XLSX:
                excelWriter = new SXSSFWriter(filePath, format);
                break;
        }
        if (excelWriter == null) {
            throw new FormatException("不支持的格式！！！");
        }
        return excelWriter;
    }

    AbstractExcelWriter(String filePath) {
        this.filePath = filePath;
        this.format = ExportFormatType.XLS;
    }

    AbstractExcelWriter(String filePath, ExportFormatType format) {
        this.filePath = filePath;
        this.format = format;
        this.sheetNameFromat = DEFAULT_SHEETNAME_FROMAT;
    }

    public AbstractExcelWriter setSheetSize(int sheetSize) {
        this.sheetSize = sheetSize;
        return this;
    }

    public AbstractExcelWriter setSheetNameFromat(String sheetNameFromat) {
        this.sheetNameFromat = sheetNameFromat;
        return this;
    }

    public AbstractExcelWriter setBrushPageSize(int brushPageSize) {
        this.brushPageSize = brushPageSize;
        return this;
    }

    public File write(ExcelWriterListener writerListener) {
        return this.doWrite(writerListener);
    }

    protected abstract File doWrite(ExcelWriterListener writerListener);

}
