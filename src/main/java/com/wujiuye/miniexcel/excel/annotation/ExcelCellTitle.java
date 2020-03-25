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

    /**
     * -1为不参与排序
     * 用于列的排序
     *
     * @return
     */
    int cellNumber() default -1;

    /**
     * 列名，为null时，取属性的字段名
     *
     * @return
     */
    String alias() default "";

    /**
     * 是否忽略这列（这个字段）
     *
     * @return
     */
    boolean ignore() default false;

}
