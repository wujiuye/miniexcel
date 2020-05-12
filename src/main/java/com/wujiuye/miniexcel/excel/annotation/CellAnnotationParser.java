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


import com.wujiuye.miniexcel.excel.util.StringUtils;

import java.lang.reflect.Field;
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
                if (cellTitle != null && cellTitle.ignore()) {
                    continue;
                }
                ExcelMetaData metaData = new ExcelMetaData();
                metaData.setField(field);
                if (cellTitle == null || StringUtils.isEmpty(cellTitle.alias())) {
                    metaData.setCellName(field.getName());
                } else {
                    metaData.setCellName(cellTitle.alias());
                }
                if (field.getType() == Date.class) {
                    if (cellTitle != null && !StringUtils.isEmpty(cellTitle.datePattern())) {
                        metaData.setDatePattern(cellTitle.datePattern());
                        metaData.setTimezone(cellTitle.timeZone());
                    }
                }
                excelMetaData.add(metaData);
            }
        }
        return excelMetaData;
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
