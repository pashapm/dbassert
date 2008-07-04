/**
 * @(#) Main.java;
 * <p/>
 * Created on Jul 4, 2008
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


public class Main {
    private static final String VERSION = "0.8.6";

    private Main() {
    }

    public static void main(final String[] args) {
        for (final String arg : args) {
            if (arg.equals("-v")){
                System.out.println("DbAssert " + VERSION);
            }
        }
   }
}
