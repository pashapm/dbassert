/**
 * @(#) DbSource.java;
 * <p/>
 * Created on Mar 23, 2008
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
import java.text.MessageFormat;
import java.util.Map;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * Class to put your database source to a known state.
 */
public class DbSource {
    protected SourceSet sourceSet;
    protected String sourceName;
    protected Class invokerClass;
    private static final String VALUE_COL = "returned_value";
    private static final String SQL_INSERT_STMT = "INSERT INTO {0}({1}) VALUES ({2})";
    private static final String SQL_DELETE_STMT = "DELETE FROM {0}";
    private static final String SQL_SELECT_COL_VAL = "SELECT {0} AS \"" + VALUE_COL + "\" FROM {1} WHERE {2}";


    /**
     * @param sourceSet  iSpecs source set.
     * @param sourceName the name of the db datasource to be used.
     * @param invoker    class name of the DbSource invoker to be able to load fixtures located with the invoker class.
     */
    DbSource(final String sourceName, final SourceSet sourceSet, final Class invoker) {
        this.sourceSet = sourceSet;
        this.sourceName = sourceName;
        this.invokerClass = invoker;
    }

    DbSource(final String sourceName) {
        this(sourceName, null, null);
    }

    /**
     * Clean tables from the given array in the datasource initialized.
     *
     * @param tables array of table names to be cleaned.
     */
    public void clean_table(final String... tables) {
        if (tables == null || tables.length == 0) {
            throw new IllegalArgumentException("tables parameter cannot be null or empty");
        }
        for (final String table : tables) {
            cleanTable(table);
        }
    }


    private void cleanTable(final String tableName) {
        final String cleanTableSQL = MessageFormat.format(SQL_DELETE_STMT, tableName);
        try {
            runSqlStatement(cleanTableSQL, false);
        }
        catch (SQLException e) {
            throw new RuntimeException("Unable to clean table: " + tableName, e);
        }
    }

    private Object runSqlStatement(final String sql, final boolean isSelect) throws SQLException {
        Connection connection = null;
        try {
            connection = this.sourceSet.getConnection(sourceName);
            final Statement statement = connection.createStatement();
            if (isSelect) {
                final ResultSet rs = statement.executeQuery(sql);
                rs.next();
                if (!rs.isLast()) {
                    throw new RuntimeException("Too many results fetched try to specify more conditions");
                }
                return rs.getObject(VALUE_COL);
            } else {
                return statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Loads given fixture to db datasource.
     *
     * @param fixtureName the name of fixture file that has to be equal to the table name which this fixture will be used for.
     *                    If DbSource initialized with invoker param (class name of the invoker class) then all fixture files will be looked for under:
     *                    &lt;invoker's package &gt;/fixtures/ folder otherwise full path to the fixture file has to be specified.
     * @return Fixture object which is basically HashMap from YAML key: value pairs.
     */
    public Fixture fixture(final String fixtureName) {
        if (fixtureName == null) {
            throw new IllegalArgumentException("fixtureName param cannot be null");
        }
        final Map fixtureMap = (Map) Yaml.load(getFixtureAsStream(fixtureName));
        String fixName = fixtureName;
        if (fixName.contains("/")) {
            fixName = fixName.substring(fixName.lastIndexOf("/") + 1, fixName.lastIndexOf("."));
        }
        final Fixture fix = new Fixture(fixName);
        fix.putAll(fixtureMap);
        try {
            applyFixtureToDS(fix);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to load fixture: " + fixtureName + " to database.", e);
        }
        return fix;
    }

    /**
     * Loads given fixtures to db datasource
     *
     * @param fixtureNames names of the fixtures to load data from.
     * @return array of Fixture objects loaded to database.
     * @see #fixture(String) Load a single fixture.
     */
    public Fixture[] fixture(final String... fixtureNames) {
        if (fixtureNames == null || fixtureNames.length == 0) {
            throw new IllegalArgumentException("fixtureNames param cannot be null or contain no elements.");
        }
        final Fixture[] fixes = new Fixture[fixtureNames.length];
        for (int i = 0; i < fixtureNames.length; i++) {
            final String fixtureName = fixtureNames[i];
            fixes[i] = fixture(fixtureName);
        }
        return fixes;
    }

    private void applyFixtureToDS(final Fixture fix) throws SQLException {
        final String tableName = fix.getName();
        for (final Object key : fix.keySet()) {
            final String sqlInsertStmt = buildSQLInsertStmt(tableName, (Map) fix.get(key));
            runSqlStatement(sqlInsertStmt, false);
        }
    }

    private String buildSQLInsertStmt(final String tableName, final Map fixture) {
        final StringBuilder fields = new StringBuilder();
        final StringBuilder values = new StringBuilder();
        for (final Object key : fixture.keySet()) {
            final String colKey = (String) key;
            final Object colValue = fixture.get(colKey);
            final String value;
            if (colValue instanceof Integer) {
                value = String.valueOf(colValue);
            } else if (colValue == null) {
                value = null;
            } else {
                value = "'" + colValue + "'";
            }
            fields.append(colKey).append(", ");
            values.append(value).append(", ");
        }
        //normalize string with columns and values
        final String field = fields.substring(0, fields.lastIndexOf(","));
        final String value = values.substring(0, values.lastIndexOf(","));
        return MessageFormat.format(SQL_INSERT_STMT, tableName, field, value);
    }

    private InputStream getFixtureAsStream(final String fixName) {
        final String fixturePath;
        if (invokerClass != null) {
            final String clazzName = invokerClass.getName();
            fixturePath = clazzName.substring(0, clazzName.lastIndexOf(".")).replace(".", "/") + "/fixtures/" + fixName + ".yml";
        } else {
            fixturePath = fixName;
        }
        final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fixturePath);
        if (is != null) {
            return is;
        } else {
            throw new RuntimeException("Unable to load fixture: " + fixName);
        }
    }

    Object select(final String selectOp, final String tableName, final String condtionClause) throws SQLException {
        final String sqlSelect = MessageFormat.format(SQL_SELECT_COL_VAL, selectOp, tableName, condtionClause);
        return runSqlStatement(sqlSelect, true);
    }

    // Only for testing purpose
    public class TestBackdoor {
        public InputStream $getFixtureAsStream(final String fixName) {
            return getFixtureAsStream(fixName);
        }


        public String $buildSQLInsertStmt(final String tableName, final Map fixture) {
            return buildSQLInsertStmt(tableName, fixture);
        }

    }

}
