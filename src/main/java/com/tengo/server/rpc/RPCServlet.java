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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import com.google.inject.Inject;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.client.rpc.SerializationException;

/**
 * Base class for all RPC services. Default implementation is GWT RPC
 * However provides a framework for other protocols as well.
 */
@SuppressWarnings("serial")
public class RPCServlet extends RemoteServiceServlet {
    private ProtocolProvider _protocolProvider;
    private Authenticator    _authenticator;
    private Interceptor      _interceptor;

    protected static final String GWT_CONTENT_TYPE = "text/x-gwt-rpc";

    @Inject
    protected void init(ProtocolProvider p, Authenticator a,
            Interceptor i) {
        _protocolProvider = p;
        _authenticator = a;
        _interceptor = i;
    }
    protected RPCServlet() {}


    /**
     * Override service call so we can let Other protocol requests to be 
     * parsed/Processed 
     * differently
     * @param req the incoming request 
     * @param res the outgoing response
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
                throws ServletException, IOException {
        if ( req.getContentType().equals(GWT_CONTENT_TYPE) ) { // GWT RPC
            super.service(req, res);
            return;
        }
        Protocol p = null;
        if ( (p = _protocolProvider.getProtocol(req.getContentType(), req)) 
                == null) {
            // No such protocol
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        processProtocol(p, req, res);
    }
    /**
     * Function to authenticate the call. If any authenticators injected
     * calls them otherwise returns true.
     * @param req the request
     * @param res the response
     * @param p the protocol
     * @param m the method to be invoked
     * @param parameters the parameters to be passed to the method
     * @return true is authentication worked, false otherwise.
     */
     protected boolean authenticate(HttpServletRequest req, 
                HttpServletResponse res, Protocol p, Method m,
                Object[] parameters) throws Exception {
        if ( _authenticator == null) {
            return true;
        }
        return _authenticator.authenticate(req, m, parameters);
    }

    /**
     * Function called before the method invocation. If any interceptor is 
     * installed the interceptor's before is called. Otherwise a noop.
     * @param req the request
     * @param m the method to be invoked
     * @param parameters the parameters to be passed to the method
     */
    protected void beforeInvoke(HttpServletRequest req, Method m, 
                Object parameters[]) {
        if ( _interceptor != null) {
            _interceptor.before(req, m, parameters);
        }
    }
    /**
     * Function called after the method invocation. If any interceptor is 
     * installed the interceptor's after is called. Otherwise a noop.
     * @param req the request
     * @param m the method to be invoked
     * @param parameters the parameters to be passed to the method
     */
    protected void afterInvoke(HttpServletRequest req, Method m, 
                Object parameters[]) {
        if ( _interceptor != null) {
            _interceptor.after(req, m, parameters);
        }
    }
    /**
     * Function called if the method invocation throws an exception.
     * If any interceptor is installed the interceptor's exception method is
     * called. Note: This method will be called only if before was called.
     * @param req the request
     * @param m the method to be invoked
     * @param parameters the parameters to be passed to the method
     * @param e the exception thrown by the Method invocation
     */
    protected void exceptionOnInvoke(HttpServletRequest req, Method m, 
                Object parameters[], Exception e) {
        if ( _interceptor != null) {
            _interceptor.exception(req, m, parameters, e);
        }
    }

    /**
     * Function to process protocols other than GWT requests
     * @param p the protocol to be used
     * @param req the request
     * @param res the response
     */
    protected void processProtocol(Protocol p, HttpServletRequest req,
            HttpServletResponse res) {
        boolean beforeCalled = false;
        Method m=null;
        Object parameters[] = null;
        try {
            Request r = p.decodeRequest(this, req);
            parameters = r.getParameters();
            m = r.getMethod();
            if ( authenticate(req, res, p, m, parameters) ) {
                beforeInvoke(req, m, parameters);
                beforeCalled = true;
                p.invokeAndEncodeResponse(res, this, m, parameters);
                afterInvoke(req, m, parameters);
            }
        }
        catch(Exception e) {
            if (beforeCalled) {
                exceptionOnInvoke(req, m, parameters, e);
            }
            e.printStackTrace();
            p.encodeResponseForFailure(res, m, e);
        }
    }

    /**
     * Override processCall for GWT RPC to execute intercept and authenticator
     * hanlders
     * @param p the protocol to be used
     * @param req the request
     * @param res the response
     */
     @Override
     public String processCall(String payload) throws SerializationException {
        boolean beforeCalled = false;
        Object parameters[] = null;
        Method m = null;
        HttpServletRequest req= getThreadLocalRequest();
        HttpServletResponse res= getThreadLocalResponse();
        String ret=null;
        try {
            RPCRequest r = RPC.decodeRequest(payload, getClass(), this);
            parameters = r.getParameters();
            m = r.getMethod();
            if ( authenticate(req, res, null, m, parameters) ) {
                beforeInvoke(req, m, parameters);
                beforeCalled = true;
                ret = RPC.invokeAndEncodeResponse(this, 
                    m, parameters, r.getSerializationPolicy(), r.getFlags());
                afterInvoke(req, m, parameters);
            }
        }
        catch(Exception e) {
            if (beforeCalled) {
                exceptionOnInvoke(req, m, parameters, e);
            }
            e.printStackTrace();
            ret = RPC.encodeResponseForFailure(m, e);
        }
        return ret;
    }
}
