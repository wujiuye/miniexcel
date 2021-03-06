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

import java.util.List;

/**
 * 默认写监听器的适配器
 * 后续版本会删除
 *
 * @author wujiuye
 */
@Deprecated
public class DefaultExcelWriteListenerAdapter extends GeneralExcelWriteListener {

    /**
     * 使用默认写监听器
     *
     * @param data     要导出的数据
     * @param pageSize 页大小
     */
    public DefaultExcelWriteListenerAdapter(List<?> data, Integer pageSize) {
        super(data, pageSize);
    }

    @Override
    public void onError(Exception e) {
        throw new RuntimeException(e.getLocalizedMessage(), e);
    }

}
