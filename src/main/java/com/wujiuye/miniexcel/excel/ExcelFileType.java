package com.wujiuye.miniexcel.excel;

/**
 * excel文件类型
 *
 * @author wujiuye 2020/03/31
 */
public enum ExcelFileType {

    /**
     * csv格式
     */
    CSV(".csv"),
    /**
     * xls格式
     */
    XLS(".xls"),
    /**
     * xlsx格式
     */
    XLSX(".xlsx");

    String fromat;

    ExcelFileType(String fromat) {
        this.fromat = fromat;
    }

    public String getFromat() {
        return fromat;
    }

}
