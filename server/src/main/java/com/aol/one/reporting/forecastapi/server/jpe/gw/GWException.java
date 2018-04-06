/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.jpe.gw;

/**
 * Grid walk algorithm exceptions.
 * 
 * @author Copyright &copy; 2012 John Eldreth All rights reserved.
 */
public class GWException extends Exception {
   private static final long    serialVersionUID = 1L;

    /**
     * Default GWException.
     */
    public GWException() {
        super("A grid walk algorithm exception occurred.");
    }

    /**
     * GWException with a specified message.
     * 
     * @param message Exception message.
     */
    public GWException(
        String      message
    ) {
        super(message);
    }
}
