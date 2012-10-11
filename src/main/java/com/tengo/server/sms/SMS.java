/*
 * @author prasadm80@gmail.com
 */
package com.tengo.server.sms;

import com.google.inject.ImplementedBy;

@ImplementedBy(com.tengo.server.sms.impl.TropoServer.class)
public interface SMS {
    void sendMsg(String to, String msg);
}
