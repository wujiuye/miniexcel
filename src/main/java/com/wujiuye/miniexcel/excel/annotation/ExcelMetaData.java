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
package com.wujiuye.miniexcel.excel.annotation;

import java.lang.reflect.Field;

/**
 * 列与字段的映射，列描述元数据
 *
 * @author wujiuye
 * @version 1.0 on 2019/4/30 {描述：}
 */
public class ExcelMetaData {

    /**
     * 字段
     */
    private Field field;
    /**
     * 列名
     */
    private String cellName;
    /**
     * 针对日期类型
     */
    private String datePattern;
    /**
     * 时区
     */
    private Integer timezone;

    public String getFieldName() {
        return field.getName();
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getCellName() {
        return cellName;
    }

    public void setCellName(String cellName) {
        this.cellName = cellName;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public Integer getTimezone() {
        return timezone;
    }

    public void setTimezone(Integer timezone) {
        this.timezone = timezone;
    }

}
