package com.wujiuye.miniexcel.excel.reader;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/13 {描述：}
 */
public interface ExcelReaderListener {

    /**
     * 开始读取该Sheet
     *
     * @param sheetName sheet名
     */
    void onReadSheetStart(String sheetName);

    /**
     * 处理读取列标题
     *
     * @param cellNumber
     * @param cellTitle
     */
    void onReadSheetTitle(int cellNumber, String cellTitle);

    /**
     * 处理读取一行
     *
     * @param data       当前（行，列）的数据
     * @param rowNumber  当前行号
     * @param cellNumber 当前列号
     * @return
     */
    void onReadRow(Object data, int rowNumber, int cellNumber);

}
