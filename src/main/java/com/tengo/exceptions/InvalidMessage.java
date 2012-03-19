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

public class InvalidMessage extends Exception {

    public InvalidMessage() {}

    public InvalidMessage(final String msg) {
        super(msg);
    }

    public InvalidMessage(final Throwable cause) {
        super(cause);
    }
}
