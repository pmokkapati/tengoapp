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
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

/**
 * A protocol class. Service implementations allow protocols other than GWT
 * to be used to call onto the same service implementations.
 */
public abstract class  Protocol {
    /**
     * Function to decode a encoded request. 
     * @param servlet the RPC servlet
     * @param req the HttpServlet request
     * @return the decoded Request Object
     */
    public abstract Request decodeRequest(RPCServlet servlet, 
                HttpServletRequest req) throws IOException, ServletException;

    /**
     * Function to encode a response
     * @param res write response to 
     * @param m the method the call was made on
     * @param ret the response from the method to be encoded
     */
    public abstract void encodeResponseForSuccess(HttpServletResponse res,                      Method m, Object ret);
    /**
     * Function to encode a exception
     * @param res write response to 
     * @param m the method the call was made on
     * @param excep the exception to be encoded
     */
    public abstract void encodeResponseForFailure(HttpServletResponse res,
            Method m, Throwable excep);

    /**
     * Function to invoke the method and return encoded response.
     * @param res write response to 
     * @param target the object to execute the method on
     * @param m the method to execute
     * @param args the arguments to the method
     * @throws Exception any exceptions thrown by invoking the method
     */
    public void invokeAndEncodeResponse(HttpServletResponse res,
            Object target, Method m, Object args[]) throws Exception {
        Object ret = m.invoke(target, args);
        encodeResponseForSuccess(res, m, ret);
    }
}
