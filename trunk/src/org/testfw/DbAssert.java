/**
 * @(#) DbAssert.java;
 * <p/>
 * Created on Mar 8, 2008
 * AUTHOR    ** Danil Glinenko
 * EMAIL     ** dglinenko@gmail.com
 * <p/>
 */

package org.testfw;

import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to make assertions against database data.
 */
public class DbAssert {
    private SourceSet sources;
    private String tableName;
    private Map<String, Object> conditions;
    private DbSource dbSource;

    private DbAssert(final String dsourcesFile) {
        if (dsourcesFile == null) {
            throw new IllegalArgumentException("Data source definition file cannot be null.");
        }
        sources = new SourceSet();
        try {
            sources.loadSources(Thread.currentThread().getContextClassLoader().getResourceAsStream(dsourcesFile));
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to load/parse: " + dsourcesFile + " as data source definition file.", e);
        }
    }


    /**
     * Initiates DbAssert by give Ispec source file.
     *
     * @param sourcesFile Xml file containing source definitions for Ispecs.
     * @return instanse of DbAssert class to make assertion on the data.
     */
    public static DbAssert init(final String sourcesFile) {
        if (sourcesFile == null) {
            throw new IllegalArgumentException("sourcesFile param cannot be null.");
        }
        return new DbAssert(sourcesFile);
    }

    /**
     * Sets source name to be used to retrieve data from.
     *
     * @param sourceNameToUse name of the source to use.
     * @param invokerClass    class name of the DbAssert invoker to be able to load fixtures located with the invoker class.
     * @return DbSource object to be used to prepare the datasource (clean tables, load data, etc.)
     */
    public DbSource source(final String sourceNameToUse, final Class invokerClass) {
        if (sourceNameToUse == null) {
            throw new IllegalArgumentException("SourceNameToUse param cannot be null.");
        }
        resetCondtions();
        this.tableName = null;
        return this.dbSource = new DbSource(sourceNameToUse, sources, invokerClass);
    }

    /**
     * Sets source name to be used to retrieve data from.
     *
     * @param sourceNameToUse name of the source to use.
     * @return DbSource object to be used to prepare the datasource (clean tables, load data, etc.)
     */
    public DbSource source(final String sourceNameToUse) {
        return source(sourceNameToUse, null);
    }

    public DbSource fake_source(final String sourceName, final String schemaFileName, final Class invokerClass) {
        if (sourceName == null || schemaFileName == null) {
            throw new IllegalArgumentException("sourceName or schemaFileName params cannot be null");
        }
        resetCondtions();
        final HsqlSource fake = new HsqlSource(sourceName, sources, invokerClass);
        fake.loadSchemaFile(schemaFileName);
        return this.dbSource = fake;
    }

    public DbSource fake_source(final String sourceName, final String schemaFileName) {
        return fake_source(sourceName, schemaFileName, null);
    }

    /**
     * Sets new condition clause for SELECT statement used by dbAssert to retrieve data from datasource.
     *
     * @param conditionKey   - field name in databse to be used to search values by
     * @param conditionValue - value to be used to compare found result with.
     *                       <p>EXAMPLE: <br />
     *                       condition("cusomer_name", "John Smith") becomes SELECT ... WHERE customer_name = 'John Smith'; <br />
     * @return returns instance of itself to be able to use constructs like .condition(cond1).addCondition(cond2).addCondition(cond3);
     */
    public DbAssert condition(final String conditionKey, final String conditionValue) {
        this.setCondition(conditionKey, conditionValue);
        return this;
    }

    /**
     * Sets new condition clause for SELECT statement used by dbAssert to retrieve data from datasource.
     *
     * @param conditionKey   - field name in databse to be used to search values by
     * @param conditionValue - value to be used to compare found result with.
     *                       <p>EXAMPLE: <br />
     *                       condition("id", 123) becomes SELECT ... WHERE id = 123; <br />
     * @return returns instance of itself to be able to use constructs like .condition(cond1).addCondition(cond2).addCondition(cond3);
     */
    public DbAssert condition(final String conditionKey, final Integer conditionValue) {
        this.setCondition(conditionKey, conditionValue);
        return this;
    }

    /**
     * Adds addtional condition for the Assertion. The condition will be added using AND clause. You may have as many condions as you want.
     *
     * @param conditionKey   - field name in databse to be used to search values by
     * @param conditionValue - value to be used to compare found result with.
     * @return returns instance of itself to be able to use constructs like .condition(cond1).addCondition(cond2).addCondition(cond3);
     */
    public DbAssert addCondition(final String conditionKey, final String conditionValue) {
        this.setAdditionalCondition(conditionKey, conditionValue);
        return this;
    }

    /**
     * Adds addtional condition for the Assertion. The condition will be added using AND clause. You may have as many condions as you want.
     *
     * @param conditionKey   - field name in databse to be used to search values by
     * @param conditionValue - value to be used to compare found result with.
     * @return returns instance of itself to be able to use constructs like .condition(cond1).addCondition(cond2).addCondition(cond3);
     */
    public DbAssert addCondition(final String conditionKey, final Integer conditionValue) {
        this.setAdditionalCondition(conditionKey, conditionValue);
        return this;
    }

    /**
     * Sets table name to be used for asertions.
     *
     * @param tableName name of database table to be used for assertions.
     */
    public void table(final String tableName) {
        if (tableName == null) {
            throw new IllegalArgumentException("TableName param can not be null.");
        }
        this.tableName = tableName;
        //release conditions
        resetCondtions();
    }

    /**
     * Compares actual value in database field <code>fieldName</code> with given <code>expectedValue</code>
     *
     * @param fieldName     field name in table to look for.
     * @param expectedValue expected value in the field.
     */
    public void assert_column(final String fieldName, final Object expectedValue) {
        if (fieldName == null) {
            throw new IllegalArgumentException("FieldName: <" + fieldName + "> or expectedValue: <" + expectedValue + "> params cannot be null.");
        }
        final Object actualValue = column_value(fieldName);
        Assert.assertEquals(expectedValue, actualValue);

    }

    /*
     * Checks if the field with given name is empty in database.
     *
     * @param fieldName field name to check value into.
     */
    public void assert_not_empty(final String fieldName) {
        if (fieldName == null) {
            throw new IllegalArgumentException("fieldName param cannot be null.");
        }
        validateInput(fieldName);
        Assert.assertNotNull(column_value(fieldName));
    }

    /**
     * Checks if the record count for the conditions set match
     * <code>count</code>
     *
     * @param count expected number of records should equal this.
     */
    public void assert_count(final Integer count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be 0 or more");
        }
        Assert.assertEquals(count.longValue(), column_value("count(*)"));
    }

    /**
     * Checks if number of returned records greater than given number
     *
     * @param fieldName fieldName for count() function. Might be "*"
     * @param counter   expected number of records should be greter than this.
     */
    public void assert_count_gt(final String fieldName, final Integer counter) {
        if (fieldName == null || counter < 0) {
            throw new IllegalArgumentException("fieldName param must not be null and counter must not be negative.");
        }
        Assert.assertTrue(takeCount(fieldName) > counter);
    }

    /**
     * Checks if number of returned records less than given number
     *
     * @param fieldName fieldName for count() function. Might be "*"
     * @param counter   expected number of records should be less than this.
     */
    public void assert_count_lt(final String fieldName, final int counter) {
        if (fieldName == null || counter < 0) {
            throw new IllegalArgumentException("fieldName param must not be null and counter must not be negative.");
        }
        Assert.assertTrue(takeCount(fieldName) < counter);
    }

    /**
     * Compares actual values in database with expected ones.
     *
     * @param fieldValueMap Map with db fields and expected values. KEY: fieldName; VALUE: expectedValue.
     */
    public void assert_columns(final Map<String, String> fieldValueMap) {
        if (fieldValueMap == null) {
            throw new IllegalArgumentException("fieldValueMap param cannot be null.");
        }

        for (final String fieldName : fieldValueMap.keySet()) {
            final String expectedValue = fieldValueMap.get(fieldName);
            assert_column(fieldName, expectedValue);
        }
    }

    public Object column_value(final String fieldName) {
        return runQuery(fieldName);
    }

    private void setCondition(final String conditionKey, final Object conditionValue) {
        if (conditionKey == null || conditionValue == null) {
            throw new IllegalArgumentException("conditionKey and conditionValue params cannot be null.");
        }
        resetCondtions();
        this.conditions.put(conditionKey, conditionValue);
    }

    private void setAdditionalCondition(final String conditionKey, final Object conditionValue) {
        if (conditionKey == null || conditionValue == null) {
            throw new IllegalArgumentException("conditionKey and conditionValue params cannot be null.");
        }
        this.conditions.put(conditionKey, conditionValue);
    }

    private void resetCondtions() {
        this.conditions = new HashMap<String, Object>();
    }

    private Long takeCount(final String fieldName) {
        final Long count;
        if (dbSource == null) {
            throw new RuntimeException("Name of the source must be defined. Use dbAssert.sourceName()");
        }
        if (tableName == null) {
            throw new RuntimeException("Set table name using dbAssert.table() first.");
        }
        final Object returnedValue = column_value("count(" + fieldName + ")");
        try {
            count = (Long) returnedValue;
        }
        catch (NumberFormatException e) {
            throw new RuntimeException("Returned result is not of numeric type.");
        }
        return count;
    }

    private void validateInput(final String selectOp) {
        if (dbSource == null) {
            throw new RuntimeException("Name of the source must be defined. Use dbAssert.sourceName()");
        }
        if (!selectOp.contains(".") && tableName == null) {
            throw new RuntimeException("Set ether tableName using dbAssert.table() or use notation: tableName.columnName for assertColumn() method");
        }
        // if it's not a count operation then conditions must not be empty
        if (selectOp.startsWith("count")) {
            conditions.put("1", 1);
        } else if (conditions.isEmpty()) {
            throw new RuntimeException("Set at least one condition clause using dbAssert.condition()");
        }

    }


    private Object runQuery(final String selectOperand) {
        validateInput(selectOperand);
        String tableName = this.tableName;
        if (tableName == null) {
            tableName = tableName(selectOperand);
        }
        final Object result;
        try {
            result = this.dbSource.select(selectOperand, tableName, makeCondtionClause(conditions));
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to initilize and run integration spec", e);
        }
        return result;

    }

    private String makeCondtionClause(final Map<String, Object> conditions) {
        final StringBuilder sb = new StringBuilder();
        int i = 0;
        for (final String key : conditions.keySet()) {
            final int condtionsNum = conditions.keySet().size();
            final Object value = conditions.get(key);
            if (value instanceof Integer) {
                sb.append(key + " = " + value + " ");
            } else {
                sb.append(key + " = '" + value + "' ");
            }
            if (i < condtionsNum - 1) {
                sb.append("AND ");
                i++;
            }
        }
        return sb.toString().trim();
    }

    private String fieldName(final String tableColumn) {
        return tableColumn.substring(tableColumn.indexOf(".") + 1);
    }

    private String tableName(final String tableColumn) {
        return tableColumn.substring(0, tableColumn.indexOf("."));
    }

    /*
   Only for testing purposes.
    */

    public class TestBackdoor {

        public String $fieldName(final String tableColumn) {
            return fieldName(tableColumn);
        }

        public String $tableName(final String tableColumn) {
            return tableName(tableColumn);
        }

        public String $makeConditionClause(final Map<String, Object> conditions) {
            return makeCondtionClause(conditions);
        }
    }

}
