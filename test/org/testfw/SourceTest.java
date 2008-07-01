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

import org.junit.Test;
import org.junit.Assert;

import java.sql.SQLException;

public class SourceTest {

    @Test
    public void testCreateConnection() throws SQLException {
        final Source source = new Source("hsqlSource", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:testSrc","sa", "");
        Assert.assertNotNull(source.createConnection());
    }
}
