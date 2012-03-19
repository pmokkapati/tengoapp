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
package com.tengo.server.sms.impl;

import java.util.HashMap;
import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

import com.google.inject.Singleton;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import com.tengo.exceptions.*;
import com.tengo.msgs.*;
import com.tengo.businesslogic.Bank;
import com.tengo.beans.*;
import com.tengo.server.sms.SMSParser;
import com.tengo.server.sms.SMS;
import com.tengo.server.ivr.IVR;

/**
 * Class to handle both incoming and outgoing SMS messages using Calista
 */
@Singleton
public class CalixtaServer extends HttpServlet implements SMS {
    protected Bank _bank;
    protected IVR _ivr;
    protected String _url = "http://www.calixtaondemand.com/Controller.php/__a/sms.send.remote.sa";
    protected String _cte = "38459";
    protected String _encpwd = "53081c9519ed5bc34c86ec9a169bb79fa4088e89d52035f3d0019ddaf3ebec08";
    protected final String _mtipo = "SMS";
    protected String _email = "pmokkapati@mytengo.com";
        
    @Inject
    protected void init(IVR ivr, Bank b) {
        _bank = b;
        _ivr = ivr;
    }
    /**
     * internal function to send a response to a incoming SMS message
     */
    public void sendResponse(HttpServletResponse res, String msg) 
            throws IOException, ServletException {
        ServletOutputStream strm = res.getOutputStream();
        strm.print(msg);
    }

    /**
     * Function to send a SMS message thru Calixta
     */
    public void sendMsg(String to, String msg) {
        HttpURLConnection conn = null;
        String param = "cte=" + URLEncoder.encode(_cte)
                + "&encpwd=" + URLEncoder.encode(_encpwd)
                + "&email=" + URLEncoder.encode(_email)
                + "&numtel=" + URLEncoder.encode(to)
                + "&mtipo=" + URLEncoder.encode(_mtipo)
                + "&msg=" + URLEncoder.encode(msg);
        System.out.println("Param: '" + param + "'");
        try {
            conn = (HttpURLConnection)(new URL(_url).openConnection());
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", 
                    "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length",
                Integer.toString(param.getBytes().length));
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(param);
            out.flush();
            out.close();
            InputStream in = conn.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String line;
            StringBuffer response = new StringBuffer();
            while ( (line=r.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            r.close();
            System.out.println("Response: " + response.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if ( conn != null) {
                conn.disconnect();
            }
        }
    }


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) 
            throws ServletException, IOException {
        String from = req.getParameter("phone");
        String message = req.getParameter("message");
        System.out.println("Calixta: Got SMS message from '"  + from  
            + "' msg:'" + message);
        Message m=null;
        try {
            m = SMSParser.parse(from, message);
        }
        catch (InvalidMessage e) {
            sendResponse(res, "Invalid SMS message. Please contact Tengo support "
                + " at XXXX for help.");
            return;
        }
        switch (m.getType()) {
            case NewAccount:
                processNewAccount(from, (NewAccountMsg)m, res);
                break;
            case Deposit:
                processDeposit(from, (DepositMsg)m, res);
                break;
            case Withdraw:
                processWithdraw(from, (WithdrawMsg)m, res);
                break;
            case Transfer:
                processTransfer(from, (TransferMsg)m, res);
                break;
            case Balance:
                processBalance(from, (BalanceMsg)m, res);
                break;
            default:
                sendResponse(res,
                    "Invalid SMS message. Please contact Tengo support "
                    + " at XXXX for help.");
        }
    }

    /**
     * Function to process a new account request
     * @param from the mobile number that sent the request
     * @param msg the new account request
     */
    protected void processNewAccount(String from, NewAccountMsg msg,
            HttpServletResponse res) throws ServletException, IOException {
        System.out.println("Inside processNewAccount. From '"
            + from + "', name: '" + msg.getName() + "' ");
        // Check to see if the given telephone number is already tied to 
        // an account
        try {
            Account a = _bank.getAccount(msg.getCustomerNum());
            if ( a != null) {
                sendResponse(res,"Your number " + msg.getCustomerNum() 
                    + " is already linked to an existing tengo account");
                return;
            }
            String xid = _bank.queueNewAccount(Account.AccountType.Customer,
                msg.getCustomerNum(), XLog.AccessMode.Sms);
            _ivr.newAccount(msg.getCustomerNum(), msg.getName(), xid);
        }
        catch (Exception e) {
            System.out.println("Unexpected exception processing newaccount "
                + "request from " + from + "");
            e.printStackTrace();
            sendResponse(res,"Error processing your request. Please contact tengo "
                + "support at XXX");
        }
    }
    /**
     * Function to process a deposit request
     * @param from the mobile number that sent the request
     * @param msg the deposit request
     */
    protected void processDeposit(String from, DepositMsg msg,
            HttpServletResponse res) throws ServletException, IOException {
        try {
            Account agent = _bank.getAccount(msg.getAgentNum());
            if ( agent == null) {
                sendResponse(res,"No Tengo Agent account tied to " 
                        + msg.getAgentNum() + ". Deposit not processed.");
                return;
            }
            if ( msg.getAgentPin() == null || ! _bank.matchesPin(agent, 
                    msg.getAgentNum(), msg.getAgentPin()) ) {
                sendResponse(res,"Invalid Agent pin. Deposit not processed");
                return;
            }
            Account customer = _bank.getAccount(msg.getCustomerNum());
            if ( customer == null) {
                sendResponse(res,"No Tengo account tied to " 
                        + msg.getCustomerNum() + ". Deposit not processed.");
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
                   sendResponse(res,"Invalid customer pin. Deposit not processed");
                }
                else {
                    String xid = _bank.deposit(agent, customer, msg.getAmount(),
                        false);
                    sendResponse(res,msg.getAmount() + " has been deposited into "
                        + customer.getName() + "(" + customer.getTelNum()
                        + ") account. Your (Agent) balance is " + agent.getBalance() );
                    sendMsg(customer.getTelNum(),
                        (msg.getAmount() + " has been deposited into "
                        + " your account. Your balance is " 
                        + customer.getBalance()) );
                }
            }
        }
        catch (Exception e) {
            System.out.println("Unexpected exception processing deposit "
                + "request from " + from);
            e.printStackTrace();
            sendResponse(res,"Error processing your request. Please contact Tengo "
                + "support at XXX");
        }
    }
    /**
     * Function to process a withdrawal request
     * @param from the mobile number that sent the request
     * @param msg the withdraw message
     */
    protected void processWithdraw(String from, WithdrawMsg msg,
            HttpServletResponse res) throws ServletException, IOException {
        try {
            Account agent = _bank.getAccount(msg.getAgentNum());
            if ( agent == null) {
                sendResponse(res,"No Tengo Agent account tied to " 
                        + msg.getAgentNum() + ". Withdrawal not processed.");
                return;
            }
            if ( msg.getAgentPin() == null || ! _bank.matchesPin(agent, 
                    msg.getAgentNum(), msg.getAgentPin()) ) {
                sendResponse(res,"Invalid Agent pin. Withdrawal not processed");
                return;
            }
            Account customer = _bank.getAccount(msg.getCustomerNum());
            if ( customer == null) {
                sendResponse(res,"No Tengo account tied to " 
                        + msg.getCustomerNum() + ". Withdrawal not processed.");
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
                   sendResponse(res,"Invalid customer pin. Withdrawal not processed");
                }
                else {
                    String xid = _bank.deposit(agent, customer, msg.getAmount(),
                        false);
                    sendResponse(res,msg.getAmount() + " has been withdrawn from "
                        + customer.getName() + "(" + customer.getTelNum()
                        + ") account. Your (Agent) balance is " 
                        + agent.getBalance());
                    sendMsg(customer.getTelNum(), 
                        (msg.getAmount() + " has been withdrawn from "
                            + " your account. Your balance is '" 
                            + customer.getBalance()) );
                }
            }
        }
        catch (Exception e) {
            System.out.println("Unexpected exception processing withdraw "
                + "request from " + from );
            e.printStackTrace();
            sendResponse(res,"Error processing your request. Please contact Tengo "
                + "support at XXX");
        }
    }
    /**
     * Function to process a transfer request
     * @param telNum the mobile number that sent the request
     * @param msg the transfer message
     */
    protected void processTransfer(String telNum, TransferMsg msg,
            HttpServletResponse res) throws ServletException, IOException {
        try {
            Account from = _bank.getAccount(msg.getFromNum());
            if ( from == null) {
                sendResponse(res,"No Account tied to "
                        + msg.getFromNum() + ". Transfer not processed.");
                return;
            }
            Account to = _bank.getAccount(msg.getToNum());
            if ( to == null) {
                sendResponse(res,"No Tengo account tied to " 
                        + msg.getToNum() + ". Transfer not processed.");
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
                   sendResponse(res,"Invalid pin number. Transfer not processed");
                }
                else {
                    String xid = _bank.transfer(from, to, msg.getAmount(), 
                        false);
                    sendResponse(res,msg.getAmount() + " has been transfered "
                        + " to " + to.getName() + "(" 
                        + msg.getToNum() + ") account. Your balance is '" 
                        + from.getBalance());
                    sendMsg(msg.getToNum(), 
                        (msg.getAmount() + " has been deposited "
                        + " into your account by " 
                        + from.getName() + "(" + from.getTelNum() 
                        + "). Your balance is " + to.getBalance()));
                }
            }
        }
        catch (InsufficientFundsException e) {
            sendResponse(res,"Insufficient funds. Transfer not processed");
        }
        catch (Exception e) {
            System.out.println("Unexpected exception processing transfer "
                + "request from " + telNum);
            e.printStackTrace();
            sendResponse(res,"Error processing your request. Please contact Tengo "
                + "support at XXX");
        }
    }
    /**
     * Function to process a balance request
     * @param from the mobile number that sent the request
     * @param msg the balance message
     */
    protected void processBalance(String from, BalanceMsg msg,
            HttpServletResponse res) throws ServletException, IOException {
        try {
            Account a = _bank.getAccount(msg.getCustomerNum());
            if ( a == null) {
                sendResponse(res,"No Account tied to " + msg.getCustomerNum());
                return;
            }
            sendResponse(res,"Your balance is " + a.getBalance() );
        }
        catch (Exception e) {
            System.out.println("Unexpected exception processing balance "
                + "request from " + from );
            e.printStackTrace();
            sendResponse(res,"Error processing your request. Please contact Tengo "
                + "support at XXX");
        }
    }
}
