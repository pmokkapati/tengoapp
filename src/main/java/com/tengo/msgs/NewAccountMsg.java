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


public class NewAccountMsg extends Message {
    public String _customerNum;
    public String _name;

    /**
     * New Account message
     */
    public NewAccountMsg(String from, String name) {
        super(MsgType.NewAccount);
        _customerNum = from;
        _name = name;
    }

    public String getCustomerNum() { return _customerNum; }
    public void setCustomerNum(String s) { _customerNum = s; }

    public String getName() { return _name; }
    public void setName(String s) { _name = s; }

}        
