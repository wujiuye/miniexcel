package com.wujiuye.miniexcel.excel.reader;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/13 {描述：}
 */
public class DefaultExcelReaderListener implements ExcelReaderListener {

    /**
     * sheetName -> {cellIndex->cellName}
     */
    private Map<String, Map<Integer, String>> cellTitleMap = new HashMap<>();
    private String currentSheetName;
    /**
     * cellName -> value
     */
    private List<Map<String, Object>> data = new ArrayList<>();
    private int currentRow = -1;
    private Map<String, Object> currentRowData = new HashMap<>();

    private boolean ydCell = false;//是否约定只获取某些列
    private List<String> readCells;

    public DefaultExcelReaderListener() {

    }

    //是否只获取某些列的值
    public DefaultExcelReaderListener(String... cellName) {
        if (cellName != null && cellName.length > 0) {
            ydCell = true;
            readCells = Arrays.stream(cellName).collect(Collectors.toList());
        }
    }

    @Override
    public void onReadSheetStart(String sheetName) {
        this.currentSheetName = sheetName;
        if (!cellTitleMap.containsKey(this.currentSheetName)) {
            cellTitleMap.put(this.currentSheetName, new HashMap<>());
        }
    }

    @Override
    public void onReadSheetTitle(int cellNumber, String cellTitle) {
        this.cellTitleMap.get(this.currentSheetName).put(cellNumber, cellTitle);
    }

    @Override
    public void onReadRow(Object data, int rowNumber, int cellNumber) {
        String cellName = cellTitleMap.get(currentSheetName).get(cellNumber);
        if (ydCell && !readCells.contains(cellName)) {
            return;
        }
        if (this.currentRow != rowNumber) {
            this.data.add(new HashMap<>(currentRowData));
            currentRowData.clear();
            this.currentRow = rowNumber;
        }
        this.currentRowData.put(cellName, data);
    }

    public List<Map<String, Object>> getData() {
        return this.data;
    }
}
