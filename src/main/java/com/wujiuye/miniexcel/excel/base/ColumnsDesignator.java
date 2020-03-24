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
