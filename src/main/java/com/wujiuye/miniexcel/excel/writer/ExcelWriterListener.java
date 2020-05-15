/**
 * Copyright [2019-2020] [wujiuye]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wujiuye.miniexcel.excel.writer;

import java.util.List;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/30 {描述：}
 */
public interface ExcelWriterListener<T> {

    /**
     * 出异常时被调用
     */
    default void onError(Exception e) {
        e.printStackTrace();
    }

    /**
     * 获取输出的数据的类型
     *
     * @return
     */
    Class<T> getDataObjectClass();

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
