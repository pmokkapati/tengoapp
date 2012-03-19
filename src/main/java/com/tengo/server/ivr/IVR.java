/*
 * 
 * Copyright 2011 by Tengo, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information 
 * of Tengo, Inc.
 *
 * @author psm
 */
package com.tengo.server.ivr;

import com.google.inject.ImplementedBy;

import com.tengo.beans.Account;

@ImplementedBy(com.tengo.server.ivr.impl.TropoServer.class)
public interface IVR {
    enum AuthType  { Deposit, Withdraw, Transfer, Balance, History };

    void newAccount(String callNum, String name, String xid);
    void authorize(AuthType t, Account a, String xid, String initiatorNum);
}
