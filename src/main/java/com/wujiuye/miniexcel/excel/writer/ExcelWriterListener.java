package com.wujiuye.miniexcel.excel.writer;

import java.util.List;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/30 {描述：}
 */
public interface ExcelWriterListener<T> {

    /**
     * 出异常时调用
     */
    default void onError(Exception e) {
        e.printStackTrace();
    }

    /**
     * 获取输出的数据的类型
     *
     * @return
     */
    Class getDataObjectClass();

    /**
     * 是否需要自动生成标题输出，使用反射获取范型T的字段名作为标题
     *
     * @return 标题会被连续的输出到第一行的连续列
     */
    boolean autoGenerateTitle();

    /**
     * 每次刷盘一次完成后，继续写入数据时先调用该方法获取下一次输出的数据真实的总数
     *
     * @param sn 第几个sheet
     * @return 返回0则结束导出
     */
    int getNetOutputDataRealSize(int sn);

    /**
     * 获取本次需要输出的数据
     *
     * @param sn         第几个sheet
     * @param limitStart 上次完成刷盘后的指针位置 (从0开始) （闭区间-包含）
     * @param limitEnd   limitStart + getOutputDataSizeWithSheetNumber返回的值 (开区间-不包含)
     * @return 不运行返回null，否则抛出异常
     */
    List<T> getOutputDataWithSheetNumber(int sn, int limitStart, int limitEnd);

}
