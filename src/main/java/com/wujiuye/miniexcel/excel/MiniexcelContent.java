package com.wujiuye.miniexcel.excel;

import com.wujiuye.miniexcel.excel.annotation.GlobalCfgProperties;
import com.wujiuye.miniexcel.excel.util.properties.PropertiesUtils;

import java.util.logging.Logger;

/**
 * 上下文
 *
 * @author wujiuye 2020/05/21
 */
public class MiniexcelContent {

    /**
     * 全局配置
     */
    private static GlobalCfgProperties properties = null;

    static {
        try {
            properties = PropertiesUtils.getPropertiesConfig(GlobalCfgProperties.class);
            Logger.getLogger("miniexcel").info("[miniexcel] " + properties.toString());
        } catch (Exception ignored) {
            Logger.getLogger("miniexcel").info("[miniexcel] not found global config properties...");
        }
    }

    public static GlobalCfgProperties getGlobalCfgProperties() {
        return properties;
    }

}
