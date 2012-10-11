/*
 * @author prasadm80@gmail.com
 */
package com.tengo.msgs;


public class TransferMsg extends Message {
    public String _fromNum;
    public String _toNum;
    public double _amount;
    public String _fromPin=null;

    /**
     * New Account message
     */
    public TransferMsg(String fromNum, String toNum, double amount, 
            String pin) {
        super(MsgType.Transfer);
        _fromNum = fromNum;
        _toNum = toNum;
        _amount = amount;
        _fromPin = pin;
    }

    public String getFromNum() { return _fromNum; }
    public void setFromNum(String s) { _fromNum = s; }

    public String getToNum() { return _toNum; }
    public void setToNum(String s) { _toNum = s; }

    public String getFromPin() { return _fromPin; }
    public void setFromPin(String s) { _fromPin = s; }

    public double getAmount() { return _amount; }
    public void setAmount(double v) { _amount = v; }

}        
