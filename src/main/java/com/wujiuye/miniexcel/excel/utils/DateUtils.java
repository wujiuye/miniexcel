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
package com.wujiuye.miniexcel.excel.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author wujiuye
 * @version 1.0 on 2019/3/11 {描述：
 * 时间转化工具，使用java8新的时间api
 * }
 */
public class DateUtils {


    /**
     * 将字符串日期转为Date
     *
     * @param strDate
     * @return
     */
    public static Date parsingDate(String strDate) {
        LocalDate localDate = LocalDate.parse(strDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return Date.from(localDate.atStartOfDay()
                .atZone(ZoneOffset.of("Asia/Shanghai"))
                .toInstant());
    }

    /**
     * 将字符串日期转为Date
     *
     * @param strDate
     * @return
     */
    public static Date parsingDatetime(String strDate) {
        LocalDateTime localDateTime = LocalDateTime.parse(strDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return Date.from(localDateTime
                .atZone(ZoneOffset.of("Asia/Shanghai"))
                .toInstant());
    }

    /**
     * date转字符串日期
     *
     * @param date
     * @return
     */
    public static String parsingDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * date转字符串日期
     *
     * @param date
     * @return
     */
    public static String parsingDate(Date date) {
        return date.toInstant().atZone(ZoneOffset.of("Asia/Shanghai"))
                .toLocalDate()
                .atStartOfDay(ZoneOffset.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * date转字符串日期
     *
     * @param date
     * @return
     */
    public static String parsingDatetime(Date date) {
        return date.toInstant().atZone(ZoneOffset.of("Asia/Shanghai"))
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Localdatetime转Date
     *
     * @param date
     * @return
     */
    public static Date parsingDatetime(LocalDateTime date) {
        return Date.from(date
                .atZone(ZoneOffset.of("Asia/Shanghai"))
                .toInstant());
    }

}
