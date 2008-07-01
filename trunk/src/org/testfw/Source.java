/**
 * @(#) Source.java;
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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

class Source {
    private final String name;
    private final String driver;
    private final String url;
    private final String username;
    private final String password;

    Source(final String name, final String driver, final String url, final String username, final String password) {
        if (name == null || driver == null || url == null) {
            throw new IllegalArgumentException("Source name, driver or url cannot be null.");
        }
        this.name = name;
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    Connection createConnection() throws SQLException {
        final Connection jdbcCon;
        try {
            Class.forName(this.driver);
            jdbcCon = DriverManager.getConnection(this.url, this.username, this.password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to load jdbc driver: " + this.driver);
        }
        return jdbcCon;
    }
}
