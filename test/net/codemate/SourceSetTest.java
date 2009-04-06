/**
 * @(#) SourceSetTest.java;
 * <p/>
 * Created on Jun 23, 2008
 * AUTHOR    ** Danil Glinenko
 * EMAIL     ** dglinenko@gmail.com
 * <p/>
 *
 * Copyright (C) 2008 Danil Glinenko. All rights reserved.
 */

package net.codemate;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

public class SourceSetTest {
    @Test
    public void testLoadSources() throws SQLException {
        final SourceSet set = new SourceSet();
        set.loadSources(Thread.currentThread().getContextClassLoader().getResourceAsStream("net/codemate/databases.yml"));
        Assert.assertNotNull(set.getConnection("hsqlSource"));
    }
}
