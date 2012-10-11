/*
 * @author prasadm80@gmail.com
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
