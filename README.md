### 添加依赖

maven
```xml
<!-- https://mvnrepository.com/artifact/com.github.wujiuye/miniexcel -->
<dependency>
    <groupId>com.github.wujiuye</groupId>
    <artifactId>miniexcel</artifactId>
    <version>1.1.0-RELEASE</version>
</dependency>
```

gradle
```groovy
// https://mvnrepository.com/artifact/com.github.wujiuye/miniexcel
compile group: 'com.github.wujiuye', name: 'miniexcel', version: '1.1.0-RELEASE'
```

### miniexcel简介

miniexcel主要目的是解决读写大数据量（上w条记录）时导致的OOM问题，解决办法就是基于事件模式读，使用SXSSFWorkbook写。
poi提供的基于事件模式的读，在eventusermodel包下，相对来说实现比较复杂，但是它处理速度快，占用内存少，可以用来处理海量的Excel数据。
而SXSSFWorkbook是基于流模式的写，在xssf.streaming包下。

其次，miniexcel基于工厂模式，自动根据文件后缀名识别创建针对2003、2007不同格式的读取器，只要文件后缀名规范就不需要自己根据文件格式创建读取器。

miniexcel正如其名，很简单，源码也简单，只是加了点设计模式。操作excel无非就是读和写，所以miniexcel有了读取器和写入器的概念，
同时为了更加通用，不管是读取器还是写入器，在开始读或写时，都必须要先创建一个监听器，作用不尽相同。
当读取数据时，监听器用于接收读取到的数据；当写入数据时，监听器要传给写入器当前要写入的数据。

miniexcel还引入了泛型和注解的使用，但目前我只是提供了一个注解，在写入（导出）数据时，可使用@ExcelCellTitle注解给bean的字段取一个标题名，
还有标题的排序，新版本添加了是否忽略该字段的配置。默认情况下使用bean的字段名作为标题，标题不排序。

### 读，将excel文件中的记录读取到内存中List

使用AbstractExcelReader.getReader方法获取一个文件读取器，第一个参数是文件的绝对路径（包含后缀名），第二个参数是是否读取列标题。
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

DefaultExcelReaderListener是我为满足项目需求实现的一个默认监听器。
支持只读某一列，只需要在new时传入列名即可。
DefaultExcelReaderListener的源码如下。

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

    //是否只获取某些列的值
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

1、在方法加上接收客户端上传的excel文件

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
            //导数据上传的临时文件
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

1、首先创建一个bean，不支持Map类型哦。目前只支持简单类，不支持复杂类。什么是复杂类，就是Object中有非java基本数据类型的字段。
目前支持的非基本数据类型（包括其Integer、Long等）只有Date。

```java
/**
 * @author wujiuye
 * @version 1.0 on 2019/5/6 {描述：}
 */
@NoArgsConstructor
@Data
public class ExcelTestBean {
    @ExcelCellTitle(cellNumber = 1, alias = "test_id")
    private Long testId;
    @ExcelCellTitle(cellNumber = 2, alias = "test_name")
    private String testName;
    @ExcelCellTitle(cellNumber = 3, alias = "test_date")
    private Date createDate;
}
```

2、将List数据写入excel文件

Writer的几个方法\
a、.setSheetSize(1_000) 设置每个sheet的大小为1000行，当写满1000行时，自动创建一个新的sheet；\
b、.setSheetNameFromat("export_{sn}")即给sheet设置命名规则，其中{sn}是必须的，会被替换为序号。

ExcelWriterListener的几个方法\
a、getDataObjectClass获取记录的真实类型\
b、autoGenerateTitle是否需要在创建sheet时自动生成标题\
c、getNetOutputDataRealSize与getOutputDataWithSheetNumber很重要，也是需要配合使用的，前者是表示接下来要写入的数据的大小，后者则需要返回接下来实际需要写入的数据。如果getNetOutputDataRealSize返回0则结束。limitStart与limitEnd是借鉴了分页查询的思想，实现分页写入，这样可以边查询边写入，避免一次将所有数据查询出来占用大量的内存。

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

在基类Controller添加一个响应文件的方法
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

目前实现的功能很简单，同时miniexcel也只是为了解决简单数据的导入导出而设计的，并不想搞得太复杂。

### 意见反馈
当前miniexcel被用在作者所在公司的项目中，如果遇到bug我会及时修复。
也欢迎大家使用，欢迎大家参与到miniexcel开源项目来，发现问题给作者提个醒，或者拉一个分支修复，感谢！！！


### 版本更新说明

#### 版本1.1.0-RELEASE

日期：2019-10-10\
版本号：1.1.0-RELEASE\
更新说明：
* 1、提供默认的写监听器，应付大部分小数据导出场景。
* 2、提供导出列指示器，实现导出列指示器可以在不使用@ExcelCellTitle注解的情况下，设置导出忽略的字段，设置字段对应导excel的列标题。

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

日期：2020-03-24\
版本号：1.1.1-RELEASE\
更新说明：
* 1、修复bug，去掉使用外部集合判断

#### 版本1.1.2-RELEASE

日期：2020-03-25\
版本号：1.1.2-RELEASE\
更新说明：
* 1、注解提供ignore配置
* 2、修复注解声明的列名与ColumnsDesignator冲突问题
* 3、调整代码结构
* 4、把提供默认的默认读监听器DefaultExcelWriteListener改为DefaultExcelWriteListenerAdapter，抛出异常