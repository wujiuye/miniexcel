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
package com.wujiuye.miniexcel.excel.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author wujiuye
 * @version 1.0 on 2019/3/11 {描述：
 * 时间转化工具，使用java8新的时间api
 * }
 */
public class DateUtils {

    private static final ConcurrentMap<String, DateTimeFormatter> FORMATTER_MAP = new ConcurrentHashMap<>();

    private static DateTimeFormatter getFormatter(String pattern) {
        return FORMATTER_MAP.computeIfAbsent(pattern, s -> DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 将字符串日期转为Date
     *
     * @param strDate  日期字符串
     * @param pattern  日期格式
     * @param timeZone 时区
     * @return
     */
    public static Date toDate(String strDate, String pattern, int timeZone) {
        if (StringUtils.isEmpty(strDate)) {
            return null;
        }
        ZoneOffset timeZoneOfferset = getZoneOffersetStr(timeZone);
        if (pattern.contains("HH")) {
            LocalDate localDate = LocalDate.parse(strDate, getFormatter(pattern));
            return Date.from(localDate.atStartOfDay().atZone(timeZoneOfferset).toInstant());
        } else {
            LocalDateTime localDate = LocalDateTime.parse(strDate, getFormatter(pattern));
            return Date.from(localDate.atZone(timeZoneOfferset).toInstant());
        }
    }

    /**
     * date转字符串日期
     *
     * @param date     日期
     * @param pattern  日期格式
     * @param timeZone 时区
     * @return
     */
    public static String fromDate(Date date, String pattern, int timeZone) {
        if (date == null) {
            return null;
        }
        ZoneOffset timeZoneOfferset = getZoneOffersetStr(timeZone);
        ZonedDateTime zonedDateTime = date.toInstant().atZone(timeZoneOfferset);
        if (pattern.contains("HH")) {
            return zonedDateTime.toLocalDateTime().format(getFormatter(pattern));
        } else {
            return zonedDateTime.toLocalDate().atStartOfDay(timeZoneOfferset).format(getFormatter(pattern));
        }
    }

    /**
     * 获取时区的ZoneOffset
     * ZoneOffset.of底层已经做了缓存
     *
     * @param timeZone 取之：-12 ~ 0 ~ +12
     * @return
     */
    private static ZoneOffset getZoneOffersetStr(int timeZone) {
        String zoneStr;
        if (timeZone >= 0) {
            zoneStr = "+" + timeZone;
        } else {
            zoneStr = String.valueOf(timeZone);
        }
        return ZoneOffset.of(zoneStr);
    }

}
