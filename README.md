# miniexcel

* [使用文档](https://github.com/wujiuye/miniexcel/wiki)
* [版本更新历史](https://github.com/wujiuye/miniexcel/VERSION.MD)

## 简介
`miniexcel`主要目的是解决读写大数据量（上`w`条记录）时导致的`OOM`问题。

`miniexcel`正如其名，简单且小巧，源码也简单，只是加了点设计模式。操作`excel`无非就是读和写，所以`miniexcel`有了读取器和写入器的概念，同时为了更加通用，不管是读取器还是写入器，在开始读或写时，都必须要先创建一个监听器，这便是可玩性高的原因。

* 当读取数据时，监听器用于接收读取到的数据；
* 当写入数据时，监听器要传给写入器当前要写入的数据；

为什么这么设计？
* 如果自己实现读取监听器，那么可以灵活的仅读取自己需要的列，可以实现按顺序读取而不按注解读取；
* 如果自己实现写入监听器，那么非常好的支持分页查询，每查一页自动写一页，并且支持设置每个`sheet`多少行记录，超过自动切换`sheet`写入。

`miniexcel`在新版本还引入了泛型和注解的使用。`@ExcelCellTitle`注解是双向使用的，导出生效，导入也生效。
* 用于写入（导出）数据时，`@ExcelCellTitle`注解可给`bean`的字段取一个标题名，还有设置标题的排序，新版本添加了是否忽略该字段的配置。
默认情况下使用`bean`的字段名作为列标题，标题不排序。
* 用于读取（导入）数据时，`@ExcelCellTitle`注解用于表格列与`bean`的字段做映射；

`miniexcel`只是为了解决简单数据的导入导出而设计的，并不想搞得太复杂。所以`miniexcel`不支持合并单元格之类的操作。

## 在项目中使用miniexcel

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


