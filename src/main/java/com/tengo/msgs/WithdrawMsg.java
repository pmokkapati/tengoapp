/*
 * @author prasadm80@gmail.com
 */
package com.tengo.msgs;


public class WithdrawMsg extends Message {
    public String _agentNum;
    public String _agentPin;
    public String _customerNum;
    public double _amount;
    public String _customerPin=null;

    /**
     * New Account message
     */
    public WithdrawMsg(String agentNum, String agentPin, String customerNum,
                double amount, String pin) {
        super(MsgType.Withdraw);
        _agentNum = agentNum;
        _agentPin = agentPin;
        _customerNum = customerNum;
        _amount = amount;
        _customerPin = pin;
    }

    public String getAgentNum() { return _agentNum; }
    public void setAgentNum(String s) { _agentNum = s; }

    public String getAgentPin() { return _agentPin; }
    public void setAgentPin(String s) { _agentPin = s; }

    public String getCustomerNum() { return _customerNum; }
    public void setCustomerNum(String s) { _customerNum = s; }

    public String getCustomerPin() { return _customerPin; }
    public void setCustomerPin(String s) { _customerPin = s; }

    public double getAmount() { return _amount; }
    public void setAmount(double v) { _amount = v; }

}        
