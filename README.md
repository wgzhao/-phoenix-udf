# UDF for Apache Phoenix

创建 Apache Phoenix 的 UDF 有几个先决条件，在[官方文档有说明](http://phoenix.apache.org/udf.html#How_to_write_custom_UDF) 
要特别注意 `Configuration` 这一节的内容

主要就是：

1. 必须配置 `hbase.dynamic.jars.dir`
2. jar 包必须上传到 `hbase.dynamic.jars.dir` 定义的路径，其他无效
3. 函数修改后，必须退出链接，然后执行 `drop function`, `create function` 
4. 测试函数时 `select func()` 这种方式会给出 `function undefined` 的错误，必须使用 `select func() from table` 这种方式，这点官方文档没说，浪费大把时间。
