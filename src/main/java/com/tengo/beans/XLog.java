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
package com.tengo.beans;
import java.io.Serializable;
import java.util.Date;

import com.tengo.sqldb.*;

@Table(name="XLog")
public class XLog implements Serializable {
    public enum XType {
        New,
        Deposit,
        Withdraw,
        History,
        Balance,
        Chargeback,
        View,
        Update
    };
    public enum AccessMode {
        Sms,
        Web,
        Ivr,
        Agent
    };

    public enum Status {
        Committed,
        PendingAuth,
        Pending
    };


    private long _id;
    private String _xid;
    private Date _created;
    private long _loginId;
    private AccessMode _accessMode;
    private String _accessAddress;
    private XType _txnType;
    private long _accountId;
    private double _amount;
    private Status _status;

    @Id
    public long getId() { return _id; }
    public void setId(long id) { _id = id; }

    public String getXid() { return _xid; }
    public void setXid(String id) { _xid = id; }

    public Date getCreated() { return _created; }
    public void setCreated(Date d) { _created = d; }

    public long getLoginId() { return _loginId; }
    public void setLoginId(long id) { _loginId = id; }

    public AccessMode getAccessMode() { return _accessMode; }
    public void setAccessMode(AccessMode id) { _accessMode = id; }

    public String getAccessAddress() { return _accessAddress; }
    public void setAccessAddress(String v) { _accessAddress = v; }

    public XType getTxnType() { return _txnType; }
    public void setTxnType(XType typ) { _txnType = typ; }

    public long getAccountId() { return _accountId; }
    public void setAccountId(long id) { _accountId = id; }

    public double getAmount() { return _amount; }
    public void setAmount(double amt) { _amount = amt; }

    public Status getStatus() { return _status; }
    public void setStatus(Status s) { _status = s; }

}
