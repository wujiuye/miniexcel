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

import com.wujiuye.miniexcel.excel.util.LineBufferReader;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author wujiuye
 * @version 1.0 on 2020/05/15 支持CSV格式读取
 */
public class CsvReader extends AbstractExcelReader {

    CsvReader(String filePath, boolean readCellTitle) {
        super(filePath, readCellTitle);
    }

    CsvReader(InputStream in, boolean readCellTitle) {
        super(in, readCellTitle);
    }

    @Override
    protected void doRead() {
        if (inputStream == null) {
            try {
                inputStream = new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            if (inputStream instanceof FileInputStream) {
                this.nioRead((FileInputStream) inputStream);
            } else {
                this.bioRead(inputStream);
            }
        } finally {
            if (inputStream instanceof FileInputStream) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void nioRead(FileInputStream inputStream) {
        this.excelReaderListener.onReadSheetStart("Sheet1");
        int row = 0;
        try (FileChannel channel = inputStream.getChannel()) {
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            LineBufferReader reader = new LineBufferReader(buffer);
            while (true) {
                String str = reader.readLine();
                if (str != null) {
                    onReadLine(row++, str);
                } else {
                    break;
                }
            }
            buffer.clear();
        } catch (IOException e) {
            this.excelReaderListener.onError(e);
        }
    }

    private void bioRead(InputStream inputStream) {
        this.excelReaderListener.onReadSheetStart("Sheet1");
        int row = 0;
        try (InputStreamReader isr = new InputStreamReader(inputStream);
             BufferedReader reader = new BufferedReader(isr)) {
            String str;
            while (true) {
                str = reader.readLine();
                if (str != null) {
                    onReadLine(row++, str);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            this.excelReaderListener.onError(e);
        }
    }

    private void onReadLine(int row, String line) {
        String[] cells = line.split(",");
        for (int i = 0; i < cells.length; i++) {
            if (row == 0 && this.readCellTitle) {
                this.excelReaderListener.onReadSheetTitle(i, cells[i]);
            } else {
                this.excelReaderListener.onReadRow(cells[i], row, i);
            }
        }
    }

}
