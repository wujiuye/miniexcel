package com.wujiuye.miniexcel.test.model;

import com.wujiuye.miniexcel.excel.annotation.ExcelCellTitle;
import com.wujiuye.miniexcel.excel.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 测试Model
 *
 * @author wujiuye 2020/05/12
 */
public class DateModel {

    @ExcelCellTitle
    private String xxxx;
    @ExcelCellTitle
    private int yyy;
    @ExcelCellTitle
    private Integer xxx;
    @ExcelCellTitle(cellNumber = 1, alias = "别名", datePattern = "yyyy-MM-dd HH", timeZone = 8)
    private Date date;

    public static List<DateModel> getTestData() {
        List<DateModel> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DateModel dateModel = new DateModel();
            if ((i & 0x01) == 1) {
                dateModel.date = new Date();
            } else {
                dateModel.date = null;
            }
            dateModel.xxx = 1;
            dateModel.xxxx = "测试";
            list.add(dateModel);
        }
        return list;
    }

    @Override
    public String toString() {
        return xxxx + ',' +
                yyy + ',' +
                xxx + ',' +
                DateUtils.fromDate(date, "yyyy-MM-dd HH:mm:ss", 8);
    }

}
