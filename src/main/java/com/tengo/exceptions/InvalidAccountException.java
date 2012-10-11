/*
 * @author prasadm80@gmail.com
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
