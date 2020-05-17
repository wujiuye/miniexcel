# 版本更新历史记录

## 版本1.1.0-RELEASE

日期：`2019-10-10`\
版本号：`1.1.0-RELEASE`\
更新说明：
* 1、提供默认的写监听器，应付大部分小数据导出场景。
* 2、提供导出列指示器。

```java
public interface ColumnsDesignator {

    /**
     * 是否忽略该列
     * 一般使用注解，除非有特殊需求
     *
     * @param column data的数据类型的字段名
     * @return
     */
    boolean isIgnore(String column);

    /**
     * 是否需要重命名
     * 一般使用注解，除非有特殊需求
     *
     * @param fieldName         data的数据类型的字段名
     * @param currentColumnName 当前使用的列名，也就是从注解上面获取的，如果注解没有配置，则等于字段名
     * @return 不需要重命名则直接返回参数currentColumnName，需要重命名则返回重命名后的列名（excel文件的标题名称）
     */
    String renameColumn(String fieldName, String currentColumnName);

}
```

## 版本1.1.1-RELEASE

日期：`2020-03-24`\
版本号：`1.1.1-RELEASE`
更新说明：
* 1、修复`bug`，去掉使用外部集合判断

## 版本1.1.2-RELEASE

日期：`2020-03-25`\
版本号：`1.1.2-RELEASE`\
更新说明：
* 1、注解提供`ignore`配置
* 2、修复注解声明的列名与`ColumnsDesignator`冲突问题
* 3、调整代码结构
* 4、把提供默认的默认读监听器`DefaultExcelWriteListener`改为`DefaultExcelWriteListenerAdapter`，默认抛出异常。

## 版本1.3.18-RELEASE

日期：`2020-03-31`\
版本号：`1.3.18-RELEASE`\
更新说明：
* 1、读取器和写入器支持根据输入流和输出流创建

创建读取器
```text
AbstractExcelReader reader = AbstractExcelReader.getReader(new FileInputStream(file), ExcelFileType.XLSX, true);
```

创建写入器
```text
AbstractExcelWriter writer = AbstractExcelWriter.getWriter(new FileOutputStream(file),ExcelFileType.XLSX);
```

* 2、导出与导入的数据类型Class，如Student，能够获取到父类的字段

* 3、提供注解读取监听器AnnotationExcelReaderListener

AnnotationExcelReaderListener使用泛型读取，无需再对导出的数据做类型转换

```java

    @Getter
    @Setter
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    public  class GoodsEntity extends Portion {
        
        @ExcelCellTitle(alias = "商品ID", cellNumber = 5)
        private Long goodsId;
       
        @ExcelCellTitle(alias = "商品货号", cellNumber = 6)
        private Long goodsCode;
    }
```

使用AnnotationExcelReaderListener读取监听器读取数据
```java
public class TestMain{
    public static void main(String[] args) throws FileNotFoundException {
        File file = new File("/Users/wjy/Downloads/文件.xlsx");
        AbstractExcelReader reader = AbstractExcelReader.getReader(new FileInputStream(file), ExcelFileType.XLSX, true);
        AnnotationExcelReaderListener<GoodsEntity> listener = new AnnotationExcelReaderListener<>(GoodsEntity.class);
        reader.read(listener);
        listener.getRecords().forEach(System.out::println);
    }
}
```

## 版本1.3.39-RELEASE

日期：`2020-04-07`\
版本号：`1.3.39-RELEASE`\
更新说明：
* 修复使用输出流创建写入器时导出excel失败的bug，使用输出流时不传文件路径导致创建文件失败，加个判断；

## 版本1.3.20-RELEASE

日期：`2020-04-19`\
版本号：`1.3.20-RELEASE`\
更新说明：
* 修复读取单元格将数字当日期处理问题，无论单元格是日期格式还是数字格式都当前数字处理，具体类型转换交由读取监听器处理；

## 版本1.3.21-RELEASE

日期：`2020-05-12`\
版本号：`1.3.21-RELEASE`\
更新说明：
* 导入导出支持日期类型（Date），支持指定导出和导入日期格式设置（默认："yyyy-MM-dd HH:mm:ss"），支持时区设置；

```java
public class SupporDateModel{

    @ExcelCellTitle(datePattern = "yyyy-MM-dd HH", timeZone = 8)
    private Date date;

}
```
> 注意给日期字段配置datePattern，否则导入可能因为格式问题，自动映射字段值异常。

注解是双向使用的，导入和导出都会生效
```java
public @interface ExcelCellTitle {

    /**
     * -1为不参与排序
     * 用于列的排序
     *
     * @return
     */
    int cellNumber() default -1;

    /**
     * 列名，为null时，取属性的字段名
     *
     * @return
     */
    String alias() default "";

    /**
     * 是否忽略这列（这个字段）
     *
     * @return
     */
    boolean ignore() default false;

    /**
     * 日期类型格式
     *
     * @return
     */
    String datePattern() default "yyyy-MM-dd HH:mm:ss";

    /**
     * 时区，默认东八区，0表示0时区，8表示东八区，以此类推
     *
     * @return
     */
    int timeZone() default 8;

}
```

## 版本1.4.01-RELEASE

日期：`2020-05-16`\
版本号：`1.4.01-RELEASE`\
更新说明：
* 支持CSV格式的导入导出（不依赖poi）；
* 支持导出数据为空，为空时只导出标题，目的时支持导出模版；
```java
public class ExportTest{

    public void test(){
        AbstractExcelWriter excelWriter = AbstractExcelWriter.createExcelWriter("/tmp/miniexcel-test", ExcelFileType.CSV);
        excelWriter.write(new ExcelWriteListenerAdapter<DateModel>(null, 1000) {});
    }

}
```