package com.wujiuye.miniexcel.excel.base;

import java.lang.reflect.Field;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/30 {描述：}
 */
public class ExcelMetaData {

    private Class targetClass;
    private String fieldName;
    private Field field;
    private String cellName;

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
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
}
