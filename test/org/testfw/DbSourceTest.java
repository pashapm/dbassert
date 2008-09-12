/**
 * @(#) DbSourceTest.java;
 * <p/>
 * Created on Mar 23, 2008
 * AUTHOR    ** Danil Glinenko
 * EMAIL     ** dglinenko@gmail.com
 * <p/>
 *
 * Copyright (C) 2008 Danil Glinenko. All rights reserved.
 */

package org.testfw;

import org.ho.yaml.Yaml;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DbSourceTest
{
    DbAssert dbAssert;
    DbSource dbSrc;
    DbSource dbSrc2;
    DbSource.TestBackdoor door;
    DbSource.TestBackdoor door2;
    SourceSet source;

    @Before
    public void setUp() 
    {
        source = new SourceSet();
        source.loadSources(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/testfw/databases.yml"));
        dbSrc = new DbSource("testSource", source , getClass());
        dbSrc2 = new DbSource("testSource",source,  null);
        dbAssert = DbAssert.init("org/testfw/databases.yml");
        dbAssert.source("testSource");
        door = dbSrc.new TestBackdoor();
        door2 = dbSrc2.new TestBackdoor();
        dbSrc.clean_table("test_table");
    }

    @Test
    public void testLoadSqlFromFile() throws IOException
    {
        Assert.assertFalse(door.$loadSqlFromFile("org/testfw/HSqlSourceSchema.sql").isEmpty());
    }

    @Test
    public void testDataInTheSource()
    {
        final DbAssert dbAssert = DbAssert.init("org/testfw/databases.yml");
        final DbSource testSrc = dbAssert.source("hsqlSource", "org/testfw/HSqlSourceSchema.sql", getClass());
        final Fixture customerFixtures = testSrc.fixture("customers");
        final Fixture customer_one = (Fixture) customerFixtures.get("customer_one");
        dbAssert.table("customers");
        dbAssert.condition("id", (Integer) customer_one.get("id"));
        dbAssert.assert_column("name", customer_one.get("name"));
    }

    @Test
    public void testClean_table()
    {
        dbSrc.clean_table("test_table");
        dbAssert.table("test_table");
        dbAssert.assert_count(0);
    }

    @Test
    public void testFixture()
    {
        final Fixture customerFixtures = dbSrc.fixture("test_table");
        Assert.assertEquals("test_table", customerFixtures.getName());
        final Fixture customer_one = (Fixture) customerFixtures.get("customer_one");
        Assert.assertEquals("customer_one", customer_one.getName());
        Assert.assertEquals(1, customer_one.get("id"));
        Assert.assertEquals("John Smith", customer_one.get("name"));
        dbAssert.table("test_table");
        dbAssert.condition("id", (Integer) customer_one.get("id"));
        dbAssert.assert_column("name", "John Smith");
    }

    @Test
    public void testMultipleFixtureLoading()
    {
        final Fixture[] fixtures = dbSrc.fixture("test_table", "customers");
        Assert.assertEquals("test_table", fixtures[0].getName());
        Assert.assertEquals("customers", fixtures[1].getName());
    }

    @Test
    public void testGetFixtureAsStream()
    {
        Assert.assertNotNull(door.$getFixtureAsStream("customers"));
        Assert.assertNotNull(door2.$getFixtureAsStream("org/testfw/fixtures/customers.yml"));
        final Map data = (Map) Yaml.load(door.$getFixtureAsStream("customers"));
        final Map customer_one = (Map) data.get("customer_one");
        Assert.assertEquals(1, customer_one.get("id"));
        Assert.assertEquals("John Smith", customer_one.get("name"));
    }

    @Test
    public void testBuildSQLInsertStmt()
    {
        final Map fixture = new HashMap();
        fixture.put("id", 12);
        fixture.put("name", "danil");

        final String sql = door.$buildSQLInsertStmt("table_name", fixture);
        Assert.assertEquals("INSERT INTO table_name(id, name) VALUES (12, 'danil')", sql);
    }
}
