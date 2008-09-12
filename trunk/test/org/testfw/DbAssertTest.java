/**
 * @(#) DbAssertTest.java;
 * <p/>
 * Created on Mar 8, 2008
 * AUTHOR    ** Danil Glinenko
 * EMAIL     ** dglinenko@gmail.com
 * <p/>
 *
 * Copyright (C) 2008 Danil Glinenko. All rights reserved.
 */

package org.testfw;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class DbAssertTest
{
    private static DbAssert dbAssert;
    private static DbAssert.TestBackdoor testDoor;
    private static final String SOURCE_FILE = "org/testfw/databases.yml";
    private static DbSource testSource;
    private static DbSource testOracle;

    @BeforeClass
    public static void oneTimeSetUp()
    {
        dbAssert = DbAssert.init(SOURCE_FILE);
        testDoor = dbAssert.new TestBackdoor();

        //init db sources
        testSource = dbAssert.source("testSource", "org/testfw/TestSourceSchemaFile.sql", DbAssertTest.class);
        testOracle = dbAssert.source("testOracle", DbAssertTest.class);
    }

    @Before
    public void setUp()
    {
        testSource.clean_table("events");
        testOracle.clean_table("customers");
    }

    @Test
    public void testAssertion() throws Exception
    {
        final Fixture eventsFixture = testSource.fixture("events");
        final Fixture event_one = (Fixture) eventsFixture.get("event_one");
        final Fixture event_two = (Fixture) eventsFixture.get("event_two");
        final Fixture event_three = (Fixture) eventsFixture.get("event_three");

        dbAssert.source("testSource");
        // condition id =1
        dbAssert.condition("id", String.valueOf(event_one.get("id")));
        dbAssert.assert_column("events.name", event_one.get("name"));
        dbAssert.assert_column("events.login_name", event_one.get("login_name"));

        // condition id = 2
        dbAssert.condition("id", String.valueOf(event_two.get("id")));
        dbAssert.assert_column("events.name", event_two.get("name"));

        dbAssert.condition("name", (String) event_two.get("name"));
        dbAssert.addCondition("id", (Integer) event_two.get("id"));
        dbAssert.assert_column("events.id", event_two.get("id"));
        dbAssert.assert_column("events.login_name", event_two.get("login_name"));

        // condtion name = my event3 from event_three fixture
        dbAssert.condition("name", (String) event_three.get("name"));
        dbAssert.assert_column("events.id", event_three.get("id"));

        //load fixture to testOracle ds
        final Fixture customerFixtures = testOracle.fixture("customers");
        final Fixture customer_one = (Fixture) customerFixtures.get("customer_one");
        dbAssert.source("testOracle");
        dbAssert.table("customers");
        //condition id =1
        dbAssert.condition("id", (Integer) customer_one.get("id"));
        dbAssert.assert_column("name", customer_one.get("name"));
        dbAssert.assert_not_empty("name");
        assertEquals(customer_one.get("name"), dbAssert.column_value("name"));
    }

    @Test
    public void testDataTypes()
    {
        final Fixture customerFixtures = testOracle.fixture("customers");
        final Fixture customer_one = (Fixture) customerFixtures.get("customer_one");
        dbAssert.source("testOracle");
        dbAssert.table("customers");
        dbAssert.condition("id", (Integer) customer_one.get("id"));
        dbAssert.assert_column("name", customer_one.get("name"));
    }

    @Test
    public void testCount()
    {
        //load data from fixture
        testOracle.fixture("customers");
        dbAssert.source("testOracle");
        dbAssert.table("customers");
        dbAssert.assert_count_gt("*", 1);
        dbAssert.assert_count_lt("*", 10);
        dbAssert.condition("id", 1);
        dbAssert.assert_count_lt("*", 2);
    }


    @Test
    public void testMapAssertion()
    {
        final Fixture eventsFixture = testSource.fixture("events");
        final Fixture event_one = (Fixture) eventsFixture.get("event_one");
        dbAssert.source("testSource");
        dbAssert.table("events");
        dbAssert.condition("id", (Integer) event_one.get("id"));
        final Map<String, String> fieldValue = new HashMap<String, String>();
        fieldValue.put("name", (String) event_one.get("name"));
        fieldValue.put("login_name", (String) event_one.get("login_name"));
        dbAssert.assert_columns(fieldValue);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidMapAssertion()
    {
        //there is no table definition using table() method neither in column names.
        dbAssert.source("testSource");
        dbAssert.condition("id", "1");
        final Map<String, String> fieldValueMap = new HashMap<String, String>();
        fieldValueMap.put("name", "my event");
        fieldValueMap.put("login_name", "dglinenko");
        dbAssert.assert_columns(fieldValueMap);
    }


    @Test(expected = RuntimeException.class)
    public void testMuotipleResultsReturned()
    {
        testSource.fixture("events");
        dbAssert.source("testSource");
        dbAssert.condition("name", "some");
        dbAssert.assert_column("events.id", "5");
    }

    @Test(expected = RuntimeException.class)
    public void testForEmptySource()
    {
        dbAssert.assert_column("name", "danil");
    }

    @Test(expected = RuntimeException.class)
    public void testForEmptyCondition()
    {
        dbAssert.source("testSource");
        dbAssert.assert_column("name", "danil");
    }

    @Test(expected = RuntimeException.class)
    public void testForEmptyTable()
    {
        dbAssert.source("testSource");
        dbAssert.assert_column("justcolumnwithouttabledefinition", "danil");
    }

    @Test(expected = RuntimeException.class)
    public void testForEmptyConditionsMap()
    {
        final DbSource testSource = dbAssert.source("testSource");
        testSource.fixture("events");
        dbAssert.condition("id", 2);
        dbAssert.assert_column("events.id", "2");
        dbAssert.assert_column("events.login_name", null);

        //new table set but no condition set up
        //should throw exception.
        dbAssert.table("events");
        dbAssert.assert_column("login_name", "");
    }


    @Test
    public void testParseTableFieldValues()
    {
        assertEquals("fieldName", testDoor.$fieldName("tableName.fieldName"));
        assertEquals("tableName", testDoor.$tableName("tableName.fieldName"));

    }

    @Test
    public void testMakeConditions()
    {
        final Map<String, Object> conditions = new HashMap<String, Object>();

        conditions.put("field1", "value1");
        assertEquals("field1 = 'value1'", testDoor.$makeConditionClause(conditions));

        conditions.put("field2", "value2");
        assertEquals("field2 = 'value2' AND field1 = 'value1'", testDoor.$makeConditionClause(conditions));

        conditions.put("field3", 123);
        assertEquals("field3 = 123 AND field2 = 'value2' AND field1 = 'value1'", testDoor.$makeConditionClause(conditions));
    }

    @Test
    public void testInitializationInDbAssert()
    {
        final DbAssert dbAssert = DbAssert.init("org/testfw/databases.yml");
        final DbSource fakeSource = dbAssert.source("hsqlSource", "org/testfw/HSqlSourceSchema.sql", getClass());
        fakeSource.clean_table("customers");
        final Fixture customerFixtures = fakeSource.fixture("customers");
        final Fixture customer_one  = (Fixture) customerFixtures.get("customer_one");
        dbAssert.table("customers");
        dbAssert.condition("id", (Integer) customer_one.get("id"));
        dbAssert.assert_column("name", customer_one.get("name"));
    }

    @Test
    public void testInitializationInDbAssert4()
    {
        final DbAssert dbAssert = DbAssert.init("org/testfw/databases.yml");
        final DbSource fakeSource = dbAssert.source("hsqlSource", "org/testfw/HSqlSourceSchema.sql");
        fakeSource.clean_table("customers");
        final Fixture customerFixtures = fakeSource.fixture("org/testfw/fixtures/customers.yml");
        final Fixture customer_one  = (Fixture) customerFixtures.get("customer_one");
        dbAssert.table("customers");
        dbAssert.condition("id", (Integer) customer_one.get("id"));
        dbAssert.assert_column("name", customer_one.get("name"));
    }

    @Test
    public void testInitializationInDbAssert2()
    {
        final DbAssert dbAssert = DbAssert.init("org/testfw/databases.yml");
        final DbSource myTestSource = dbAssert.source("testSource", getClass());
        myTestSource.clean_table("customers");
        final Fixture customerFixtures = myTestSource.fixture("customers");
        final Fixture customer_one  = (Fixture) customerFixtures.get("customer_one");
        dbAssert.table("customers");
        dbAssert.condition("id", (Integer) customer_one.get("id"));
        dbAssert.assert_column("name", customer_one.get("name"));
    }

    @Test
    public void testInitializationInDbAssert3()
    {
        final DbAssert dbAssert = DbAssert.init("org/testfw/databases.yml");
        final DbSource myTestSource = dbAssert.source("testSource");
        myTestSource.clean_table("customers");
        final Fixture customerFixtures = myTestSource.fixture("org/testfw/fixtures/customers.yml");
        final Fixture customer_one  = (Fixture) customerFixtures.get("customer_one");
        dbAssert.table("customers");
        dbAssert.condition("id", (Integer) customer_one.get("id"));
        dbAssert.assert_column("name", customer_one.get("name"));
    }
}
