/**
 * @(#) SourceTest.java;
 * <p/>
 * Created on Jun 23, 2008
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
