package com.wujiuye.miniexcel.excel.util.properties;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/24 {描述：}
 */
@Target({ElementType.TYPE})
@Retention(RUNTIME)
@Documented
public @interface PropertiesAnnotation {

    /**
     * 文件路径，基于classpath
     *
     * @return
     */
    String filePath();

    /**
     * 属性名前缀
     *
     * @return
     */
    String prefix();

}
