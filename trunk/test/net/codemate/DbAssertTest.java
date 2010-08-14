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

package net.codemate;

import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class DbAssertTest
{
    private static DbAssertInterface dbAssert;
    private static final String SOURCE_FILE = "net/codemate/databases.yml";
    private static DbSource testSource;

    @BeforeClass
    public static void oneTimeSetUp()
    {
        dbAssert = DbAssert.init(SOURCE_FILE);
        //init db sources
        testSource = dbAssert.source("testSource", DbAssertTest.class);
        testSource.loadSchemaFile("net/codemate/TestSourceSchemaFile.sql");
    }

    @Before
    public void setUp()
    {
        testSource.clean_table("events");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        testSource.close();
    }



    @Test
    public void testOne() {
        final Fixture eventsFixture = testSource.fixture("events");
        final Fixture event_one = (Fixture) eventsFixture.get("event_one");
        // condition id =1
        dbAssert.table("events").where("id", String.valueOf(event_one.get("id")));
        dbAssert.assert_column("name", event_one.get("name"));
        dbAssert.assert_column("login_name", event_one.get("login_name"));
    }

    @Test
    public void testOnePointFive() {
        final Fixture eventsFixture = testSource.fixture("events");
        final Fixture event_one = (Fixture) eventsFixture.get("event_one");
        // condition id =1
        dbAssert.table("events").where("id=1");
        dbAssert.assert_column("name", event_one.get("name"));
        dbAssert.assert_column("login_name", event_one.get("login_name"));
    }

    @Test
    public void testOnePointSix() {
        final Fixture eventsFixture = testSource.fixture("events");
        final Fixture event_one = (Fixture) eventsFixture.get("event_one");
        // condition id =1
        dbAssert.table("events").where("name='my event'");
        dbAssert.assert_column("id", event_one.get("id"));
        dbAssert.assert_column("login_name", event_one.get("login_name"));

        dbAssert.table("events").where("name", event_one.get("name"));
        dbAssert.assert_column("id", event_one.get("id"));
        dbAssert.assert_column("login_name", event_one.get("login_name"));

    }

    @Test
    public void testTwo() {
        final Fixture eventsFixture = testSource.fixture("events");
        final Fixture event_one = (Fixture) eventsFixture.get("event_one");
        // condition id =1
        dbAssert.table().where("id", String.valueOf(event_one.get("id")));
        dbAssert.assert_column("events.name", event_one.get("name"));
        dbAssert.assert_column("events.login_name", event_one.get("login_name"));
    }

    @Test
    public void testThree() {
        final Fixture eventsFixture = testSource.fixture("events");
        final Fixture event_two = (Fixture) eventsFixture.get("event_two");

        // condition id = 2
        dbAssert.table().where("id", String.valueOf(event_two.get("id")));
        dbAssert.assert_column("events.name", event_two.get("name"));

        dbAssert.table().where("name", event_two.get("name")).and("id", event_two.get("id"));
        dbAssert.assert_column("events.id", event_two.get("id"));
        dbAssert.assert_column("events.login_name", event_two.get("login_name"));
        dbAssert.assert_column("events.login_name", null);

    }
    @Test
    public void testFour(){
        final Fixture eventsFixture = testSource.fixture("events");
        final Fixture event_three = (Fixture) eventsFixture.get("event_three");
        dbAssert.table("events").where("id = 3");
        dbAssert.assert_column("login_name", "");
        dbAssert.assert_not_empty("name");
        assertEquals(event_three.get("name"), dbAssert.column_value("name"));
    }


    @Test
    public void testFive() {
        testSource.fixture("events");
        dbAssert.table("events").where("id = 1").and("name = 'my event'");
        dbAssert.assert_column("login_name", "dglinenko");
    }

    public void testSix()
    {
        testSource.fixture("events");
        dbAssert.table().where("id", 2);
        dbAssert.assert_column("events.id", "2");
        dbAssert.assert_column("events.login_name", null);

    }

    @Test
    public void testCount()
    {
        testSource.fixture("events");
        dbAssert.table("events");
        dbAssert.assert_count_gt("*", 1);
        dbAssert.assert_count_lt("*", 30);
        dbAssert.assert_count(20);
        dbAssert.table("events").where("id =1");
        dbAssert.assert_count_lt("*", 2);
        dbAssert.assert_count(1);
    }


    @Test
    public void testMapAssertion()
    {
        final Fixture eventsFixture = testSource.fixture("events");
        final Fixture event_one = (Fixture) eventsFixture.get("event_one");
        dbAssert.table("events").where("id", event_one.get("id"));
        final Map<String, String> fieldValue = new HashMap<String, String>();
        fieldValue.put("name", (String) event_one.get("name"));
        fieldValue.put("login_name", (String) event_one.get("login_name"));
        dbAssert.assert_columns(fieldValue);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidMapAssertion()
    {
        testSource.fixture("events");
        //there is no table definition using table() method neither in column names.
        dbAssert.table().where("id", "1");
        final Map<String, String> fieldValueMap = new HashMap<String, String>();
        fieldValueMap.put("name", "my event");
        fieldValueMap.put("login_name", "dglinenko");
        dbAssert.assert_columns(fieldValueMap);
    }


    @Test(expected = RuntimeException.class)
    public void testMultipleResultsReturned()
    {
        // there are multiple results returned by assert
        testSource.fixture("events");
        dbAssert.table().where("name", "some");
        dbAssert.assert_column("events.id", "5");
    }

    @Test(expected = RuntimeException.class)
    public void testForEmptySource()
    {
        DbAssert dbAssert = DbAssert.init(SOURCE_FILE);
        dbAssert.assert_column("name", "danil");
    }

    @Test(expected = RuntimeException.class)
    public void testForEmptyConditionAndTable()
    {
        dbAssert.assert_column("name", "danil");
    }

    @Test
    public void testInitializationInDbAssert()
    {
        final DbAssert dbAssert = DbAssert.init("net/codemate/databases.yml");
        final DbSource fakeSource = dbAssert.source("hsqlSource", getClass());
        fakeSource.loadSchemaFile("net/codemate/HSqlSourceSchema.sql");
        fakeSource.clean_table("customers");
        final Fixture customerFixtures = fakeSource.fixture("customers");
        final Fixture customer_one  = (Fixture) customerFixtures.get("customer_one");
        dbAssert.table("customers").where("id", customer_one.get("id"));
        dbAssert.assert_column("name", customer_one.get("name"));
    }

    @Test
    public void testInitializationInDbAssert4()
    {
        final DbAssert dbAssert = DbAssert.init("net/codemate/databases.yml");
        final DbSource fakeSource = dbAssert.source("hsqlSource");
        fakeSource.loadSchemaFile("net/codemate/HSqlSourceSchema.sql");
        fakeSource.clean_table("customers");
        final Fixture customerFixtures = fakeSource.fixture("net/codemate/fixtures/customers.yml");
        final Fixture customer_one  = (Fixture) customerFixtures.get("customer_one");
        dbAssert.table("customers").where("id", customer_one.get("id"));
        dbAssert.assert_column("name", customer_one.get("name"));
    }

    @Test
    public void testInitializationInDbAssert2()
    {
        final DbAssert dbAssert = DbAssert.init("net/codemate/databases.yml");
        final DbSource myTestSource = dbAssert.source("testSource", getClass());
        myTestSource.clean_table("customers");
        final Fixture customerFixtures = myTestSource.fixture("customers");
        final Fixture customer_one  = (Fixture) customerFixtures.get("customer_one");
        dbAssert.table("customers").where("id", customer_one.get("id"));
        dbAssert.assert_column("name", customer_one.get("name"));
    }

    @Test
    public void testInitializationInDbAssert3()
    {
        final DbAssert dbAssert = DbAssert.init("net/codemate/databases.yml");
        final DbSource myTestSource = dbAssert.source("testSource");
        myTestSource.clean_table("customers");
        final Fixture customerFixtures = myTestSource.fixture("net/codemate/fixtures/customers.yml");
        final Fixture customer_one  = (Fixture) customerFixtures.get("customer_one");
        dbAssert.table("customers").where("id", customer_one.get("id"));
        dbAssert.assert_column("name", customer_one.get("name"));
    }
}
