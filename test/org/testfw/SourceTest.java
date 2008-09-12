/**
 * @(#) SourceTest.java;
 * <p/>
 * Created on Jun 23, 2008
 * AUTHOR    ** Danil Glinenko
 * EMAIL     ** dglinenko@gmail.com
 * <p/>
 *
 * Copyright (C) 2008 Danil Glinenko. All rights reserved.
 */

package org.testfw;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SourceTest {

    @Test
    public void testCreateConnection() throws SQLException {
        final Source source = new Source("hsqlSource", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:testSrc","sa", "");
        Assert.assertNotNull(source.createConnection());
    }
    @Test
    public void testPreparedStmt() throws SQLException {
        final Source source = new Source("hsqlSource", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:testSrc","sa", "");
        final Connection con = source.createConnection();

        Assert.assertNotNull(con);
        final DbAssert dbas = DbAssert.init("org/testfw/databases.yml");
        final DbSource src = dbas.source("hsqlSource");
        src.clean_table("customers");

        final PreparedStatement pstmt = con.prepareStatement("INSERT INTO customers(id, name) values(?, ?)");
        pstmt.setInt(1, 75);
        pstmt.setString(2, "aaa");
        pstmt.execute();
        
        dbas.condition("customers.id", 75);
        dbas.assert_column("customers.name", "aaa");
        System.out.println(dbas.column_value("customers.name"));

    }
}
