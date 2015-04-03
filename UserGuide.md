# Instructions #
  1. Download the library from [downloads page](http://code.google.com/p/dbassert/downloads/list).
  1. Put it under your classpath.
  1. Define your datasources using YAML in some file
```
postgres:
    url: jdbc:postgresql://127.0.0.1/test
    username: postgres
    password: postgres
    driver: org.postgresql.Driver

hsqlSource:
    url: jdbc:hsqldb:file:testSrc
    username: sa
    password:
    driver: org.hsqldb.jdbcDriver
```
  1. Intialize DbAssert using with the datasources file and start using it.

## The dependencies are: ##
  * [JYaml](https://sourceforge.net/projects/jyaml) - JYaml is a Java library for working with the Yaml file format.
All database settings and fixtures use that format.
  * [HSQLDB](http://sourceforge.net/project/showfiles.php?group_id=23316) - if you want to use _fake_ sources, like dbAssert.fake\_source("my\_fake\_source");
in this case in-memory database instance will be created.