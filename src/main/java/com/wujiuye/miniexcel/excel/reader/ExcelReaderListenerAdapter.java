package com.wujiuye.miniexcel.excel.reader;

import com.wujiuye.miniexcel.excel.annotation.ExcelMetaData;

import java.util.List;

/**
 * 读取监听器适配器
 *
 * @author wujiuye 2020/03/30
 */
public abstract class ExcelReaderListenerAdapter implements ExcelReaderListener {

    @Override
    public void onReadSheetStart(String sheetName) {

    }

    @Override
    public void onReadSheetTitle(int cellNumber, String cellTitle) {

    }

}
