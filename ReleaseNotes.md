## Version 0.9.3 ##
1. Bug fixes:
  * [Issue5](https://code.google.com/p/dbassert/issues/detail?id=5)
2. jYaml is now part of the package. No external jyaml-1.3.jar library required.

## Version 0.9.2 ##
1. Bug fixes:
  * [Issue2](https://code.google.com/p/dbassert/issues/detail?id=2)
2. Removed org.testfw package.


## Version 0.9.1 ##
1. API changes. Introduced new package namespace "net/codemate". Please use this one, in next version I'm going to get rid of "org/testfw" completely, however it's still there.

2. Re-factored DbAssert. No "condition" and "addCondition" methods anymore. There is a better way to do so:
```
dbAssert.table("table_name").where("id",123).and("name ='Danil'");
```

## Version 0.8.7 ##
1. Got rid of HsqlSource.java

2. Any of sources can accept db schema file now.
```
testSource = dbAssert.source("testSource", "org/testfw/TestSourceSchemaFile.sql", DbAssertTest.class);
```
this loads the schema file to the datasource. Previously it was possible only for fake\_source (HSQLDB).

3. Removed DbAssert::fake\_source() method. DbAssert::source() does the same now. For DbAssert there is no difference which datasource you want to use. To use HSQLDB (in-memory, file) specify source like:
```
hsqlSource:
    url: jdbc:hsqldb:file:testSrc
    username: sa
    password:
    driver: org.hsqldb.jdbcDriver
```
4. Added conditional table cleaning method.
```
dbSrc.clean_table_where("test_table", "name = \'Danil\'");
```

## Version 0.8.6 ##