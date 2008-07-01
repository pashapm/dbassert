/**
 * @(#) SourceSetTest.java;
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

public class SourceSetTest {
    @Test
    public void testLoadSources() throws SQLException {
        final SourceSet set = new SourceSet();
        set.loadSources(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/testfw/databases.yml"));
        Assert.assertNotNull(set.getConnection("hsqlSource"));
    }
}
