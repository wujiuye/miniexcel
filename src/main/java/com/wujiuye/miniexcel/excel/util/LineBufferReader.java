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
package com.wujiuye.miniexcel.excel.util;

import java.nio.MappedByteBuffer;

/**
 * @author wujiuye
 * @version 1.0 on 2020/05/15
 */
public class LineBufferReader {

    private MappedByteBuffer mappedByteBuffer;
    private byte[] buff = new byte[4096];
    private int lenth;
    private int readLenth = 0;
    private int startIndex = 0;
    private int lastIndex = startIndex;
    private int currentReadLenth = 0;
    /**
     * 解决\r后面跟\n的问题
     */
    private boolean skipLF = false;

    public LineBufferReader(MappedByteBuffer buffer) {
        this.mappedByteBuffer = buffer;
        this.mappedByteBuffer.position(0);
        this.lenth = mappedByteBuffer.limit();
    }

    private void nextRead() {
        if (readLenth == lenth) {
            return;
        }
        int len = Math.min(4096, lenth - readLenth);
        if (startIndex == 0) {
            mappedByteBuffer.get(buff, 0, len);
            readLenth += len;
            currentReadLenth = len;
            startIndex = 0;
            lastIndex = startIndex;
            return;
        }
        int newCap = (buff.length - lastIndex) + len;
        byte[] newBuff = new byte[newCap];
        mappedByteBuffer.get(newBuff, (buff.length - lastIndex), len);
        readLenth += len;
        if (lastIndex < buff.length) {
            System.arraycopy(buff, lastIndex, newBuff, 0, buff.length - lastIndex);
        }
        buff = newBuff;
        currentReadLenth = newBuff.length;
        // 计算新的指针
        startIndex -= lastIndex;
        lastIndex = 0;
    }

    public String readLine() {
        bufferLoop:
        for (; ; ) {
            if (startIndex >= currentReadLenth) {
                nextRead();
            }
            boolean eol = false;
            int i;
            byte b;
            charLoop:
            for (i = startIndex; i < currentReadLenth; i++) {
                b = buff[i];
                if ((b == '\n') || (b == '\r')) {
                    eol = true;
                    break charLoop;
                }
            }

            if (skipLF && startIndex == i) {
                startIndex = i + 1;
                lastIndex = startIndex;
                skipLF = false;
                continue bufferLoop;
            }

            startIndex = i;

            if (eol) {
                String str = new String(buff, lastIndex, (startIndex - 1) - lastIndex);
                startIndex++;
                lastIndex = startIndex;
                skipLF = true;
                return str;
            }

            // 收尾
            if (readLenth == lenth) {
                if (lastIndex != startIndex) {
                    return new String(buff, lastIndex, startIndex - lastIndex);
                } else {
                    return null;
                }
            }
        }
    }

}
