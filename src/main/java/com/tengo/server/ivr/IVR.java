/*
 * @author prasadm80@gmail.com
 */
package com.tengo.server.ivr;

import com.google.inject.ImplementedBy;

import com.tengo.beans.AccountInfo;

@ImplementedBy(com.tengo.server.ivr.impl.TropoServer.class)
public interface IVR {
    enum AuthType  { Deposit, Withdraw, Transfer, Balance, History };

    void newAccount(String callNum, String name, String xid);
    void authorize(AuthType t, AccountInfo a, String xid, String initiatorNum);
}
