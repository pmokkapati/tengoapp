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

import com.tengo.sqldb.*;

@Table(name="Login")
public class Login implements Serializable {
    public enum UserType {AccountUser, Support };
        
    private long _loginId;
    private long _accountId;
    private UserType  _userType=UserType.Support;
    private String _userid;
    private String _password;

    @Id
    public long getLoginId() { return _loginId; }
    public void setLoginId(long id) { _loginId = id; }

    public long getAccountId() { return _accountId; }
    public void setAccountId(long id) { _accountId = id; }

    public UserType getUserType() { return _userType; }
    public void setUserType(UserType id) { _userType = id; }

    public String getUserid() { return _userid; }
    public void setUserid(String v) { _userid = v; }

    public String getPassword() { return _password; }
    public void setPassword(String v) { _password = v; }

}
