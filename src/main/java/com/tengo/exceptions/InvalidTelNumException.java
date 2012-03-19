/*
 * 
 * Copyright 2012 by Tengo, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information 
 * of Tengo, Inc.
 *
 * @author psm
 */
package com.tengo.exceptions;

public class InvalidTelNumException extends Exception {

    public InvalidTelNumException() {}

    public InvalidTelNumException(final String msg) {
        super(msg);
    }

    public InvalidTelNumException(final Throwable cause) {
        super(cause);
    }
}
