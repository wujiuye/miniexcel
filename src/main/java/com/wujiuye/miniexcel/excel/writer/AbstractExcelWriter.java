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
package com.wujiuye.miniexcel.excel.writer;


import com.wujiuye.miniexcel.excel.annotation.ColumnsDesignator;

import java.io.File;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/30 {描述：}
 */
public abstract class AbstractExcelWriter {

    /**
     * 导出格式
     */
    public enum ExportFormatType {
        CSV(".csv"),
        XLS(".xls"),
        XLSX(".xlsx");
        String fromat;

        ExportFormatType(String fromat) {
            this.fromat = fromat;
        }

        public String getFromat() {
            return this.fromat;
        }
    }

    /**
     * 文件刷出的路径
     */
    protected String filePath;
    protected ExportFormatType format;
    /**
     * 刷盘记录的大小，即一次导出的记录数，应尽量的少，避免占用太多内存
     */
    protected int brushPageSize = 1000;
    /**
     * 每个sheet最大支持1048576行：超出会抛出IllegalArgumentException: Invalid row number (1048576) outside allowable range (0..1048575)
     */
    protected int sheetSize = 100_000;
    /**
     * 每个sheet最多保持多少调记录就自动切换到一个新的sheet
     * 格式中必须包含{sn}
     */
    protected String sheetNameFromat;
    /**
     * sm是第几个sheet，从1开始
     */
    private final String DEFAULT_SHEETNAME_FROMAT = "sheet_{sn}";

    public static AbstractExcelWriter createExcelWriter(String filePath, ExportFormatType format) {
        AbstractExcelWriter excelWriter;
        switch (format) {
            case XLS:
            case XLSX:
                excelWriter = new SXSSFWriter(filePath, format);
                break;
            case CSV:
            default:
                throw new FormatException("不支持的格式！！！");
        }
        return excelWriter;
    }

    AbstractExcelWriter(String filePath, ExportFormatType format) {
        this.filePath = filePath;
        this.format = format;
        this.sheetNameFromat = DEFAULT_SHEETNAME_FROMAT;
    }

    public AbstractExcelWriter setSheetSize(int sheetSize) {
        this.sheetSize = sheetSize;
        return this;
    }

    public AbstractExcelWriter setSheetNameFromat(String sheetNameFromat) {
        this.sheetNameFromat = sheetNameFromat;
        return this;
    }

    public AbstractExcelWriter setBrushPageSize(int brushPageSize) {
        this.brushPageSize = brushPageSize;
        return this;
    }

    public File write(ExcelWriterListener<?> writerListener) {
        return this.doWrite(writerListener, null);
    }

    /**
     * 支持指定导出列指示器，更丰富的自定义导致功能
     *
     * @param writerListener    写数据监听器
     * @param columnsDesignator 导出列指示器
     * @return
     */
    public File write(ExcelWriterListener<?> writerListener, ColumnsDesignator columnsDesignator) {
        return this.doWrite(writerListener, columnsDesignator);
    }


    protected abstract File doWrite(ExcelWriterListener<?> writerListener, ColumnsDesignator columnsDesignator);


}
