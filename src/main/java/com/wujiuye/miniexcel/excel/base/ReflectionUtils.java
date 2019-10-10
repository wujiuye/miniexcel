package com.wujiuye.miniexcel.excel.base;


import com.wujiuye.miniexcel.excel.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/30 {描述：}
 */
public class ReflectionUtils {

    public static List<ExcelMetaData> getFieldWithTargetClass(Class targetClass) {
        List<ExcelMetaData> excelMetaData = new ArrayList<>();
        Field[] fields = targetClass.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                ExcelMetaData metaData = new ExcelMetaData();
                metaData.setField(field);
                metaData.setFieldName(field.getName());
                metaData.setTargetClass(targetClass);
                ExcelCellTitle cellTitle = metaData.getField().getAnnotation(ExcelCellTitle.class);
                if (cellTitle == null || StringUtils.isEmpty(cellTitle.alias())) {
                    metaData.setCellName(field.getName());
                } else {
                    metaData.setCellName(cellTitle.alias());
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
            Collections.sort(list, (o1, o2) -> o1.getKey() - o2.getKey());
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
