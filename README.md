`poi`提供的基于事件模式的读，在`eventusermodel`包下，相对来说实现比较复杂，但是它处理速度快，占用内存少，可以用来处理海量的`Excel`数据。而`SXSSFWorkbook`是基于流模式的写，在`xssf.streaming`包下，解决`OOM`问题。

### miniexcel简介

`miniexcel`主要目的是解决读写大数据量（上`w`条记录）时导致的`OOM`问题，解决办法就是基于事件模式读，使用`SXSSFWorkbook`写。

其次，`miniexcel`基于工厂模式，自动根据文件后缀名识别创建不同格式的读取器，只要文件后缀名规范就不需要自己根据文件格式创建读取器，但这点显然不够严格。

`miniexcel`正如其名，简单且小巧，源码也简单，只是加了点设计模式。操作`excel`无非就是读和写，所以`miniexcel`有了读取器和写入器的概念，同时为了更加通用，不管是读取器还是写入器，在开始读或写时，都必须要先创建一个监听器，这便是可玩性高的原因。

当读取数据时，监听器用于接收读取到的数据；当写入数据时，监听器要传给写入器当前要写入的数据，如果自己实现写入监听器，那么非常好的支持分页查询，每查一页自动写一页，并且支持设置每个`sheet`多少行记录，超过自动切换`sheet`写入。

`miniexcel`还引入了泛型和注解的使用，但目前我只是提供了一个注解，在写入（导出）数据时，可使用`@ExcelCellTitle`注解给`bean`的字段取一个标题名，还有设置标题的排序，新版本添加了是否忽略该字段的配置。默认情况下使用`bean`的字段名作为列标题，标题不排序。

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

### 读，将excel文件中的记录读取到内存中的List

使用`AbstractExcelReader.getReader`方法获取一个文件读取器，第一个参数是文件的绝对路径（包含后缀名），第二个参数是是否读取列标题。

```java
public class TestMain{
    /**
     * 解决记录超过1000内存oom问题
     *
     * @param filePath 文件路径
     * @param cellName 
     * @return
     */
    public static List<Object> readDataWithCellName(final String filePath, final String cellName) {
        final List<Object> list = Lists.newArrayList();
        AbstractExcelReader reader = AbstractExcelReader.getReader(filePath, true);
        DefaultExcelReaderListener listener = new DefaultExcelReaderListener(cellName);
        reader.read(listener);
        if (listener.getData() != null && listener.getData().size() > 0) {
            listener.getData().forEach(entry -> list.add(entry.get(cellName)));
        }
        return list;
    }
}
```

`DefaultExcelReaderListener`是我为满足项目需求实现的一个默认监听器。支持只读某些列，只需要在`new`时传入列名即可。`DefaultExcelReaderListener`的源码如下。

```java
/**
 * @author wujiuye
 * @version 1.0 on 2019/4/13 {描述：}
 */
public class DefaultExcelReaderListener implements ExcelReaderListener {

    //sheetName -> {cellIndex->cellName}
    private Map<String, Map<Integer, String>> cellTitleMap = new HashMap<>();
    private String currentSheetName;
    //cellName -> value
    private List<Map<String, Object>> data = new ArrayList<>();
    private int currentRow = -1;
    private Map<String, Object> currentRowData = new HashMap<>();

    private boolean ydCell = false;//是否约定只获取某些列
    private List<String> readCells;

    public DefaultExcelReaderListener() {

    }

    // 是否只获取某些列的值
    public DefaultExcelReaderListener(String... cellName) {
        if (cellName != null && cellName.length > 0) {
            ydCell = true;
            readCells = Arrays.stream(cellName).collect(Collectors.toList());
        }
    }

    @Override
    public void onReadSheetStart(String sheetName) {
        this.currentSheetName = sheetName;
        if (!cellTitleMap.containsKey(this.currentSheetName)) {
            cellTitleMap.put(this.currentSheetName, new HashMap<>());
        }
    }

    @Override
    public void onReadSheetTitle(int cellNumber, String cellTitle) {
        this.cellTitleMap.get(this.currentSheetName).put(cellNumber, cellTitle);
    }

    @Override
    public void onReadRow(Object data, int rowNumber, int cellNumber) {
        String cellName = cellTitleMap.get(currentSheetName).get(cellNumber);
        if (ydCell && !readCells.contains(cellName)) {
            return;
        }
        if (this.currentRow != rowNumber) {
            this.data.add(new HashMap<>(currentRowData));
            currentRowData.clear();
            this.currentRow = rowNumber;
        }
        this.currentRowData.put(cellName, data);
    }

    public List<Map<String, Object>> getData() {
        return this.data;
    }
}
```

#### 在web项目中使用

1、在方法加上接收客户端上传的`excel`文件

```java
public class XxxController{

    public void importBy(@RequestParam("file") CommonsMultipartFile excelFile){
    }

}
```

2、将文件临时存储，读取完成后删除

```java
public class XxxController{

    public void importBy(){
        File tmpExcelFile = null;
        try {
            String suffix = excelFile.getOriginalFilename().substring(excelFile.getOriginalFilename().lastIndexOf("."));
            // 导数据上传的临时文件
            tmpExcelFile = File.createTempFile("export_data_upload_tmp" + System.currentTimeMillis() + CodeUtils.md5(excelFile.getOriginalFilename()), suffix);
            excelFile.transferTo(tmpExcelFile);
            //
            // 在此处读取文件内容
            //
        }finally{
            //删除临时文件
            if (tmpExcelFile != null) {
                try {
                   tmpExcelFile.delete();
                } catch (Exception e) {
                }
            }
        }
    }

}
```

### 写，将List数据写入excel文件

1、首先创建一个`bean`，不支持`Map`类型哦。目前只支持简单类，不支持复杂类。

A：什么是复杂类？
Q：就是`Object`中有非`java`基本数据类型的字段。目前支持的非基本数据类型（包括其`Integer`、`Long`等）只有`Date`。

```java
/**
 * @author wujiuye
 * @version 1.0 on 2019/5/6 {描述：}
 */
@NoArgsConstructor
@Data
public class ExcelTestBean {
    @ExcelCellTitle(cellNumber = 1, alias = "这是id")
    private Long testId;
    @ExcelCellTitle(cellNumber = 2, alias = "这是名称")
    private String testName;
    @ExcelCellTitle(cellNumber = 3, alias = "这是日期")
    private Date createDate;
}
```

2、将List数据写入excel文件

`Writer`的几个方法

a、`setSheetSize(1_000)`:设置每个`sheet`的大小为`1000`行，当写满`1000`行时，自动创建一个新的`sheet`；
b、`setSheetNameFromat("export_{sn}")`即给`sheet`设置命名规则，其中`{sn}`是必须的，会被替换为序号。

`ExcelWriterListener`的几个方法

a：`getDataObjectClass`获取记录的真实类型
b：`autoGenerateTitle`是否需要在创建`sheet`时自动生成标题
c：`getNetOutputDataRealSize`与`getOutputDataWithSheetNumber`很重要，也是需要配合使用的。前者是表示接下来要写入的数据的大小，后者则需要返回接下来实际需要写入的数据。如果`getNetOutputDataRealSize`返回`0`则结束。`limitStart`与`limitEnd`是借鉴了分页查询的思想，实现分页写入，这样可以边查询边写入，避免一次将所有数据查询出来占用大量的内存。

```java
 public class TestMain{
    /**
     * 测试导出
     */
    @Test
    public void testWrite() {
        final List<ExcelTestBean> records = new ArrayList<>();
        ExcelTestBean item1 = new ExcelTestBean();
        item1.setXXX(xxx);
        // ...
        for (int i = 0; i < 3_000; i++) {
            records.add(item1);
        }

        SXSSFWriter writer = (SXSSFWriter) AbstractExcelWriter.createExcelWriter("/Users/wjy/Desktop/test_export_excel_1", AbstractExcelWriter.ExportFormatType.XLS)
                .setSheetSize(1_000)
                .setSheetNameFromat("export_{sn}");
        writer.write(new ExcelWriterListener() {

            private int count = 0;

            @Override
            public Class<?> getDataObjectClass() {
                return ExcelTestBean.class;
            }

            @Override
            public boolean autoGenerateTitle() {
                return true;
            }

            @Override
            public int getNetOutputDataRealSize(int sn) {
                if (count >= records.size()) {
                    return 0;
                } else {
                    count += 100;
                    return 100;
                }
            }

            @Override
            public List getOutputDataWithSheetNumber(int sn, int limitStart, int limitEnd) {
                return records.subList(limitStart, limitEnd);
            }
        });
    }
}
```

#### 在web项目中使用

```java
public class XxxController{

    public void export(){
        File file = writer.write(new ExcelWriterListener() {});
    }

}
```

在基类`Controller`添加一个响应文件的方法

```java
public class BaseController{
    /**
         * 响应文件给客户端请求
         *
         * @param response
         * @param excelFile
         * @param afterDeleteFile 响应完成后是否删除文件
         * @throws IOException
         */
        protected void responseExcelFile(HttpServletResponse response, File excelFile, boolean afterDeleteFile) throws IOException {
            response.setContentType("application/octet-stream; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + excelFile.getPath());
            byte[] buf = new byte[2048];
            try (FileInputStream fin = new FileInputStream(excelFile)) {
                int len;
                while ((len = fin.read(buf, 0, buf.length)) > 0) {
                    response.getOutputStream().write(buf, 0, len);
                }
            } finally {
                if (afterDeleteFile) {
                    excelFile.delete();
                }
            }
        }
}
```

目前实现的功能很简单，同时`miniexcel`也只是为了解决简单数据的导入导出而设计的，并不想搞得太复杂。如果想实现复杂的需求，可通过扩展实现，这得益于`miniexcel`的可扩展性设计。

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
 /**
     * 换货
     */
    @Getter
    @Setter
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    public  class PortionAndGoodsChange extends Portion {
        /**
         * 换出商品ID
         */
        @ExcelCellTitle(alias = "换出商品ID", cellNumber = 5)
        private Long exchangeLeId;
        /**
         * 换出商品货号
         */
        @ExcelCellTitle(alias = "换出商品子货号", cellNumber = 6)
        private Long exchangeLeCode;
    }
```

使用AnnotationExcelReaderListener读取监听器读取数据
```java
public class TestMain{
    public static void main(String[] args) throws FileNotFoundException {
        File file = new File("/Users/wjy/Downloads/导入文件.xlsx");
        AbstractExcelReader reader = AbstractExcelReader.getReader(new FileInputStream(file), ExcelFileType.XLSX, true);
        AnnotationExcelReaderListener<PortionAndGoodsChange> listener
                = new AnnotationExcelReaderListener<>(ExchangeExcelForm.PortionAndGoodsChange.class);
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