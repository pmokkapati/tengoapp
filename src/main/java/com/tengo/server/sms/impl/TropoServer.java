/*
 * @author prasadm80@gmail.com
 */
package com.tengo.server.sms.impl;

import java.util.HashMap;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.voxeo.tropo.Tropo;
import com.voxeo.tropo.enums.Network;
import com.voxeo.tropo.TropoSession;

import com.tengo.exceptions.*;
import com.tengo.msgs.*;
import com.tengo.businesslogic.Bank;
import com.tengo.beans.*;
import com.tengo.server.sms.SMSParser;
import com.tengo.server.sms.SMS;
import com.tengo.server.ivr.IVR;

/**
 * Class to handle both incoming and outgoing SMS messages.
 */
@Singleton
public class TropoServer extends HttpServlet implements SMS {
    protected Bank _bank;
    protected IVR _ivr;
    protected String _token;
        
    @Inject
    protected void init(@Named("TropoSMSToken") String token,
            IVR ivr, Bank b) {
        _token = token;
        _bank = b;
        _ivr = ivr;
    }

    public void sendMsg(String to, String msg) {
        Tropo t = new Tropo();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("Reason", "sendMsg");
        params.put("TelNum", to);
        params.put("Message", msg);
        t.launchSession(_token, params);
    }

    /**
     * Function to process the request if request is in response to a session
     * launched by this class
     * @param res HttpServletResponse object
     * @param t The Tropo Object
     * @param s The Tropo Sesssion
     * @return true if call is meant for this object. If it is a incoming SMS
     * request then false;
     */
    protected boolean consumeOutbound(HttpServletResponse res, Tropo t, 
            TropoSession s) {
        HashMap<String, String> params = s.getParameters();
        String reason = null;
        if ( params == null || (reason = params.get("Reason")) == null) {
            return false;
        }
        if ( reason.equals("sendMsg") ) {
            t.message(params.get("TelNum"), Network.SMS,
                "17788001761").say(params.get("Message"));
            t.render(res);
        }
        else {
            System.out.println("Unexpected reason '" + reason + "'");
        }
        return true;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) 
            throws ServletException, IOException {
        // Check to see if session started because of calls from tengo 
        // server.
        Tropo t = new Tropo();
        TropoSession s= t.session(req);
        if ( !consumeOutbound(res, t, s) ) {
            processSMS(res, t, s);
        }
    }

    /**
     * Function to process a new account request
     * @param telNum the mobile number that sent the request
     * @param to the number that received the request
     * @param msg the new account request
     * @param t Tropo Object
     * @param res HttpServlet response used to respond back to sms
     */
    protected void processNewAccount(String from, String to, 
            NewAccountMsg msg, Tropo t, HttpServletResponse res) {
        System.out.println("Inside processNewAccount. From '"
            + from + "', name: '" + msg.getName() + "' ");
        // Check to see if the given telephone number is already tied to 
        // an account
        try {
            Account a = _bank.getAccount(msg.getCustomerNum());
            if ( a != null) {
                t.say("Your number " + msg.getCustomerNum() 
                    + " is already linked to an existing tengo account");
                t.render(res);
                return;
            }
            String xid = _bank.queueNewAccount(Account.AccountType.Customer,
                msg.getCustomerNum(), XLog.AccessMode.Sms);
            _ivr.newAccount(msg.getCustomerNum(), msg.getName(), xid);
            t.hangup();
            t.render(res);
        }
        catch (Exception e) {
            System.out.println("Unexpected exception processing newaccount "
                + "request from " + from + "");
            e.printStackTrace();
            t.say("Error processing your request. Please contact tengo "
                + "support at XXX");
            t.render(res);
        }
    }
    /**
     * Function to process a deposit request
     * @param telNum the mobile number that sent the request
     * @param to the number that received the request
     * @param msg the deposit request
     * @param t Tropo Object
     * @param res HttpServlet response used to respond back to sms
     */
    protected void processDeposit(String telNum, String to, 
            DepositMsg msg, Tropo t, HttpServletResponse res) {
        try {
            Account agent = _bank.getAccount(msg.getAgentNum());
            if ( agent == null) {
                t.say("No Tengo Agent account tied to " 
                        + msg.getAgentNum() + ". Deposit not processed.");
                t.render(res);
                return;
            }
            if ( msg.getAgentPin() == null || ! _bank.matchesPin(agent, 
                    msg.getAgentNum(), msg.getAgentPin()) ) {
                t.say("Invalid Agent pin. Deposit not processed");
                t.render(res);
            }
            Account customer = _bank.getAccount(msg.getCustomerNum());
            if ( customer == null) {
                t.say("No Tengo account tied to " 
                        + msg.getCustomerNum() + ". Deposit not processed.");
                t.render(res);
                return;
            }
            if ( msg.getCustomerPin() == null) {
                String xid = _bank.deposit(agent, customer, msg.getAmount(), 
                    true);
                _ivr.authorize(IVR.AuthType.Deposit, customer, xid, 
                        msg.getAgentNum());
            }
            else {
                if (!_bank.matchesPin(customer, customer.getTelNum(),
                        msg.getCustomerPin()) ) {
                   t.render(res);
                   t.say("Invalid customer pin. Deposit not processed");
                }
                else {
                    String xid = _bank.deposit(agent, customer, msg.getAmount(),
                        false);
                    t.say(msg.getAmount() + " has been deposited into "
                        + customer.getName() + "(" + customer.getTelNum()
                        + ") account. Your (Agent) balance is " + agent.getBalance() );
                    sendMsg(customer.getTelNum(),
                        (msg.getAmount() + " has been deposited into "
                        + " your account. Your balance is " 
                        + customer.getBalance()) );
                }
                t.render(res);
            }
        }
        catch (Exception e) {
            System.out.println("Unexpected exception processing deposit "
                + "request from " + telNum);
            e.printStackTrace();
            t.say("Error processing your request. Please contact Tengo "
                + "support at XXX");
            t.render(res);
        }
    }
    /**
     * Function to process a withdrawal request
     * @param telNum the mobile number that sent the request
     * @param to the number that received the request
     * @param msg the withdraw message
     * @param t Tropo Object
     * @param res HttpServlet response used to respond back to sms
     */
    protected void processWithdraw(String telNum, String to, 
            WithdrawMsg msg, Tropo t, HttpServletResponse res) {
        try {
            Account agent = _bank.getAccount(msg.getAgentNum());
            if ( agent == null) {
                t.say("No Tengo Agent account tied to " 
                        + msg.getAgentNum() + ". Withdrawal not processed.");
                t.render(res);
                return;
            }
            if ( msg.getAgentPin() == null || ! _bank.matchesPin(agent, 
                    msg.getAgentNum(), msg.getAgentPin()) ) {
                t.say("Invalid Agent pin. Withdrawal not processed");
                t.render(res);
            }
            Account customer = _bank.getAccount(msg.getCustomerNum());
            if ( customer == null) {
                t.say("No Tengo account tied to " 
                        + msg.getCustomerNum() + ". Withdrawal not processed.");
                t.render(res);
                return;
            }
            if ( msg.getCustomerPin() == null) {
                String xid = _bank.withdraw(agent, customer, msg.getAmount(), 
                    true);
                _ivr.authorize(IVR.AuthType.Withdraw, customer, xid,
                    msg.getAgentNum());
            }
            else {
                if (!_bank.matchesPin(customer, customer.getTelNum(),
                        msg.getCustomerPin()) ) {
                   t.render(res);
                   t.say("Invalid customer pin. Withdrawal not processed");
                }
                else {
                    String xid = _bank.deposit(agent, customer, msg.getAmount(),
                        false);
                    t.say(msg.getAmount() + " has been withdrawn from "
                        + customer.getName() + "(" + customer.getTelNum()
                        + ") account. Your (Agent) balance is " 
                        + agent.getBalance());
                    sendMsg(customer.getTelNum(), 
                        (msg.getAmount() + " has been withdrawn from "
                            + " your account. Your balance is '" 
                            + customer.getBalance()) );
                }
                t.render(res);
            }
        }
        catch (Exception e) {
            System.out.println("Unexpected exception processing withdraw "
                + "request from " + telNum );
            e.printStackTrace();
            t.say("Error processing your request. Please contact Tengo "
                + "support at XXX");
            t.render(res);
        }
    }
    /**
     * Function to process a transfer request
     * @param telNum the mobile number that sent the request
     * @param toNum the number that received the request
     * @param msg the transfer message
     * @param t Tropo Object
     * @param res HttpServlet response used to respond back to sms
     */
    protected void processTransfer(String telNum, String toNum, 
            TransferMsg msg, Tropo t, HttpServletResponse res) {
        try {
            Account from = _bank.getAccount(msg.getFromNum());
            if ( from == null) {
                t.say("No Account tied to "
                        + msg.getFromNum() + ". Transfer not processed.");
                t.render(res);
                return;
            }
            Account to = _bank.getAccount(msg.getToNum());
            if ( to == null) {
                t.say("No Tengo account tied to " 
                        + msg.getToNum() + ". Transfer not processed.");
                t.render(res);
                return;
            }
            if ( msg.getFromPin() == null) {
                String xid = _bank.transfer(from, to, msg.getAmount(), 
                    true);
                _ivr.authorize(IVR.AuthType.Transfer, from, xid,
                        msg.getFromNum());
            }
            else {
                if (!_bank.matchesPin(from, from.getTelNum(),
                        msg.getFromPin()) ) {
                   t.say("Invalid pin number. Transfer not processed");
                }
                else {
                    String xid = _bank.transfer(from, to, msg.getAmount(), 
                        false);
                    t.say(msg.getAmount() + " has been transfered "
                        + " to " + to.getName() + "(" 
                        + msg.getToNum() + ") account. Your balance is '" 
                        + from.getBalance());
                    sendMsg(msg.getToNum(), 
                        (msg.getAmount() + " has been deposited "
                        + " into your account by " 
                        + from.getName() + "(" + from.getTelNum() 
                        + "). Your balance is " + to.getBalance()));
                }
                t.render(res);
            }
        }
        catch (InsufficientFundsException e) {
            t.say("Insufficient funds. Transfer not processed");
            t.render(res);
        }
        catch (Exception e) {
            System.out.println("Unexpected exception processing transfer "
                + "request from " + telNum);
            e.printStackTrace();
            t.say("Error processing your request. Please contact Tengo "
                + "support at XXX");
            t.render(res);
        }
    }
    /**
     * Function to process a balance request
     * @param telNum the mobile number that sent the request
     * @param toNum the number that received the request
     * @param msg the balance message
     * @param t Tropo Object
     * @param res HttpServlet response used to respond back to sms
     */
    protected void processBalance(String telNum, String toNum, 
            BalanceMsg msg, Tropo t, HttpServletResponse res) {
        try {
            Account a = _bank.getAccount(msg.getCustomerNum());
            if ( a == null) {
                t.say("No Account tied to " + msg.getCustomerNum());
                t.render(res);
                return;
            }
            t.say("Your balance is " + a.getBalance() );
            t.render(res);
        }
        catch (Exception e) {
            System.out.println("Unexpected exception processing balance "
                + "request from " + telNum );
            e.printStackTrace();
            t.say("Error processing your request. Please contact Tengo "
                + "support at XXX");
            t.render(res);
        }
    }

    protected void processSMS(HttpServletResponse res, Tropo t, 
            TropoSession s) throws IOException {
        String from = s.getFrom().getId();
        String to = s.getTo().getId();
        System.out.println("Got SMS message from '"  + from  + "' to '"
            + to + "' msg:'" + s.getInitialText());
        Message m=null;
        try {
            m = SMSParser.parse(from, s.getInitialText());
        }
        catch (InvalidMessage e) {
            t.say("Invalid SMS message. Please contact Tengo support "
                + " at XXXX for help.");
            t.render(res);
            return;
        }
        switch (m.getType()) {
            case NewAccount:
                processNewAccount(from, to, (NewAccountMsg)m, t, res);
                break;
            case Deposit:
                processDeposit(from, to, (DepositMsg)m, t, res);
                break;
            case Withdraw:
                processWithdraw(from, to, (WithdrawMsg)m, t, res);
                break;
            case Transfer:
                processTransfer(from, to, (TransferMsg)m, t, res);
                break;
            case Balance:
                processBalance(from, to, (BalanceMsg)m, t, res);
                break;
            default:
                t.say("Invalid SMS message. Please contact Tengo support "
                    + " at XXXX for help.");
                t.render(res);
        }
    }
        
}
