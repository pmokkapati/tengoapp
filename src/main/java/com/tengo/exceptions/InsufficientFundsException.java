/*
 * @author prasadm80@gmail.com
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
