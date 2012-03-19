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

public class InvalidTransaction extends Exception {

    public InvalidTransaction() {}

    public InvalidTransaction(final String msg) {
        super(msg);
    }

    public InvalidTransaction(final Throwable cause) {
        super(cause);
    }
}
