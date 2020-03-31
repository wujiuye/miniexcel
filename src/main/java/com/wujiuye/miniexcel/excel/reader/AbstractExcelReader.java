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

import com.wujiuye.miniexcel.excel.ExcelFileType;

import java.io.File;
import java.io.InputStream;


/**
 * @author wujiuye
 * @version 1.0 on 2019/4/13 {描述：}
 */
public abstract class AbstractExcelReader {

    protected boolean readCellTitle;
    protected String filePath;
    protected InputStream inputStream;
    protected ExcelReaderListener excelReaderListener;

    /**
     * 根据文件输入流和文件类型创建一个读取器
     *
     * @param in            excel文件输入流
     * @param type          文件后缀类型
     * @param readCellTitle 是否读取列标题
     * @return
     */
    public static AbstractExcelReader getReader(InputStream in, ExcelFileType type, boolean readCellTitle) {
        switch (type) {
            case XLS:
                return new XLS2003Reader(in, readCellTitle);
            case XLSX:
                return new XLSX2007Reader(in, readCellTitle);
            case CSV:
            default:
                throw new RuntimeException("不支持该文件格式！！！");
        }
    }

    /**
     * 根据文件路径创建一个reader
     *
     * @param filePath      文件绝对路径（含后缀名）
     * @param readCellTitle 是否把表格的第一行作为标题
     * @return
     */
    public static AbstractExcelReader getReader(String filePath, boolean readCellTitle) {
        //根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
        if (filePath.toUpperCase().endsWith(".XLS")) {
            return new XLS2003Reader(filePath, readCellTitle);
        } else if (filePath.toUpperCase().endsWith(".XLSX")) {
            return new XLSX2007Reader(filePath, readCellTitle);
        }
        throw new RuntimeException("不支持该文件格式！！！");
    }

    /**
     * 构造方法
     *
     * @param filePath      excel文件的路径
     * @param readCellTitle 是否需要读取列标题，即把第一行的数据当初列标题
     */
    AbstractExcelReader(String filePath, boolean readCellTitle) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("file {" + filePath + "} no exists!!!");
        }
        this.readCellTitle = readCellTitle;
        this.filePath = filePath;
    }

    /**
     * 构造方法
     *
     * @param in            excel文件的输入流
     * @param readCellTitle 是否需要读取列标题，即把第一行的数据当初列标题
     */
    AbstractExcelReader(InputStream in, boolean readCellTitle) {
        this.readCellTitle = readCellTitle;
        this.inputStream = in;
    }

    /**
     * 开始读取文件内容
     *
     * @param readerListener 读取监听器，负责怎么读取数据
     */
    public void read(ExcelReaderListener readerListener) {
        if (readerListener == null) {
            throw new RuntimeException("监控器不能为null！！！");
        }
        this.excelReaderListener = readerListener;
        this.doRead();
    }

    protected abstract void doRead();
}
