/*
 * @author prasadm80@gmail.com
 */
package com.tengo.msgs;


public class BalanceMsg extends Message {
    public String _customerNum;
    public String _pin;

    /**
     * New Account message
     */
    public BalanceMsg(String from, String pin) {
        super(MsgType.Balance);
        _customerNum = from;
        _pin = pin;
    }

    public String getCustomerNum() { return _customerNum; }
    public void setCustomerNum(String s) { _customerNum = s; }

    public String getPin() { return _pin; }
    public void setPin(String pin) { _pin = pin; }
}        
