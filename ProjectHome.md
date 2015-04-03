## Database testing easily in jUnit ##
DbAssert is a set of assertion methods for jUnit tests to verify your database data.
It uses non-intrusive way for writing unit tests, you don't have to extend any specific class to be able to query and test your database. Initialize DbAssert and start making assertions against your database right away.
### Assert data ###
```
//Initialize DbAssert by passing YAML file defining the datasources.
dbAssert = DbAssert.init("org/testfw/Sources.yml");

//Set condition to search record by. 
dbAssert.table("authors").where("id", 123).and("last_name", "Coupland") ; 

// assert customers _name_ column equals Douglas value in database.
dbAssert.assert_column("name", "Douglas");

//checks field name contains non-empty value
dbAssert.assert_not_empty("bio");

// check if count of records returned more than one
dbAssert.assert_count_gt("*", 1);

// chek if number of unique departments returned by query less than 2
dbAssert.assert_count_lt("department", 2);

//check if number of returned records "count(*).." equals 0
dbAssert.assert_count(0);

// returns value of field "user_name" as String
final String userName = (String) dbAssert.column_value(”user_name”);
```

### Prepare datasource ###
Often your tests require a datasource to be in a know state. You can do it as well with DbSource. Use Fixtures to load predefined set of data to the datasource.
```
//Prepare datasource if you need to.
//cleans up "customers" table
postgres.clean_table("customers");

//load data from customers.yml file to datasource
final Fixture customers = postgres.fixture("customers");
final Fixture customer_one  = (Fixture) customers.get("customer_one");

```

### Create (fake) datasource ###
You don't have to have database instance installed to start using DbAssert. You can _fake_ sources with built-in HSQLDB support.
```
dbAssert.source("myTestSource", "org/testfw/testSourceSchema.sql");
```

DbAssert is very flexible and simple.

And there is much more. See [Wiki](http://code.google.com/p/dbassert/w/list) for the details.