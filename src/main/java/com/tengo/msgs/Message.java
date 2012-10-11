/*
 * @author prasadm80@gmail.com
 */
package com.tengo.msgs;

import com.tengo.beans.AccountInfo;

public abstract class Message {

    public enum MsgType { NewAccount, Deposit, Withdraw, Transfer, Balance, 
            History };
    private MsgType _type;
    private String _xid=null;
    private AccountInfo.Language _language = AccountInfo.Language.English;

    protected Message(MsgType t) { 
        _type = t;
    }
    protected Message(MsgType t, AccountInfo.Language l) { 
        _type = t;
        _language = l;
    }

    public MsgType getType() { return _type; }

    public void setXid(String xid) { _xid = xid; }
    public String getXid() { return _xid; }

    public AccountInfo.Language getLanguage() { return _language; }
    public void setLanguage(AccountInfo.Language l) { _language = l; }
}
