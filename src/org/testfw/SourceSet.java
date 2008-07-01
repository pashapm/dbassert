/**
 * @(#) SourceSet.java;
 * <p/>
 * Created on Jun 20, 2008
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

import org.ho.yaml.Yaml;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class SourceSet {
    private final Map<String, Source> sources = new ConcurrentHashMap<String, Source>();

    void loadSources(final InputStream dsInputStream) {
        if (dsInputStream == null) {
            throw new IllegalArgumentException("InputStream with source definitions cannot be null");
        }
        final Map dbSources = (Map) Yaml.load(dsInputStream);

        for (final Object sourceName : dbSources.keySet()) {
            final Map<String, String> sourceData = (Map<String, String>) dbSources.get(sourceName);
            final String driver = sourceData.get("driver");
            final String url = sourceData.get("url");
            final String username = sourceData.get("username");
            final String password = sourceData.get("password");

            final Source source = new Source((String) sourceName, driver, url, username, password);
            this.sources.put((String) sourceName, source);
        }
    }

    Connection getConnection(final String sourceName) throws SQLException {
        final Source source = sources.get(sourceName);
        if (source == null) {
            throw new RuntimeException("\"" + sourceName + "\"" + " is not initialized.");
        }
        return source.createConnection();
    }
}
