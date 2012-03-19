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

public class InsufficientFundsException extends Exception {

    public InsufficientFundsException() {}

    public InsufficientFundsException(final String msg) {
        super(msg);
    }

    public InsufficientFundsException(final Throwable cause) {
        super(cause);
    }
}
