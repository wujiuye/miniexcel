package com.wujiuye.miniexcel.excel.util;

import com.wujiuye.miniexcel.excel.writer.FormatException;

import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * 反射为对象设置值
 *
 * @author wujiuye 2020/03/30
 */
public class ReflectUtils {

    /**
     * 为对象的字段赋值
     *
     * @param obj   对象
     * @param field 字段
     * @param value 值
     * @throws IllegalAccessException
     */
    public static void applyValueBy(Object obj, Field field, Object value) throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        field.setAccessible(true);
        if (Integer.class.equals(fieldType)) {
            field.set(obj, getInt(value));
        } else if (Long.class.equals(fieldType)) {
            field.set(obj, getLong(value));
        } else if (Double.class.equals(fieldType)) {
            field.set(obj, getDouble(value));
        } else if (Float.class.equals(fieldType)) {
            field.set(obj, getFloat(value));
        } else if (BigDecimal.class.equals(fieldType)) {
            field.set(obj, getBigDecimal(value));
        } else if (String.class.equals(fieldType)) {
            field.set(obj, value.toString());
        } else {
            throw new FormatException("暂时不支持这种类型的自动映射值！class=" + obj.getClass()
                    + ",field=" + field.getName()
                    + ",fieldType=" + field.getType());
        }
    }

    private static Integer getInt(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else if (value instanceof Double || value instanceof Float) {
            return new BigDecimal(value.toString()).intValue();
        } else {
            return null;
        }
    }

    private static Long getLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof String) {
            return Long.parseLong((String) value);
        } else if (value instanceof Double || value instanceof Float) {
            return new BigDecimal(value.toString()).longValue();
        } else {
            return null;
        }
    }

    private static Float getFloat(Object value) {
        if (value instanceof Integer) {
            return Float.parseFloat(value.toString());
        } else if (value instanceof String) {
            return Float.parseFloat((String) value);
        } else if (value instanceof Double || value instanceof Float) {
            return new BigDecimal(value.toString()).floatValue();
        } else {
            return null;
        }
    }

    private static Double getDouble(Object value) {
        if (value instanceof Integer) {
            return Double.parseDouble(value.toString());
        } else if (value instanceof String) {
            return Double.parseDouble((String) value);
        } else if (value instanceof Double || value instanceof Float) {
            return new BigDecimal(value.toString()).doubleValue();
        } else {
            return null;
        }
    }

    private static BigDecimal getBigDecimal(Object value) {
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        } else if (value instanceof String) {
            return new BigDecimal((String) value);
        } else {
            return null;
        }
    }

}
