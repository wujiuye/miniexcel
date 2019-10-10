package com.wujiuye.miniexcel.excel.base;

/**
 * @author wujiuye
 * @version 1.0 on 2019/10/10 {描述：
 * 列指示器
 * }
 */
public interface ColumnsDesignator {

    /**
     * 是否忽略该列
     *
     * @param column data的数据类型的字段名
     * @return
     */
    boolean isIgnore(String column);

    /**
     * 是否需要重命名
     *
     * @param column data的数据类型的字段名
     * @return 不需要重命名则直接返回参数column，需要重命名则返回重命名后的列名（excel文件的标题名称）
     */
    String renameColumn(String column);

}
