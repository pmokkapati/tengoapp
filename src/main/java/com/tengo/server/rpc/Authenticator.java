/*
 * Copyright 2012 by Tengo, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information 
 * of Tengo, Inc.
 *
 * @author psm
 */
package com.tengo.server.rpc;

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;


/**
 * Interface for classes that implement security on GWT service methods.
 */
public interface Authenticator {
    /**
     * Method to determine if a method is executable by the remote caller
     * @param req the remote request - HttpServletRequest
     * @param m method to be executed on the service object
     * @param parameters array of parameter values
     * @return true if remote caller is allowed to execute the method. 
     *          otherwise false
     * @throws SecurityException if caller is not allowed to execute
     */
    public boolean authenticate(HttpServletRequest req, 
            Method m, Object parameters[] ) throws SecurityException;
}
