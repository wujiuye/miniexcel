package com.wujiuye.miniexcel.excel.reader;

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/13 {描述：}
 */
public final class XLS2003Reader extends BigRowsExcelReader {

    XLS2003Reader(String filePath, boolean readCellTitle) {
        super(filePath, readCellTitle);
    }

    @Override
    public void doRead() {
        FileInputStream is = null;
        InputStream inputStream = null;
        try {
            is = new FileInputStream(filePath);
            POIFSFileSystem poifs = new POIFSFileSystem(is);
            inputStream = poifs.createDocumentInputStream("Workbook");
            HSSFRequest req = new HSSFRequest();
            // 为HSSFRequest增加HSSFListener
            req.addListenerForAllRecords(new ExcelReadeListener());
            HSSFEventFactory factory = new HSSFEventFactory();
            // 处理inputstream
            factory.processEvents(req, inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭inputstream
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 1、使用POI事件模式解析Excel 2003文件，需要先将Excel 2003文件转化为POI中POIFSFileSystem对象
     * 2、实现接口HSSFListener，实现自己的监听器listener
     * 3、通过Record.sid 为某些特定的Record设置监听listener
     * 4、根据Excel 2003文件路径获取该文件的输入流FileInputStream - in
     * 5、根据输入流in创建POIFSFileSysytem实例对象poifs
     * 6、第二个目录条目名字是Workbook，找到第二目录条目，根据对应的流创建一个输入流DocumentInputStream
     * 7、根据输入流DocumentInputStream，解析为一个个记录Record
     * 8、如果解析出的Record设置了监听，触发监听事件
     * 9、处理监听器中事件
     */
    private class ExcelReadeListener implements HSSFListener {

        private String currentSheetName;
        private SSTRecord sheetData;

        /**
         * BOFRecord.sid, 				// HSSFWorkbook、HSSFSheet的开始
         * EOFRecord.sid, 				// HSSFWorkbook、HSSFSheet的结束
         * BoundSheetRecord.sid, 		// BoundSheetRecord记录了sheetName
         * SSTRecord.sid, 				// SSTRecord记录了所有Sheet的文本单元格的文本
         * DimensionsRecord.sid, 		// DimensionsRecord记录了每个Sheet的有效起始结束行列索引
         * MergeCellsRecord.sid, 		// MergeCellsRecord记录了每个Sheet中的合并单元格信息
         * ExtendedFormatRecord.sid, 	// ExtendedFormatRecord记录了扩展的单元格样式
         * FormatRecord.sid, 			// FormatRecord记录单元格样式信息
         * ColumnInfoRecord.sid,		// ColumnInfoRecord记录了Sheet中列信息，如列是否隐藏
         * RowRecord.sid,				// RowRecord记录了Sheet中行信息，如行索引，行是否隐藏
         * BlankRecord.sid, 			// Sheet中空单元格，存在单元格样式
         * BoolErrRecord.sid, 			// Sheet中布尔或错误单元格
         * FormulaRecord.sid, 			// Sheet中公式单元格
         * LabelSSTRecord.sid, 		    // Sheet中文本单元格
         * NumberRecord.sid			    // Sheet中数值单元格：数字单元格和日期单元格
         *
         * @param record
         */
        @Override
        public void processRecord(Record record) {
            switch (record.getSid()) {
                // 标记workbook或sheet开始，这里会进行判断
                case BOFRecord.sid:
                    break;
                //处理sheet,多个sheet会按sheet的顺序读取
                case BoundSheetRecord.sid:
                    BoundSheetRecord bsr = (BoundSheetRecord) record;
                    String sheetName = bsr.getSheetname();
                    this.currentSheetName = sheetName;
                    XLS2003Reader.this.excelReaderListener.onReadSheetStart(this.currentSheetName);
                    break;
                // 包含一个sheet的所有文本单元格
                case SSTRecord.sid:
                    SSTRecord sstrec = (SSTRecord) record;
                    this.sheetData = sstrec;
                    break;
                //文本单元格处理
                case LabelSSTRecord.sid:
                    LabelSSTRecord lrec = (LabelSSTRecord) record;
                    if (lrec.getRow() == 0 && XLS2003Reader.this.readCellTitle) {
                        XLS2003Reader.this.excelReaderListener.onReadSheetTitle(lrec.getColumn(),
                                this.sheetData.getString(lrec.getSSTIndex()).getString());
                    } else {
                        XLS2003Reader.this.excelReaderListener.onReadRow(this.sheetData.getString(lrec.getSSTIndex()).getString(),
                                XLS2003Reader.this.readCellTitle ? lrec.getRow() - 1 : lrec.getRow(),
                                lrec.getColumn());
                    }
                    break;
                //数值单元格和日期单元格处理
                case NumberRecord.sid:
                    NumberRecord numrec = (NumberRecord) record;
                    XLS2003Reader.this.excelReaderListener.onReadRow(numrec.getValue(), numrec.getRow(), numrec.getColumn());
                    break;
                case EOFRecord.sid:
                    break;
            }
        }
    }

}
