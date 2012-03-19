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
package com.tengo.msgs;

import com.tengo.beans.Account;

public abstract class Message {

    public enum MsgType { NewAccount, Deposit, Withdraw, Transfer, Balance, 
            History };
    private MsgType _type;
    private String _xid=null;
    private Account.Language _language = Account.Language.English;

    protected Message(MsgType t) { 
        _type = t;
    }
    protected Message(MsgType t, Account.Language l) { 
        _type = t;
        _language = l;
    }

    public MsgType getType() { return _type; }

    public void setXid(String xid) { _xid = xid; }
    public String getXid() { return _xid; }

    public Account.Language getLanguage() { return _language; }
    public void setLanguage(Account.Language l) { _language = l; }
}
