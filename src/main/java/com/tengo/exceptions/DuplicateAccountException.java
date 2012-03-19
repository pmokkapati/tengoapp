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

public class DuplicateAccountException extends Exception {

    public DuplicateAccountException() {}

    public DuplicateAccountException(final String msg) {
        super(msg);
    }

    public DuplicateAccountException(final Throwable cause) {
        super(cause);
    }
}
