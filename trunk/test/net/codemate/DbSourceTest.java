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

package net.codemate;

import org.ho.yaml.Yaml;
import org.junit.After;
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
    SourceSet sourceSet;

    @Before
    public void setUp()
    {
        sourceSet = new SourceSet();
        sourceSet.loadSources(Thread.currentThread().getContextClassLoader().getResourceAsStream("net/codemate/databases.yml"));
        String sourceName = "testSource";
        dbSrc = new DbSource(sourceName, sourceSet.getSourceByName(sourceName) , getClass());
        dbSrc2 = new DbSource(sourceName, sourceSet.getSourceByName(sourceName),  null);
        dbAssert = DbAssert.init("net/codemate/databases.yml");
        dbAssert.source("testSource");
        door = dbSrc.new TestBackdoor();
        door2 = dbSrc2.new TestBackdoor();
        dbSrc.clean_table("test_table");
    }

    @After
    public  void oneTieTearDown() {
        dbSrc.close();
        dbSrc2.close();
    }

    @Test
    public void testLoadSqlFromFile() throws IOException
    {
        Assert.assertFalse(door.$loadSqlFromFile("net/codemate/HSqlSourceSchema.sql").isEmpty());
    }

    @Test
    public void testDataInTheSource()
    {
        final DbAssert dbAssert = DbAssert.init("net/codemate/databases.yml");
        final DbSource testSrc = dbAssert.source("hsqlSource",  getClass());
        testSrc.loadSchemaFile("net/codemate/HSqlSourceSchema.sql");
        final Fixture customerFixtures = testSrc.fixture("customers");
        final Fixture customer_one = (Fixture) customerFixtures.get("customer_one");
        dbAssert.table("customers").where("id", customer_one.get("id"));
        dbAssert.assert_column("name", customer_one.get("name"));
    }

    @Test
    public void testClean_table()
    {
        dbSrc.fixture("test_table");
        dbAssert.table("test_table");
        dbAssert.assert_count(2);
        dbSrc.clean_table("test_table");
        dbAssert.assert_count(0);
    }

    @Test
    public void testCleanTableWithCond(){
        dbSrc.fixture("test_table");
        dbAssert.table("test_table");
        dbAssert.assert_count(2);
        dbSrc.clean_table_where("test_table", "id = 2");
        dbAssert.assert_count(1);
    }

    @Test
    public void testCleanTableWithCond2(){
        dbSrc.fixture("test_table");
        dbAssert.table("test_table");
        dbAssert.assert_count(2);
        dbSrc.clean_table_where("test_table", "name = \'Danil Glinenko\'");
        dbAssert.assert_count(1);
    }

    @Test
    public void testCleanTableWithCond3(){
        dbSrc.fixture("test_table");
        dbAssert.table("test_table");
        dbAssert.assert_count(2);
        dbSrc.clean_table_where("test_table", "id = 1 or name = \'Danil Glinenko\'");
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
        dbAssert.table("test_table").where("id", customer_one.get("id"));
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
        Assert.assertNotNull(door2.$getFixtureAsStream("net/codemate/fixtures/customers.yml"));
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
