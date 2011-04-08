/**
 * @(#) Fixture.java;
 * <p/>
 * Created on Mar 23, 2008
 * AUTHOR    ** Danil Glinenko
 * EMAIL     ** dglinenko@gmail.com
 * <p/>
 *
 * Copyright (C) 2008 Danil Glinenko. All rights reserved.
 */

package net.codemate;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Class representing Fixture - file in YAML format.
 * It extended from HashMap and represent key:value pairs from YAML file.
 */
public class Fixture extends LinkedHashMap {
    private String name;

    /**
     * Default constructor for the Fixture
     *
     * @param name - name of the fixture which is actually equals table name to which this fixture will be applied to.
     */
    public Fixture(final String name) {
        this.name = name;
    }

    /**
     * Sets the name of the fixture.
     *
     * @param name name of the fixture.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns values from the fixture Map.
     *
     * @param name - the key whose associated value to be returned.
     * @return the value to which the specified key is mapped from YAML, or null if this fixture contains no mapping for the key
     *         Returned value can be of HashMap type as well.
     */
    public Object get(final String name) {
        final Object obj = super.get(name);
        if (obj instanceof Map) {
            final Map map = (Map) obj;
            final Fixture fix = new Fixture(name);
            fix.putAll(map);
            return fix;
        }
        return obj;
    }


    /**
     * Returns name of the fixture created.
     *
     * @return - fixture name.
     */
    public String getName() {
        return this.name;
    }
}