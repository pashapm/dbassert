/**
 * @(#) DbAssertInterface.java;
 * <p/>
 * Created on Apr 6, 2009
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

package net.codemate;

import java.util.Map;

public interface DbAssertInterface {
    DbSource source (String sourceNameToUse, Class invokerClass);

    DbSource source(String sourceNameToUse);

//    Condition where(String param, Object value);
//
//    Condition where(String condition);
//
//    Condition where();

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
