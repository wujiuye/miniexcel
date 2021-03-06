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
package com.wujiuye.miniexcel.excel.reader;

import com.wujiuye.miniexcel.excel.annotation.CellAnnotationParser;
import com.wujiuye.miniexcel.excel.annotation.ExcelMetaData;
import com.wujiuye.miniexcel.excel.util.ReflectUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 按注解读取的Execl读取监听器
 * 获取读取器的时候记得是否读取列标题要选择true
 * AbstractExcelReader.getReader(file.getPath(), true);
 *
 * @param <T>
 * @author wujiuye 2020/03/30
 */
public class AnnotationExcelReaderListener<T> extends ExcelReaderListenerAdapter {

    /**
     * 当前读取的行号
     */
    protected int currentRowNo = 0;
    /**
     * 读取结果
     */
    protected List<T> rows;
    /**
     * 泛型类型
     */
    private Class<T> tClass;
    /**
     * 字段和列标题映射
     * 当注解写了别名时，就是注解上的别名，支持中文
     */
    private Map<String, Field> fieldMap;
    /**
     * 列号与列标题的映射
     */
    private Map<Integer, String> cellNumberTitleMap;

    protected Map<String, ExcelMetaData> metaDataMap;

    public AnnotationExcelReaderListener(Class<T> tClass) {
        this.tClass = tClass;
        List<ExcelMetaData> metaDataList = CellAnnotationParser.getFieldWithTargetClass(tClass);
        fieldMap = new HashMap<>();
        metaDataMap = new HashMap<>();
        for (ExcelMetaData excelMetaData : metaDataList) {
            fieldMap.put(excelMetaData.getCellName(), excelMetaData.getField());
            metaDataMap.put(excelMetaData.getCellName(), excelMetaData);
        }
        this.rows = new ArrayList<>();
    }

    /**
     * 读取列号与标题的映射，请确保对象的字段名或字段上注解声明的别名与excel表格的列标题对应
     *
     * @param cellNumber 列号
     * @param cellTitle  列标题
     */
    @Override
    public void onReadSheetTitle(int cellNumber, String cellTitle) {
        if (cellNumberTitleMap == null) {
            cellNumberTitleMap = new HashMap<>();
        }
        cellNumberTitleMap.put(cellNumber, cellTitle);
    }

    /**
     * 读取数据
     *
     * @param data       当前（行，列）的数据
     * @param rowNumber  当前行号
     * @param cellNumber 当前列号
     */
    @Override
    public void onReadRow(Object data, int rowNumber, int cellNumber) {
        if (Objects.isNull(data)) {
            return;
        }
        // -1 去掉标题
        if (currentRowNo != rowNumber) {
            try {
                this.rows.add(tClass.newInstance());
                currentRowNo = rowNumber;
            } catch (Exception e) {
                throw new RuntimeException("new obj error! ");
            }
        }
        String cellName = cellNumberTitleMap.get(cellNumber);
        Field field = fieldMap.get(cellName);
        // 该列被设置为忽略了
        if (field == null) {
            return;
        }
        try {
            ReflectUtils.applyValueBy(this.rows.get(this.rows.size() - 1), field, data, metaDataMap.get(cellName));
        } catch (IllegalAccessException e) {
            throw new RuntimeException("字段赋值失败：" + e.getLocalizedMessage());
        }
    }

    /**
     * 获取读取的所有记录
     *
     * @return
     */
    public List<T> getRecords() {
        return rows == null ? new ArrayList<>() : rows.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

}

