package com.wujiuye.miniexcel.excel.util.properties;

import com.wujiuye.miniexcel.excel.util.StringUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author wujiuye
 * @version 1.0 on 2019/4/24 {描述：}
 */
public class PropertiesUtils {

    /**
     * 获取配置文件内容
     *
     * @return
     */
    public static <T> T getPropertiesConfig(Class<T> configClass) throws Exception {
        ClassLoader loader = configClass.getClassLoader();
        T obj = configClass.newInstance();
        Properties properties = new Properties();
        //获取注解信息
        PropertiesAnnotation propertiesAnnotation = configClass.getAnnotation(PropertiesAnnotation.class);
        if (propertiesAnnotation == null) {
            throw new Exception("Properties read error. not found @PropertiesAnnotation annotation!!!");
        }
        if (StringUtils.isEmpty(propertiesAnnotation.filePath())) {
            throw new Exception("Properties read error. file path is null!!!");
        }
        String prefix = propertiesAnnotation.prefix();
        if (StringUtils.isEmpty(prefix)) {
            prefix = "";
        } else {
            prefix += ".";
        }
        try (InputStream in = loader.getResourceAsStream(propertiesAnnotation.filePath());
             InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            properties.load(reader);
            parsingPropertiesWithType(obj, prefix, properties);
        } catch (Exception ignored) {
        }
        return obj;
    }

    /**
     * 递归解析
     *
     * @param obj
     * @param prefix
     */
    private static void parsingPropertiesWithType(final Object obj, final String prefix, final Properties properties) throws Exception {
        Field[] fields = obj.getClass().getDeclaredFields();
        if (fields.length == 0) {
            return;
        }
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType() == Integer.class || field.getType() == int.class) {
                String value = properties.getProperty(prefix + field.getName());
                field.set(obj, Integer.valueOf(value));
            } else if (field.getType() == Long.class || field.getType() == long.class) {
                String value = properties.getProperty(prefix + field.getName());
                field.set(obj, Long.valueOf(value));
            } else if (field.getType() == Boolean.class || field.getType() == boolean.class) {
                String value = properties.getProperty(prefix + field.getName());
                field.set(obj, Boolean.valueOf(value));
            } else if (field.getType() == String.class) {
                String value = properties.getProperty(prefix + field.getName());
                field.set(obj, value);
            } else {
                Object fieldValue = field.getType().newInstance();
                field.set(obj, fieldValue);
                parsingPropertiesWithType(fieldValue, prefix + field.getName() + ".", properties);
            }
        }
    }

}
