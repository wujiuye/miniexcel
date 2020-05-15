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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 写监听器适配器
 * 当data为空时，只导出标题，而不是报错，支持导出模版文件
 *
 * @author wujiuye
 */
public abstract class ExcelWriteListenerAdapter<T> extends GeneralExcelWriteListener<T> {

    private Class<T> tClass;

    /**
     * 使用默认写监听器
     *
     * @param data     要导出的数据
     * @param pageSize 页大小
     */
    public ExcelWriteListenerAdapter(List<T> data, Integer pageSize) {
        super(data == null ? new ArrayList<>() : data, pageSize);
        Type superClass = this.getClass().getGenericSuperclass();
        if (superClass instanceof Class) {
            throw new IllegalArgumentException("Internal error: ExcelWriteListenerAdapter constructed without actual type information");
        } else {
            this.tClass = (Class<T>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }
    }

    @Override
    public Class<T> getDataObjectClass() {
        return this.tClass;
    }

    @Override
    public void onError(Exception e) {
        throw new RuntimeException(e.getLocalizedMessage(), e);
    }

}
