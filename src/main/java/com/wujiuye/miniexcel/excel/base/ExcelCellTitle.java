package com.wujiuye.miniexcel.excel.base;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/30 {描述：}
 */
@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Documented
public @interface ExcelCellTitle {

    int cellNumber() default -1;//-1为不参与排序

    String alias();//为null时，取属性的字段名
}
