package com.wujiuye.miniexcel.excel.annotation;

import com.wujiuye.miniexcel.excel.util.properties.PropertiesAnnotation;

/**
 * 全局配置
 *
 * @author wujiuye 2020/05/21
 */
@PropertiesAnnotation(filePath = "miniexcel.properties", prefix = "miniexcel")
public class GlobalCfgProperties {

    /**
     * ignore_not_exist_annotation_field: 是否忽略未加注解的字段，默认为false
     */
    private Boolean ignore_not_exist_annotation_field;
    /**
     * ignore_transient_field: 是否忽略transient字段，默认false
     */
    private Boolean ignore_transient_field;
    /**
     * 全局日期配置
     */
    private DateFieldCfg date_global_cfg;

    public static class DateFieldCfg {
        /**
         * 日期格式
         */
        private String date_pattern;
        /**
         * 时区
         */
        private Integer time_zone;

        public String getDate_pattern() {
            return date_pattern;
        }

        public void setDate_pattern(String date_pattern) {
            this.date_pattern = date_pattern;
        }

        public Integer getTime_zone() {
            return time_zone;
        }

        public void setTime_zone(Integer time_zone) {
            this.time_zone = time_zone;
        }

        @Override
        public String toString() {
            return "DateFieldCfg{" +
                    "date_pattern='" + date_pattern + '\'' +
                    ", time_zone=" + time_zone +
                    '}';
        }

    }

    public Boolean getIgnore_not_exist_annotation_field() {
        return ignore_not_exist_annotation_field;
    }

    public void setIgnore_not_exist_annotation_field(Boolean ignore_not_exist_annotation_field) {
        this.ignore_not_exist_annotation_field = ignore_not_exist_annotation_field;
    }

    public DateFieldCfg getDate_global_cfg() {
        return date_global_cfg;
    }

    public void setDate_global_cfg(DateFieldCfg date_global_cfg) {
        this.date_global_cfg = date_global_cfg;
    }

    public Boolean getIgnore_transient_field() {
        return ignore_transient_field;
    }

    public void setIgnore_transient_field(Boolean ignore_transient_field) {
        this.ignore_transient_field = ignore_transient_field;
    }

    @Override
    public String toString() {
        return "GlobalCfgProperties{" +
                "ignore_not_exist_annotation_field=" + ignore_not_exist_annotation_field +
                ", ignore_transient_field=" + ignore_transient_field +
                ", date_global_cfg=" + date_global_cfg +
                '}';
    }

}
