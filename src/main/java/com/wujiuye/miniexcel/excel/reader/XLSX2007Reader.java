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

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/13 {描述：}
 */
public final class XLSX2007Reader extends BigRowsExcelReader {

    XLSX2007Reader(String filePath, boolean readCellTitle) {
        super(filePath, readCellTitle);
    }

    @Override
    protected void doRead() {
        OPCPackage pkg = null;
        try {
            pkg = OPCPackage.open(filePath, PackageAccess.READ);
            XSSFReader reader = new XSSFReader(pkg);
            XMLReader parser = XMLReaderFactory.createXMLReader();
            // 处理公共属性：Sheet名，Sheet合并单元格
            SharedStringsTable sst = reader.getSharedStringsTable();
            parser.setContentHandler(new SheetHandler(sst));
            /**
             * 返回一个迭代器，此迭代器会依次得到所有不同的sheet。
             * 每个sheet的InputStream只有从Iterator获取时才会打开。
             * 解析完每个sheet时关闭InputStream。
             * */
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();
            int sheetIndex = 0;
            while (sheets.hasNext()) {
                this.excelReaderListener.onReadSheetStart("sheet" + sheetIndex);
                InputStream sheetstream = sheets.next();
                InputSource sheetSource = new InputSource(sheetstream);
                try {
                    // 解析sheet
                    parser.parse(sheetSource);
                } finally {
                    sheetstream.close();
                }
                sheetIndex++;
            }
        } catch (OpenXML4JException | IOException | SAXException e) {
            e.printStackTrace();
        } finally {
            if (pkg != null) {
                try {
                    pkg.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class SheetHandler extends DefaultHandler {
        private SharedStringsTable sst;
        private String lastContents;
        private boolean nextIsString;
        private String index = null;
        private int thisColumnIndex;
        private int currentRow;

        // 日期标志
        private boolean dateFlag;
        // 数字标志
        private boolean numberFlag;
        private boolean isTElement;


        private SheetHandler(SharedStringsTable sst) {
            this.sst = sst;
            this.currentRow = 0;
            this.thisColumnIndex = 0;
        }


        /**
         * 开始元素
         */
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            if (name.equals("c")) {
                index = attributes.getValue("r");
                int firstDigit = -1;
                for (int c = 0; c < index.length(); ++c) {
                    if (Character.isDigit(index.charAt(c))) {
                        firstDigit = c;
                        break;
                    }
                }
                thisColumnIndex = nameToColumn(index.substring(0, firstDigit));

                // 判断是否是新的一行
                if (Pattern.compile("^A[0-9]+$").matcher(index).find()) {
                    currentRow++;
                }
                String cellType = attributes.getValue("t");
                if (cellType != null && cellType.equals("s")) {
                    nextIsString = true;
                } else {
                    nextIsString = false;
                }
                // 日期格式
                String cellDateType = attributes.getValue("s");
                if ("1".equals(cellDateType)) {
                    dateFlag = true;
                } else {
                    dateFlag = false;
                }
                String cellNumberType = attributes.getValue("s");
                if ("2".equals(cellNumberType)) {
                    numberFlag = true;
                } else {
                    numberFlag = false;
                }
            } else if ("t".equals(name)) {
                // 当元素为t时
                isTElement = true;
            } else {
                isTElement = false;
            }
            lastContents = "";
        }

        private int nameToColumn(String name) {
            int column = -1;
            for (int i = 0; i < name.length(); ++i) {
                int c = name.charAt(i);
                column = (column + 1) * 26 + c - 'A';
            }
            return column;
        }

        /**
         * 获取value
         */
        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (nextIsString) {
                int idx = Integer.parseInt(lastContents);
                lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
                nextIsString = false;
            }
            // t元素也包含字符串
            if (isTElement) {
                String value = lastContents.trim();
                if (XLSX2007Reader.this.readCellTitle && currentRow == 1) {
                    XLSX2007Reader.this.excelReaderListener.onReadSheetTitle(thisColumnIndex, value);
                } else {
                    XLSX2007Reader.this.excelReaderListener.onReadRow(value, XLSX2007Reader.this.readCellTitle ? currentRow - 1 : currentRow, thisColumnIndex);
                }
                isTElement = false;
            } else if ("v".equals(name)) {
                // v => 单元格的值，如果单元格是字符串则v标签的值为该字符串在SST中的索引
                String value = lastContents.trim();
                // 日期格式处理
                if (dateFlag) {
                    try {
                        Date date = HSSFDateUtil.getJavaDate(Double.valueOf(value));
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        value = dateFormat.format(date);
                    } catch (NumberFormatException e) {
                    }
                }
                // 数字类型处理
                if (numberFlag) {
                    try {
                        BigDecimal bd = new BigDecimal(value);
                        value = bd.setScale(3, BigDecimal.ROUND_UP).toString();
                    } catch (Exception e) {
                    }
                }
                if (XLSX2007Reader.this.readCellTitle && currentRow == 1) {
                    XLSX2007Reader.this.excelReaderListener.onReadSheetTitle(thisColumnIndex, value);
                } else {
                    XLSX2007Reader.this.excelReaderListener.onReadRow(value, XLSX2007Reader.this.readCellTitle ? currentRow - 1 : currentRow, thisColumnIndex);
                }
            } else if (name.equals("row")) {
                //行读取完了
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            lastContents += new String(ch, start, length);
        }

    }

}
