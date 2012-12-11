/**
 * @(#) SourceSet.java;
 * <p/>
 * Created on Jun 20, 2008
 * AUTHOR    ** Danil Glinenko
 * EMAIL     ** dglinenko@gmail.com
 * <p/>
 *
 * Copyright (C) 2008 Danil Glinenko. All rights reserved.
 */

package net.codemate;

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

    /*package*/ void addSource(String name, final String driver, final String url, final String username, final String password) {
        sources.put(name, new Source(name, driver, url, username, password));
    }

    Source getSourceByName(final String sourceName) {
        final Source source = sources.get(sourceName);
        if (source == null) {
            throw new RuntimeException("\"" + sourceName + "\"" + " is not initialized.");
        }
        return source;

    }

    Connection getConnection(final String sourceName) throws SQLException {
        final Source source = getSourceByName(sourceName);
        return source.createConnection();
    }
}