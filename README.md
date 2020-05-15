### miniexcel简介

`miniexcel`主要目的是解决读写大数据量（上`w`条记录）时导致的`OOM`问题。

其次，`miniexcel`基于工厂模式，自动根据文件后缀名识别创建不同格式的读取器，只要文件后缀名规范就不需要自己根据文件格式创建读取器，但这点显然不够严格。

`miniexcel`正如其名，简单且小巧，源码也简单，只是加了点设计模式。操作`excel`无非就是读和写，所以`miniexcel`有了读取器和写入器的概念，同时为了更加通用，不管是读取器还是写入器，在开始读或写时，都必须要先创建一个监听器，这便是可玩性高的原因。

当读取数据时，监听器用于接收读取到的数据；当写入数据时，监听器要传给写入器当前要写入的数据，如果自己实现写入监听器，那么非常好的支持分页查询，每查一页自动写一页，并且支持设置每个`sheet`多少行记录，超过自动切换`sheet`写入。

`miniexcel`还引入了泛型和注解的使用，但目前我只是提供了一个注解，在写入（导出）数据时，可使用`@ExcelCellTitle`注解给`bean`的字段取一个标题名，还有设置标题的排序，新版本添加了是否忽略该字段的配置。默认情况下使用`bean`的字段名作为列标题，标题不排序。

目前实现的功能很简单，同时`miniexcel`也只是为了解决简单数据的导入导出而设计的，并不想搞得太复杂。如果想实现复杂的需求，可通过扩展实现，这得益于`miniexcel`的可扩展性设计。

### 添加依赖

maven中使用：
```xml
<!-- https://mvnrepository.com/artifact/com.github.wujiuye/miniexcel -->
<dependency>
    <groupId>com.github.wujiuye</groupId>
    <artifactId>miniexcel</artifactId>
    <version>1.1.0-RELEASE</version>
</dependency>
```

gradle中使用：
```groovy
// https://mvnrepository.com/artifact/com.github.wujiuye/miniexcel
compile group: 'com.github.wujiuye', name: 'miniexcel', version: '1.1.0-RELEASE'
```

### 使用文档

[!https://github.com/wujiuye/miniexcel/wiki](https://github.com/wujiuye/miniexcel/wiki)

### 版本更新说明

#### 版本1.1.0-RELEASE

日期：`2019-10-10`\
版本号：`1.1.0-RELEASE`\
更新说明：
* 1、提供默认的写监听器，应付大部分小数据导出场景。
* 2、提供导出列指示器，实现导出列指示器可以在不使用`@ExcelCellTitle`注解的情况下，设置导出忽略的字段，设置字段对应导`excel`的列标题。

```java
/**
 * @author wujiuye
 * @version 1.0 on 2019/10/10 {描述：
 * 列指示器
 * }
 */
public interface ColumnsDesignator {

    /**
     * 是否忽略该列
     *
     * @param column data的数据类型的字段名
     * @return
     */
    boolean isIgnore(String column);

    /**
     * 是否需要重命名
     *
     * @param column data的数据类型的字段名
     * @return 不需要重命名则直接返回参数column，需要重命名则返回重命名后的列名（excel文件的标题名称）
     */
    String renameColumn(String column);

}
```

```java
public class TestMain{
    public void testWrite(){
        File target = writer.write(new DefaultExcelWriteListenerAdapter(dataList, 1000), new ColumnsDesignator() {

            @Override
            public boolean isIgnore(String column) {
                if (CollectionUtils.isEmpty(columns)) {
                    return false;
                }
                return !columns.contains(column);
            }

            @Override
            public String renameColumn(String column) {
                if (CollectionUtils.isEmpty(columns)
                        || CollectionUtils.isEmpty(columnNames)) {
                    return column;
                }
                int index = columns.indexOf(column);
                return columnNames.get(index);
            }
        });
    }
}
```

#### 版本1.1.1-RELEASE

日期：`2020-03-24`\
版本号：`1.1.1-RELEASE`
更新说明：
* 1、修复`bug`，去掉使用外部集合判断

#### 版本1.1.2-RELEASE

日期：`2020-03-25`\
版本号：`1.1.2-RELEASE`\
更新说明：
* 1、注解提供`ignore`配置
* 2、修复注解声明的列名与`ColumnsDesignator`冲突问题
* 3、调整代码结构
* 4、把提供默认的默认读监听器`DefaultExcelWriteListener`改为`DefaultExcelWriteListenerAdapter`，默认抛出异常。

#### 版本1.3.18-RELEASE

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

#### 版本1.3.39-RELEASE

日期：`2020-04-07`\
版本号：`1.3.39-RELEASE`\
更新说明：
* 修复使用输出流创建写入器时导出excel失败的bug，使用输出流时不传文件路径导致创建文件失败，加个判断；

#### 版本1.3.20-RELEASE

日期：`2020-04-19`\
版本号：`1.3.20-RELEASE`\
更新说明：
* 修复读取单元格将数字当日期处理问题，无论单元格是日期格式还是数字格式都当前数字处理，具体类型转换交由读取监听器处理；

#### 版本1.3.21-RELEASE

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

#### 版本1.4.01-RELEASE

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

