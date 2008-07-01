/**
 * @(#) HsqlSourceTest.java;
 * <p/>
 * Created on May 27, 2008
 * AUTHOR    ** Danil Glinenko
 * EMAIL     ** dglinenko@rbauction.com
 * <p/>
 * /**
 * This software is the confidential and proprietary information of
 * Ritchie Bros. You shall use it only in accordance with the terms of
 * the license agreement you entered into with Ritchie Bros.
 *
 * Copyright (C) 2007 Ritchie Bros. All rights reserved.
 */

package org.testfw;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class HsqlSourceTest
{
    private static HsqlSource hsqlSource;
    private static HsqlSource.TestDoor door;

    @BeforeClass
    public static void mainSetUp()
    {
        final SourceSet sources = new SourceSet();
        sources.loadSources(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/testfw/databases.yml"));
        hsqlSource = new HsqlSource("hsqlSource", sources, HsqlSourceTest.class);
        door = hsqlSource.new TestDoor();
    }


    @Test
    public void testLoadSqlFromFile() throws IOException
    {
        Assert.assertFalse(door.$loadSqlFromFile("org/testfw/testSourceSchema.sql").isEmpty());
    }

    @Test
    public void testDataInTheSource()
    {
        final DbAssert dbAssert = DbAssert.init("org/testfw/databases.yml");
        final DbSource testSrc = dbAssert.source("hsqlSource", getClass());
        hsqlSource.loadSchemaFile("org/testfw/testSourceSchema.sql");
        final Fixture customerFixtures = testSrc.fixture("customers");
        final Fixture customer_one = (Fixture) customerFixtures.get("customer_one");
        dbAssert.table("customers");
        dbAssert.condition("id", (Integer) customer_one.get("id"));
        dbAssert.assert_column("name", customer_one.get("name"));
    }

}
