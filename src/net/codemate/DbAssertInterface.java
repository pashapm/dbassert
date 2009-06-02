/**
 * @(#) DbAssertInterface.java;
 * <p/>
 * Created on Apr 6, 2009
 * AUTHOR    ** Danil Glinenko
 * EMAIL     ** dglinenko@gmail.com
 * <p/>
 */

package net.codemate;

import java.util.Map;

//TODO: what did I create this interface for?
public interface DbAssertInterface {
    DbSource source (String sourceNameToUse, Class invokerClass);

    DbSource source(String sourceNameToUse);

    Condition table(final String table);

    Condition table();

    void assert_column(String fieldName, Object expectedValue);

    void assert_not_empty(String fieldName);

    void assert_count(Integer count);

    void assert_count_gt(String fieldName, Integer counter);

    void assert_count_lt(String fieldName, int counter);

    void assert_columns(Map<String, String> fieldValueMap);

    Object column_value(String fieldName);
}
