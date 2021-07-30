package com.wujiuye.miniexcel.test.csv;

import com.wujiuye.miniexcel.excel.reader.AbstractExcelReader;
import com.wujiuye.miniexcel.excel.reader.ExcelReaderListener;
import com.wujiuye.miniexcel.test.model.DateModel;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试 NIO写CSV文件
 *
 * @author wujiuye 2020/05/12
 */
public class NioWriteCsvTest {

    @Test
    public void testReadCsv() {
        String fileName = "/tmp/1627665504259.csv";
        AbstractExcelReader reader = AbstractExcelReader.getReader(fileName);
        reader.read(new ExcelReaderListener() {
            @Override
            public void onReadSheetStart(String sheetName) {
                System.out.println("sheetName=>" + sheetName);
            }

            @Override
            public void onReadSheetTitle(int cellNumber, String cellTitle) {
                System.out.println("onReadSheetTitle=>[" + cellNumber + "," + cellTitle + "]");
            }

            @Override
            public void onReadRow(Object data, int rowNumber, int cellNumber) {
                System.out.println("onReadRow=>[" + rowNumber + "," + cellNumber + "," + data + "]");
            }
        });
        System.out.println("finish....");
    }

    @Test
    public void testWriteCsv() throws IOException {
        while (!Thread.interrupted()) {
            List<DateModel> modelList = new ArrayList<>(1000000);
            for (int i = 0; i < 100000; i++) {
                modelList.addAll(DateModel.getTestData());
            }
            long start = System.currentTimeMillis();
            try (FileOutputStream out = new FileOutputStream("/tmp/" + System.currentTimeMillis() + ".csv");
                 FileChannel fileChannel = out.getChannel()) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);
                for (DateModel model : modelList) {
                    byte[] bytes = model.toString().getBytes(StandardCharsets.UTF_8);
                    if (buffer.remaining() > bytes.length + 1) {
                        buffer.put(bytes);
                        buffer.putChar('\n');
                    } else {
                        buffer.flip();
                        fileChannel.write(buffer);
                        buffer.clear();
                    }
                }
                if (buffer.position() != buffer.limit()) {
                    buffer.flip();
                    fileChannel.write(buffer);
                    buffer.clear();
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
            System.out.println("总耗时：" + (System.currentTimeMillis() - start) + " ms");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
