/**
 * @(#) Source.java;
 * <p/>
 * Created on Jun 23, 2008
 * AUTHOR    ** Danil Glinenko
 * EMAIL     ** dglinenko@gmail.com
 * <p/>
 *
 * Copyright (C) 2008 Danil Glinenko. All rights reserved.
 */

package net.codemate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class Source {
    private final String name;
    private final String driver;
    private final String url;
    private final String username;
    private final String password;
    private Connection connection;

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
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName(this.driver);
                connection = DriverManager.getConnection(this.url, this.username, this.password);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Unable to load jdbc driver: " + this.driver);
            }
        }
        return connection;
    }

    void close() {
        try {
            if (connection != null || !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (Exception e) {}
    }
}