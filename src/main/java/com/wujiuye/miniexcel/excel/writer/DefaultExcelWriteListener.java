package com.wujiuye.miniexcel.excel.writer;

import java.util.List;

/**
 * @author wujiuye
 * @version 1.0 on 2019/8/18 {描述：
 * 提供默认的写监听器
 * 如果是查数据库导出的，不建议使用，而是自己实现接口，因为可以支持分页查询，这样耗内存更少
 * }
 */
public class DefaultExcelWriteListener<T> implements ExcelWriterListener<T> {

    private List<T> data;
    private int page;
    private int pageSize;

    /**
     * 使用默认写监听器
     *
     * @param data     要导出的数据
     * @param pageSize 页大小
     */
    public DefaultExcelWriteListener(List<T> data, Integer pageSize) {
        this.data = data;
        if (this.data == null) {
            throw new NullPointerException("data is null!!!");
        }
        if (pageSize == null || pageSize <= 0) {
            this.pageSize = 1000;
        } else {
            this.pageSize = pageSize;
        }
        this.page = 1;
    }

    /**
     * 获取数据的类型
     *
     * @return
     */
    @Override
    public Class getDataObjectClass() {
        if (data.size() == 0) {
            throw new NullPointerException("data size is 0!");
        }
        return data.get(0).getClass();
    }

    /**
     * 自动生成标题
     *
     * @return
     */
    @Override
    public boolean autoGenerateTitle() {
        return true;
    }

    /**
     * 获取下一步输出的真实记录数
     *
     * @param sn 第几个sheet
     * @return
     */
    @Override
    public int getNetOutputDataRealSize(int sn) {
        try {
            if (page * pageSize < data.size()) {
                return pageSize;
            } else {
                // 不够一页则返回剩余的记录数
                int size = data.size() - (pageSize * (page - 1));
                if (size <= 0) {
                    // 返回0代表结束
                    return 0;
                }
                return size;
            }
        } finally {
            page++;
        }
    }

    /**
     * 分页输出，很好的支持数据库查询分页导出
     *
     * @param sn         第几个sheet
     * @param limitStart 上次完成刷盘后的指针位置 (从0开始) （闭区间-包含）
     * @param limitEnd   limitStart + getOutputDataSizeWithSheetNumber返回的值 (开区间-不包含)
     * @return
     */
    @Override
    public List<T> getOutputDataWithSheetNumber(int sn, int limitStart, int limitEnd) {
        return data.subList(limitStart, limitEnd);
    }

}
