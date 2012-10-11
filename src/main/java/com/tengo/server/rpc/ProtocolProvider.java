/*
 * @author prasadm80@gmail.com
 */
package com.tengo.server.rpc;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

/**
 * Protocol Provider interface. Used to get the protocol implementation
 * given the request, if protocol is other than GWT.
 */
public interface ProtocolProvider {
    public Protocol getProtocol(String contentType, HttpServletRequest req);
}
