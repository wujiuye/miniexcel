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
