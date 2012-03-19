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

public class InvalidAccountException extends Exception {

    public InvalidAccountException() {}

    public InvalidAccountException(final String msg) {
        super(msg);
    }

    public InvalidAccountException(final Throwable cause) {
        super(cause);
    }
}
