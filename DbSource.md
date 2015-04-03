# Introduction #
Everybody knows that good unit test is the test that can live on its own without any dependencies or many dependencies from other components or external datasources. Partially this can be done by mocking up external resources, but often you do really want to test your logic that retrieves something from a datasource. You want to make sure it works, don’t you? So that’s what **DbAssert** and **DbSource** are about. Yes, here is another quirky name - DbSource that allows you to put database to a known state.

Often your business logic and tests require some existing data in database, or requires your datasource being in consistent known state between test executions. It means if you test business logic that inserts customer record to database you want to be able to run it as multiple times as you want without having manually delete it after each execution. So you couldn’t end up with something like “PrimaryKeyVialationException” for the same record when you try to insert it second time. Here you go!

# Details #
What DbSource does is puts your datasource in a known state plus a little bit more.

Let’s see the initialization process:
```
// initalize DbAssert first
final DbAssert dbAssert = DbAssert.init("org/testfw/databases.yml");
// get DbSource instance so we can operate on it
DbSource myTestDb = dbAssert.source("testDatabase", getClass());
```
To clean a given table in a datasource use:
```
myTestDb.clean_table("customers");
```
You can pass as many table names to the _clean\_table_ method as you want to be cleaned up.

Another neat feature of DbSource is fixtures.
Let’s say your test relies on some data in database so you need to load this data first.
```
myTestDb.fixture("customers");
```
This assumes that you want to insert records from _customers.yml_ file to customers table.

How do we find _customers.yml_ which is YAML file format and looks like this:
```
customer_one:
    id: 1
    name: John Smith
customer_two:
    id: 2
    name: Joe Doe
```
To locate it we passed reference to the invoker class in _dbAssert.source(...);_ method. This implies that we have folder named fixtures on the same level as your test class which contains all your fixture files.
```
- MyTest.java
- fixtures/
	- customers.yml
```
If you don’t want to follow that convention then don’t pass class name to the DbSource init method and use full path to your fixture file.

myTestDb.fixture("org/testfw/customers.yml");

There is one more thing. :) There is a way to get values from fixtures:
```
//load data to customers table and return fixture
final Fixture customers = myTestDb.fixture("customers");

// get all the fields for customer_one in the fixture
final Fixture customer_one = (Fixture) customers.get("customer_one");

//check that value returned by business logic (customerDao) is the same that we have in database
assertEquals(customer_one.get("name"), customerDao.findById(customer_one.get("id")));
```
Using jUnit’s @Before and @BeforeClass annotations you can vary your cleaning strategies: before every test method so they can start on fresh instance or cleaning it once. That should give enough flexibility for your needs.