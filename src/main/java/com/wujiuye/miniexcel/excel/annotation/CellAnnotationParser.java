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


import com.wujiuye.miniexcel.excel.MiniexcelContent;
import com.wujiuye.miniexcel.excel.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 注解解析器
 *
 * @author wujiuye
 * @version 1.0 on 2019/4/30 {描述：}
 */
public class CellAnnotationParser {

    /**
     * 获取包括父类的字段
     *
     * @param targetClass 目标类型
     * @return
     */
    private static Field[] getFields(Class<?> targetClass) {
        Field[] result = null;
        for (; targetClass != Object.class; targetClass = targetClass.getSuperclass()) {
            if (result == null) {
                result = targetClass.getDeclaredFields();
            } else {
                Field[] child = targetClass.getDeclaredFields();
                if (child.length > 0) {
                    Field[] newArray = new Field[result.length + child.length];
                    System.arraycopy(result, 0, newArray, 0, result.length);
                    System.arraycopy(child, 0, newArray, result.length, child.length);
                    result = newArray;
                }
            }
        }
        return result;
    }

    public static List<ExcelMetaData> getFieldWithTargetClass(Class<?> targetClass) {
        List<ExcelMetaData> excelMetaData = new ArrayList<>();
        Field[] fields = getFields(targetClass);
        if (fields.length > 0) {
            for (Field field : fields) {
                ExcelCellTitle cellTitle = field.getAnnotation(ExcelCellTitle.class);
                if (isIgnore(field, cellTitle)) {
                    continue;
                }
                ExcelMetaData metaData = new ExcelMetaData();
                metaData.setField(field);
                excelMetaData.add(metaData);
                if (cellTitle == null || StringUtils.isEmpty(cellTitle.alias())) {
                    metaData.setCellName(field.getName());
                } else {
                    metaData.setCellName(cellTitle.alias());
                }
                applyDateCfg(field, cellTitle, metaData);
            }
        }
        return excelMetaData;
    }

    /**
     * 忽略字段
     * 条件：
     * 1、注解不为空且注解的ignore为true
     * 2、全局配置了忽略不存在注解字段，且注解为空
     *
     * @param field     字段
     * @param cellTitle 字段上的注解
     * @return
     */
    private static boolean isIgnore(Field field, ExcelCellTitle cellTitle) {
        if (cellTitle != null && cellTitle.ignore()) {
            return true;
        }
        GlobalCfgProperties properties = MiniexcelContent.getGlobalCfgProperties();
        if (properties == null) {
            return false;
        }
        if (Boolean.TRUE.equals(properties.getIgnore_not_exist_annotation_field())
                && cellTitle == null) {
            return true;
        }
        return Boolean.TRUE.equals(properties.getIgnore_transient_field())
                && (field.getModifiers() & Modifier.TRANSIENT) == Modifier.TRANSIENT;
    }

    /**
     * 如果字段的类型为Date，则初始化日期格式化和时区配置
     *
     * @param field     字段
     * @param cellTitle 注解
     * @param metaData  元数据
     */
    private static void applyDateCfg(Field field, ExcelCellTitle cellTitle, ExcelMetaData metaData) {
        if (field.getType() != Date.class) {
            return;
        }
        // 注解优先
        if (cellTitle != null) {
            if (!StringUtils.isEmpty(cellTitle.datePattern())) {
                metaData.setDatePattern(cellTitle.datePattern());
            }
            if (cellTitle.timeZone() >= -12 && cellTitle.timeZone() <= 13) {
                metaData.setTimezone(cellTitle.timeZone());
            }
        }
        // 全局配置
        GlobalCfgProperties properties = MiniexcelContent.getGlobalCfgProperties();
        if (properties != null && properties.getDate_global_cfg() != null) {
            if (StringUtils.isEmpty(metaData.getDatePattern())
                    && properties.getDate_global_cfg().getDate_pattern() != null) {
                metaData.setDatePattern(properties.getDate_global_cfg().getDate_pattern());
            }
            if (metaData.getTimezone() == null
                    && properties.getDate_global_cfg().getTime_zone() != null) {
                metaData.setTimezone(properties.getDate_global_cfg().getTime_zone());
            }
        }
        // 默认
        if (StringUtils.isEmpty(metaData.getDatePattern())) {
            metaData.setDatePattern("yyyy-MM-dd HH:mm:ss");
        }
        if (metaData.getTimezone() == null) {
            metaData.setTimezone(8);
        }
    }

    /**
     * 按照列标题排序
     *
     * @param excelMetaData
     * @return
     */
    public static List<ExcelMetaData> sortField(List<ExcelMetaData> excelMetaData) {
        final List<ExcelMetaData> data = new ArrayList<>(excelMetaData.size());
        final ExcelMetaData[] sortData = new ExcelMetaData[excelMetaData.size()];
        final List<ExcelMetaData> notJoinSort = new ArrayList<>();
        Map<Integer, List<ExcelMetaData>> sortMap = new HashMap<>();
        for (ExcelMetaData metaData : excelMetaData) {
            ExcelCellTitle cellTitle = metaData.getField().getAnnotation(ExcelCellTitle.class);
            if (cellTitle == null) {
                notJoinSort.add(metaData);
                continue;
            }
            int sort = cellTitle.cellNumber();
            if (!sortMap.containsKey(sort)) {
                sortMap.put(sort, new ArrayList<>());
            }
            sortMap.get(sort).add(metaData);
        }
        int sortIndex = 0;
        // 处理需要排序的列
        if (sortMap.size() > 0) {
            List<Map.Entry<Integer, List<ExcelMetaData>>> list = new ArrayList<>(sortMap.entrySet());
            list.sort(Comparator.comparingInt(Map.Entry::getKey));
            for (Map.Entry<Integer, List<ExcelMetaData>> mapping : list) {
                if (mapping.getValue().size() > 0) {
                    for (ExcelMetaData emd : mapping.getValue()) {
                        sortData[sortIndex++] = emd;
                    }
                }
            }
        }
        // 没有参与排序的列全部放在最后
        for (int i = sortIndex; i < sortData.length; i++) {
            sortData[i] = notJoinSort.get(i - sortIndex);
        }
        data.addAll(Arrays.asList(sortData));
        return data;
    }

}
