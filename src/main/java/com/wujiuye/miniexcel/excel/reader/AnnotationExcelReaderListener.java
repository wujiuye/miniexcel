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

    public AnnotationExcelReaderListener(Class<T> tClass) {
        this.tClass = tClass;
        List<ExcelMetaData> metaData = CellAnnotationParser.getFieldWithTargetClass(tClass);
        fieldMap = new HashMap<>();
        for (ExcelMetaData excelMetaData : metaData) {
            fieldMap.put(excelMetaData.getCellName(), excelMetaData.getField());
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
            ReflectUtils.applyValueBy(this.rows.get(this.rows.size() - 1), field, data);
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

