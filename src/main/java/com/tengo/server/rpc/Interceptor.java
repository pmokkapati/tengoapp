/*
 * @author prasadm80@gmail.com
 */
package com.tengo.server.rpc;

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
/**
 * Interceptor interface. The methods are called by Service Implementation 
 * before a service method is called, after a service a method is called or
 * on service method execution exception
 */
public interface Interceptor {
    /**
     * Function called by Service implementation before a method is called
     * @param req the request
     * @param m the method on the service implementation
     * @param parameters the parameters to be passed to the method
     */
    public void before(HttpServletRequest req, Method m, Object parameters[]);
    /**
     * Function called by Service implementation after a method is called
     * @param req the request
     * @param m the method on the service implementation
     * @param parameters the parameters to be passed to the method
     */
    public void after(HttpServletRequest req, Method m, Object parameters[]);
    /**
     * Function called by Service implementation if an exception occurs
     * during method call. NOTE: after is not called if this method is called
     * @param req the request
     * @param m the method on the service implementation
     * @param parameters the parameters to be passed to the method
     * @param e the exception that occured
     */
    public void exception(HttpServletRequest req, Method m, Object parameters[],
            Exception e);
}
