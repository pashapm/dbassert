/**
 * DbAssert.java;
 * <p/>
 * Created on Apr 6, 2009
 * AUTHOR    ** Danil Glinenko
 * EMAIL     ** dglinenko@gmail.com
 * <p/>
 */

package net.codemate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Map;

public class DbAssert implements DbAssertInterface {
    private Condition condition;
    private final SourceSet sources = new SourceSet();
    private DbSource dbSource;

    private DbAssert(final String dbSourcesFile) {
        if (dbSourcesFile == null) {
            throw new IllegalArgumentException("Data source definition file cannot be null.");
        }
        try {
            sources.loadSources(Thread.currentThread().getContextClassLoader().getResourceAsStream(dbSourcesFile));
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to load/parse: " + dbSourcesFile + " as data source definition file.", e);
        }
    }

    private DbAssert(final String driver, final String url, final String username, final String password) {
        sources.addSource("", driver, url, username, password);
    }

    /**
     * Initializes dbAssert from datasources defined in the given file.
     * @param dbSourcesFile  file where data sources are defined.
     * @return DbAssert instance.
     */
    public static DbAssert init(final String dbSourcesFile) {
        return new DbAssert(dbSourcesFile);
    }

    public static DbAssert init(final String driver, final String url, final String username, final String password) {
        assertNotNull(driver);
        assertNotNull(url);
        assertNotNull(username);
        assertNotNull(password);
        return new DbAssert(driver, url, username, password);
    }

    /**
     * Sets the source name up. It must be one from specified using init() method.
     * @param sourceNameToUse name of the source to be used.
     * @param invokerClass  class name of the DbAssert invoker to be able to load fixtures located with the invoker class.
     * @return DbSource object representing the source name passed as params. This object can be used to prepare the database.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public DbSource source(String sourceNameToUse, Class invokerClass) {
        if (sourceNameToUse == null){throw new IllegalArgumentException("\"sourceNameToUse\" param can not be null.");}
        this.condition = new Condition();
        this.dbSource = new DbSource(sourceNameToUse, this.sources.getSourceByName(sourceNameToUse), invokerClass);
        return this.dbSource;
    }

    /**
     * Sets the source name up.  Uses the default source.
     * @return DbSource object representing the default source. This object can be used to prepare the database.
     */
    public DbSource source() {
        return source("", null);
    }

    /**
     * Sets the source name up. It must be one from specified using init() method.
     * @param sourceNameToUse name of the source to be used.
     * @return DbSource object representing the source name passed as params. This object can be used to prepare the database.
     */
    @Override
    public DbSource source(String sourceNameToUse) {
        return source(sourceNameToUse, null);
    }

    /**
     * Sets the table name to be used for assertions. Resets previously set conditions.
     * @param tableName table name.
     * @return Condition object that can be used to set additional conditions for assertions.
     */
    @Override
    public Condition table(final String tableName) {
        checkProperInitialization();
        return this.condition.table(tableName);
    }

    /**
     * Resets previously set conditions.
     * @return Condition object that can be used to set addtional conditions for assertions.
     */
    @Override
    public Condition table() {
        checkProperInitialization();
        return this.condition.table();
    }
    /**
     * Compares actual value in database field <code>fieldName</code> with given <code>expectedValue</code>
     *
     * @param fieldName     field name in table to look for.
     * @param expectedValue expected value in the field.
     */
    @Override
    public void assert_column(String fieldName, Object expectedValue) {
        checkFieldName(fieldName);
        final Object actualValue = column_value(fieldName);
        assertEquals(expectedValue, actualValue);
    }

    public void assert_column(String fieldName, BigDecimal expectedValue) {
        checkFieldName(fieldName);
        BigDecimal actualValue = (BigDecimal)column_value(fieldName);
        if (actualValue != null) {
            actualValue = actualValue.stripTrailingZeros();
        }
        if (expectedValue != null) {
            expectedValue = expectedValue.stripTrailingZeros();
        }
        assertEquals(expectedValue, actualValue);
    }

    /*
    * Checks if the field with given name is empty in database.
    *
    * @param fieldName field name to check value into.
    */
    @Override
    public void assert_not_empty(String fieldName) {
        checkFieldName(fieldName);
        assertNotNull(column_value(fieldName));
    }

    private void checkFieldName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("\"fieldName\" param can not be null or empty.");
        }
    }

    /**
     * Checks if the record count for the conditions set match
     * <code>count</code>
     *
     * @param count expected number of records matched.
     */
    @Override
    public void assert_count(Integer count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be 0 or more");
        }
        assertEquals(count.longValue(), (long)takeCount("*"));
    }
    /**
     * Checks if number of returned records greater than given number
     *
     * @param fieldName fieldName for count() function. Might be "*"
     * @param counter   expected number of records should be greter than this.
     */
    @Override
    public void assert_count_gt(String fieldName, Integer counter) {
        if (fieldName == null || counter < 0) {
            throw new IllegalArgumentException("fieldName param must not be null and counter must not be negative.");
        }
        assertTrue(takeCount(fieldName) > counter);
    }
    /**
     * Checks if number of returned records less than given number
     *
     * @param fieldName fieldName for count() function. Might be "*"
     * @param counter   expected number of records should be less than this.
     */
    @Override
    public void assert_count_lt(String fieldName, int counter) {
        if (fieldName == null || counter < 0) {
            throw new IllegalArgumentException("fieldName param must not be null and counter must not be negative.");
        }
        assertTrue(takeCount(fieldName) < counter);
    }
    /**
     * Compares actual values in database with expected ones.
     *
     * @param fieldValueMap Map with db fields and expected values. KEY: fieldName; VALUE: expectedValue.
     */
    @Override
    public void assert_columns(Map<String, String> fieldValueMap) {
        if (fieldValueMap == null){throw new IllegalArgumentException("\"fieldValueMap\" param can not be null.");}
        for (final String fieldName : fieldValueMap.keySet()) {
            final String expectedValue = fieldValueMap.get(fieldName);
            assert_column(fieldName, expectedValue);
        }
    }

    /**
     * Returns actual value from the database stored in given column and matching set conditions.
     * @param fieldName field name to get value from.
     * @return actual value stored in db.
     */
    @Override
    public Object column_value(String fieldName) {
        checkFieldName(fieldName);
        validatePreConditions(fieldName);
        final Object result;
        try {
            result = this.dbSource.select(fieldName, this.condition);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private Long takeCount(final String fieldName) {
        final Long count;
        final Object returnedValue = column_value("count(" + fieldName + ")");
        try {
            count = (Long) returnedValue;
        }
        catch (NumberFormatException e) {
            throw new RuntimeException("Returned result is not of numeric type.");
        }
        return count;
    }
    private void validatePreConditions(final String fieldName) {
        boolean isTableSet = isTableDefined(fieldName) || this.condition.getTableName() != null;
        if (!isTableSet) {
            throw new RuntimeException("Pre conditions aren't set up. Try to specify table name.");
        }
    }

    private void checkProperInitialization() {
        if (this.condition == null) {
            throw new RuntimeException("Datasource is not defined. Call DbAssert::source() first to specify the source to test.");
        }
    }

    private boolean isTableDefined(final String fieldName) {
        return fieldName.contains(".");
    }
}
