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

/**
 * Class to hold remote client request. Used by protocol handlers to 
 */
public class Request {
    private Method _method=null;
    private Object _parameters[] = null;

    public Request(Method m, Object parameters[] ) {
        _method = m;
        _parameters = parameters;
    }
    public Request() {}

    public Method getMethod() { return _method; }
    public void setMethod(Method m) { _method = m; }

    public void setParameters(Object param[]) { _parameters = param; }
    public Object[] getParameters() { return _parameters; }
}
