
```
dbAssert = DbAssert.init("my/package/Sources.yml");
```
  1. Set source name you’re going to use to check data in. This comes from your _Sources.yml_ file.
```
DbSource postgres = dbAssert.source("postgres");
```
  1. Prepare datasource if you need to.
```
//cleans up "customers" table
postgres.clean_table("customers");

//load data from customers.yml file to datasource
final Fixture customers = postgres.fixture("customers");
final Fixture customer_one  = (Fixture) customers.get("customer_one");
```
  1. Set condition to search record by. To do that you need to know the field name to search by and supposed value of the field stored by your business logic you’re testing. Let’s say that my code adds new customer to hypothetical “customers” table. I know that my “customers” table contains “id” field and new customer should be stored with id 123 in this field. So I need to look record in database by “id” field with value 123.
```
dbAssert.table("customers").where("id", 123); 
//For database it becomes SELECT … FROM customers WHERE id = 123;

//Any additional condition you may add like this:
dbAssert.table("customers").where("id = 123").and("last_name", "Coupland");
//In this case for database it becomes SELECT … WHERE id = 123 AND last_name = ‘Coupland’;
```
  1. You’re able to make assertions now.
```
dbAssert.assert_column("name", "Douglas");
//Where “name” is a field name to retrieve value from. “Douglas” is the value we expect in this database field. You can add as many assertions as you want.
```
  1. More assertions
```
// HashMap can be used to assert multiple columns at once
// where the map contains column-name/expected-value key-value pairs.
dbAssert.assert_columns(map);

//checks field _name_ contains non empty value
dbAssert.assert_not_empty(”name”);

// check if count of records returned more than one
dbAssert.assert_count_gt(”*”, 1);

// chek if number of unique departments returned by query less than 2
dbAssert.assert_count_lt(”department”, 2);

//check if number of returned records “count(*)..” equals 0
dbAssert.assert_count(0);

// returns value of field “user_name” as String
final String userName = dbAssert.column_value(”user_name”);
```