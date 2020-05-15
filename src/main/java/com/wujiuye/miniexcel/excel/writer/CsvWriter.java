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

import com.wujiuye.miniexcel.excel.ExcelFileType;
import com.wujiuye.miniexcel.excel.annotation.ExcelMetaData;
import com.wujiuye.miniexcel.excel.util.DateUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * @author wujiuye
 * @version 1.0 on 2020/05/14 {描述：}
 */
public class CsvWriter extends AbstractExcelWriter {

    CsvWriter(String filePath, ExcelFileType format) {
        super(filePath, format);
    }

    CsvWriter(OutputStream ot, ExcelFileType format) {
        super(ot, format);
    }

    @Override
    protected void doWrite(OutputStream ot, ExcelWriterListener<?> writerListener, List<ExcelMetaData> metaDatas) {
        if (ot instanceof FileOutputStream) {
            nioWrite((FileOutputStream) ot, writerListener, metaDatas);
        } else {
            bioWrite(ot, writerListener, metaDatas);
        }
    }

    private void nioWrite(FileOutputStream ot, ExcelWriterListener<?> writerListener, List<ExcelMetaData> metaDatas) {
        boolean needWriter = writerListener.autoGenerateTitle();
        int sheetNumber = 1;
        // 当前页输出的大小
        int currentPageSize;
        int nextLimit = 0;
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);
        try (FileChannel fileChannel = ot.getChannel()) {
            fileChannel.lock();
            // 输出标题
            if (needWriter) {
                buffer.put(getTitle(metaDatas));
                buffer.putChar('\n');
            }
            while ((currentPageSize = writerListener.getNetOutputDataRealSize(sheetNumber)) != 0) {
                List<?> data = writerListener.getOutputDataWithSheetNumber(sheetNumber, nextLimit, nextLimit + currentPageSize);
                for (Object row : data) {
                    byte[] bytes = getRowData(metaDatas, row);
                    if (buffer.remaining() > bytes.length + 1) {
                        buffer.put(bytes);
                        buffer.putChar('\n');
                    } else {
                        buffer.flip();
                        fileChannel.write(buffer);
                        buffer.clear();
                    }
                }
                nextLimit += currentPageSize;
                //换sheet。 csv没有sheet，只是为了兼容实现分页查询导出而虚拟的
                if (nextLimit >= this.sheetSize * sheetNumber) {
                    sheetNumber++;
                }
            }
            if (buffer.position() != buffer.limit()) {
                buffer.flip();
                fileChannel.write(buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            writerListener.onError(e);
        }
    }

    private void bioWrite(OutputStream ot, ExcelWriterListener<?> writerListener, List<ExcelMetaData> metaDatas) {
        boolean needWriter = writerListener.autoGenerateTitle();
        int sheetNumber = 1;
        // 当前页输出的大小
        int currentPageSize;
        int nextLimit = 0;
        byte[] hh = "\n".getBytes(StandardCharsets.UTF_8);
        try {
            // 输出标题
            if (needWriter) {
                ot.write(getTitle(metaDatas));
                ot.write(hh);
            }
            while ((currentPageSize = writerListener.getNetOutputDataRealSize(sheetNumber)) != 0) {
                List<?> data = writerListener.getOutputDataWithSheetNumber(sheetNumber, nextLimit, nextLimit + currentPageSize);
                for (Object row : data) {
                    byte[] bytes = getRowData(metaDatas, row);
                    ot.write(bytes);
                    ot.write(hh);
                }
                nextLimit += currentPageSize;
                //换sheet。 csv没有sheet，只是为了兼容实现分页查询导出而虚拟的
                if (nextLimit >= this.sheetSize * sheetNumber) {
                    sheetNumber++;
                }
            }
            ot.flush();
        } catch (IOException e) {
            writerListener.onError(e);
        }
    }

    private byte[] getTitle(List<ExcelMetaData> titles) {
        StringBuilder titleRowData = new StringBuilder();
        boolean fisrt = true;
        for (ExcelMetaData title : titles) {
            if (!fisrt) {
                titleRowData.append(",");
            } else {
                fisrt = false;
            }
            titleRowData.append(title.getCellName());
        }
        return titleRowData.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] getRowData(List<ExcelMetaData> metaDatas, Object data) {
        StringBuilder titleRowData = new StringBuilder();
        boolean fisrt = true;
        for (ExcelMetaData metaData : metaDatas) {
            if (!fisrt) {
                titleRowData.append(",");
            } else {
                fisrt = false;
            }
            metaData.getField().setAccessible(true);
            Object cellDateVlaue = null;
            try {
                cellDateVlaue = metaData.getField().get(data);
            } catch (IllegalAccessException ignored) {
            }
            if (cellDateVlaue == null) {
                continue;
            }
            Class<?> cellClass = cellDateVlaue.getClass();
            if (cellClass == Date.class) {
                titleRowData.append(DateUtils.fromDate((Date) cellDateVlaue, metaData.getDatePattern(), metaData.getTimezone()));
            } else {
                titleRowData.append(cellDateVlaue.toString());
            }
        }
        return titleRowData.toString().getBytes(StandardCharsets.UTF_8);
    }

}
