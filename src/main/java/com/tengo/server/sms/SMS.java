/*
 * 
 * Copyright 2012 by Tengo, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information 
 * of Tengo, Inc.
 *
 * @author psm
 */
package com.tengo.server.sms;

import com.google.inject.ImplementedBy;

@ImplementedBy(com.tengo.server.sms.impl.TropoServer.class)
public interface SMS {
    void sendMsg(String to, String msg);
}
