/**
 * @(#) DbSource.java;
 * <p/>
 * Created on Mar 23, 2008
 * AUTHOR    ** Danil Glinenko
 * EMAIL     ** dglinenko@gmail.com
 * <p/>
 *
 * Copyright (C) 2008 Danil Glinenko. All rights reserved.
 */

package net.codemate;


import org.ho.yaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * Class to put your database source to a known state.
 */
public class DbSource {
    private static final Logger log = Logger.getLogger(DbSource.class.getName());

    //private final SourceSet sourceSet;
    private final Source source;
    private final String sourceName;
    private final Class invokerClass;
    private static final String VALUE_COL = "returned_value";
    private static final String SQL_INSERT_STMT = "INSERT INTO {0}({1}) VALUES ({2})";
    private static final String SQL_DELETE_STMT = "DELETE FROM {0} WHERE {1}";
    private static final String SQL_SELECT_COL_VAL = "SELECT {0} AS \"" + VALUE_COL + "\" FROM {1} WHERE {2}";
    private static final String TBL_COL_IDEN = ".";


    /**
     * @param source  Source contains connection settings to the data source.
     * @param sourceName the name of the db datasource to be used.
     * @param invoker    class name of the DbSource invoker to be able to load fixtures located with the invoker class.
     */
    DbSource(final String sourceName, final Source source, final Class invoker) {
        this.source= source;
        this.sourceName = sourceName;
        this.invokerClass = invoker;
    }

    DbSource(final String sourceName) {
        this(sourceName, null, null);
    }

    /**
     * Loads given db schema file to the datasource.
     * @param schemaFileName db schema file.
     * @return DbSource object.
     */
    public DbSource loadSchemaFile(final String schemaFileName) {
        try {
            final String sqlSchema = loadSQLFromFile(schemaFileName);
            applySql(sqlSchema);
        }
        catch (SQLException e) {
            throw new RuntimeException("Unable to apply schema file: " + schemaFileName + " check your SQL schema file.", e);
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to load schema file: " + schemaFileName, e);
        }
        return this;
    }

    /**
     * Runs SQL statements on a given sql connection object.
     *
     * @param sql        SQL statements to run.
     * @throws SQLException if unable to run SQL statements.
     */
    private void applySql(final String sql) throws SQLException {
        final StringTokenizer st = new StringTokenizer(sql, ";");
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            if (!token.equals("")) {
                this.runSqlStatement(token += ";", false);
            }
        }
    }

    private String loadSQLFromFile(final String fileName) throws IOException {
        //load file and return its contents as String
        final InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        if (file != null) {
            final byte[] b = new byte[file.available()];
            file.read(b);
            file.close();
            return new String(b);
        } else {
            throw new RuntimeException("File: " + fileName + " cannot be found or loaded:");
        }
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
            cleanTable(table, null);
        }
    }


    private void cleanTable(final String tableName, final String whereClause) {
        final String where;
        if (whereClause == null || whereClause.length() == 0) {
            where = "1 = 1";
        } else {
            where = whereClause;
        }
        final String cleanTableSQL = MessageFormat.format(SQL_DELETE_STMT, tableName, where);
        try {
            runSqlStatement(cleanTableSQL, false);
        }
        catch (SQLException e) {
            throw new RuntimeException("Unable to clean table: " + tableName, e);
        }
    }

    public void clean_table_where(final String tableName, final String whereClause) {
        if (tableName == null || tableName.length() == 0) { throw new IllegalArgumentException("\"tableName\" param can not be null or empty."); }
        cleanTable(tableName, whereClause);
    }

    private Object runSqlStatement(final String sql, final boolean isSelect) throws SQLException {
        log.fine("Running following SQL:\n" + sql);

        try {
            Connection connection = this.source.createConnection();
            final Statement statement = connection.createStatement();
            if (isSelect) {
                final ResultSet rs = statement.executeQuery(sql);
                boolean foundSmth = rs.next();
                if (foundSmth) {
                    if (!rs.isLast()) {
                        throw new RuntimeException("Too many results fetched try to specify more conditions");
                    }
                    return rs.getObject(VALUE_COL);
                } else {
                    return null;
                }
            } else {
                return statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new SQLException("Unable to execute the following SQL stmt: " + sql, e);
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
        String fixturePath ="";
        if (invokerClass != null) {
            final String clazzName = invokerClass.getName();
            if (clazzName.lastIndexOf(".") >0){
                fixturePath = clazzName.substring(0, clazzName.lastIndexOf(".")).replace(".", "/") + "/";
            }
            fixturePath += "fixtures/" + fixName + ".yml";
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

    Object select(final String selectOp, final Condition condition) throws SQLException {
        String tableName = condition.getTableName();
        if (tableName == null) {
            //try to derive table name from column name (e.g. events.name where "events" is a table and name is a column)
            tableName = deriveTableName(selectOp);
        }
        final String sqlSelect = MessageFormat.format(SQL_SELECT_COL_VAL, selectOp,tableName, condition.getCondtionClause());
        return runSqlStatement(sqlSelect, true);
    }

    private String deriveTableName(final String tableColumn) {
        if (tableColumn.contains(TBL_COL_IDEN)) {
            return tableColumn.substring(0, tableColumn.indexOf(TBL_COL_IDEN));
        } else {
            return null;
        }
    }

    /**
     * Cleans up resources and closes open database connections.
     * Be a nice citizen and please call this method when you're done.
     * <p>
     * Using @After or @AfterClass in your jUnit tests would be a good choice.
     */
    public void close() {
        this.source.close();
    }

    // Only for testing purpose
    public class TestBackdoor {
        public InputStream $getFixtureAsStream(final String fixName) {
            return getFixtureAsStream(fixName);
        }

        public String $loadSqlFromFile(final String fileName) throws IOException {
            return loadSQLFromFile(fileName);
        }

        public String $buildSQLInsertStmt(final String tableName, final Map fixture) {
            return buildSQLInsertStmt(tableName, fixture);
        }
    }

}
