package com.wujiuye.miniexcel.excel.writer;

import java.util.List;

/**
 * 默认写监听器的适配器
 *
 * @author wujiuye
 */
public class DefaultExcelWriteListenerAdapter extends DefaultExcelWriteListener {

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
