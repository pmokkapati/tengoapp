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
package com.tengo.businesslogic;

import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.tengo.sqldb.*;
import com.tengo.beans.Account;
import com.tengo.beans.Login;
import com.tengo.beans.XLog;
import com.tengo.exceptions.*;
/**
 * Bank class
 */
@Singleton
public class Bank {
    @Inject DBManager _mgr;

    private final long _startTime = Calendar.getInstance().getTimeInMillis();
    private AtomicLong _counter = new AtomicLong(1);

    /**
     * Function to generate a unique transactionid
     * TODO: May want to add host name as well.
     */
    protected String generateXid() { 
        return Long.toHexString(_startTime) + "-" + _counter.getAndIncrement();
    }

    /**
     * Internal function to log a transaction
     */
    @Transactional
    protected void log(String xid, 
            XLog.XType type,
            XLog.AccessMode accessMode, String accessAddress,
            long accountID, double amount, XLog.Status s) 
            throws DBException {
        XLog t = new XLog();
        t.setXid(xid);
        t.setCreated(new Date());
        t.setAccessMode(accessMode);
        t.setAccessAddress(accessAddress);
        t.setAccountId(accountID);
        t.setTxnType(type);
        t.setAmount(amount);
        t.setStatus(s);
        _mgr.insert(t);
    }
    /**
     * Function to return a pending transaction.
     * @param telNum the telephone number
     * @return list of records pending
     * @throws DBException
     * @throws InvalidTelNumException
     */
    public List<XLog> getPending(String xid) throws DBException,
            InvalidTransaction {
        return _mgr.select(XLog.class, "select * from XLog where xid='"
            + xid + "' and status != " + XLog.Status.Committed.ordinal());
    }
    /**
     * Function to return a pending auth transaction.
     * @param telNum the telephone number
     * @return the list of records pending auth for the given xid
     * @throws DBException
     * @throws InvalidTelNumException
     */
    public XLog getPendingAuth(XLog.XType typ, String xid) 
            throws DBException, InvalidTransaction {
        return _mgr.get(XLog.class, 
            "select * from XLog where xid="
            + xid + " and status = " + XLog.Status.PendingAuth.ordinal()
            + " and txnType = " + typ.ordinal());
    }
    /**
     * Internal function to do a tranfer
     */
    @Transactional
    protected void transfer(String xid, Account fromAccount, Account toAccount, 
            double amount, XLog.AccessMode accessMode, 
            String accessAddress, XLog.Status fromStatus, 
            XLog.Status toStatus) throws DBException {
        if ( fromStatus == XLog.Status.Committed) {
            fromAccount.setBalance(fromAccount.getBalance()-amount);
        }
        if ( toStatus == XLog.Status.Committed) {
            toAccount.setBalance(toAccount.getBalance() + amount);
        }
        log(xid, XLog.XType.Withdraw, accessMode,
            accessAddress, fromAccount.getId(), -amount, fromStatus);
        log(xid, XLog.XType.Deposit, accessMode,
            accessAddress, toAccount.getId(), amount, toStatus);
    }
    /**
     * Function to queue a new account request
     */
    @Transactional
    public String queueNewAccount(Account.AccountType accType, 
            String telNum, XLog.AccessMode m) throws DBException,
                InvalidTelNumException, DuplicateAccountException {
        // Check to see if account already exists.
        if ( getAccount(telNum) != null) {
            throw new DuplicateAccountException("Account for '" 
                + telNum + "' already exists");
        }
        String ret = generateXid();
        XLog l = new XLog();
        l.setXid(ret);
        l.setAccessMode(m);
        l.setAccessAddress(telNum);
        l.setCreated(new Date());
        l.setTxnType(XLog.XType.New);
        l.setStatus(XLog.Status.PendingAuth);
        l.setAccountId(-1);
        l.setLoginId(-1);
        _mgr.insert(l);
        return ret;
    }
    
    /**
     * Function to create a new account
     */
    @Transactional
    public Account commitNewAccount(String xid, 
            Account.AccountType accType, Account.Language lang, String telNum, 
            String name, String pin) throws DBException,
            InvalidTelNumException, InvalidTransaction {
        // Check to see if transaction exists.
        List<XLog> list = getPending(xid);
        if ( list == null || list.isEmpty()) {
            throw new InvalidTransaction("Transaction id '" + xid 
                + "' does not exist");
        }
        XLog l = list.get(0);
        if ( l.getTxnType() != XLog.XType.New) {
            throw new InvalidTransaction("Pending Transaction '" + xid 
                + "' is not a new account transaction");
        }
        l.setStatus(XLog.Status.Committed);
        Account a = new Account();
        a.setAccountType(accType);
        a.setName(name);
        a.setTelNum(telNum);
        a.setCreated(new Date());
        a.setLanguage(lang);
        a.setBalance(0);
        _mgr.insert(a);
        Login login  = new Login();
        login.setAccountId(a.getId());
        login.setUserid(telNum);
        login.setPassword(pin);
        _mgr.insert(login);
        return a;
    }

    @Transactional
    public Account newAccount(Account.AccountType t, Account.Language lang,
                String telNum, String name, String pin, XLog.AccessMode m) 
            throws DBException, InvalidTelNumException, 
                InvalidTransaction, DuplicateAccountException {
        String xid = queueNewAccount(t, telNum, m);
        return commitNewAccount(xid, t, lang, telNum, name, pin);
    }

    /**
     * Function to return account given a telnum. Returns null if account
     * does not exist
     * @param telNum the telephone number
     * @return the Account info. Null if no matching account
     * @throws DBException
     * @throws InvalidTelNumException
     */
    public Account getAccount(String telNum) throws DBException,
            InvalidTelNumException {
        return _mgr.get(Account.class, "select * from Account where telNum='"
            + telNum + "'");
    }
    /**
     * Function to return account given a id. Returns null if account
     * does not exist
     * @param id the account id
     * @return the Account info. Null if no matching account
     * @throws DBException
     */
    public Account getAccount(long id) throws DBException {
        return _mgr.get(Account.class, "select * from Account where id="
            + id );
    }

    /**
     * Function to verify a pin number for a given account
     * @param telNum the telephone number
     * @return the Account info. Null if no matching account
     * @throws DBException
     * @throws InvalidTelNumException
     */
    public boolean matchesPin(Account a, String userid, String pin) 
                throws DBException {

        List<Login> r = _mgr.select(Login.class, 
            "select * from Login where accountId=" + a.getId()
            + " and userid='" + userid + "'");
        if (r == null || r.size() < 1) {
            return false;
        }
        Login l = r.get(0);
        return l.getPassword().equals(pin);
    }

    /**
     * Function to commit a pending transaction after verifying the pin.
     */
    @Transactional
    public void commitPending(String xid, String pin) 
                throws DBException, IllegalAccessError,
                InvalidTransaction {
        List<XLog> list = getPending(xid);
        if ( list == null || list.size() == 0) {
            throw new InvalidTransaction("Transaction id '" + xid 
                + "' is invalid");
        }
        // Commit the transactions if pending. 
        // If pendingAuth and pin does not match, we will throw an exception
        // and that should rollback any prior changes.
        Account a = null;
        for (XLog l: list) {
            a = getAccount(l.getAccountId());
            if ( a == null ) {
                throw new InvalidTransaction("Error processing request for '"
                    + xid + "'. Please call XChange to report the error.");
            }
            if ( l.getStatus() == XLog.Status.PendingAuth && 
                    !matchesPin(a, a.getTelNum(), pin) ) {
                throw new IllegalAccessError("Invalid pin for '"
                            + a.getTelNum()  + "'");
            }
            a.setBalance(a.getBalance()+l.getAmount());
            l.setStatus(XLog.Status.Committed);
        }
    }


    @Transactional
    public String deposit(Account agent, Account customer, double amount,
            boolean waitForAuthorization) throws InvalidAccountException, 
                InvalidTelNumException, DBException {
        switch(agent.getAccountType()) {
            case Agent:
            case AgentAndMerchant:
                break;
            default:
                throw new InvalidAccountException(
                    "Tel # '" + agent.getTelNum() + "' is not a Agent account");
        }
        String xid = generateXid();
        // Move $$ from agent's account into Customer's account
        transfer(xid, agent, customer, amount, 
                XLog.AccessMode.Agent, agent.getTelNum(),
                (waitForAuthorization ? XLog.Status.Pending
                    : XLog.Status.Committed),
                (waitForAuthorization ? XLog.Status.PendingAuth 
                    : XLog.Status.Committed));
        return xid;
    }
    @Transactional
    public String withdraw(Account agent, Account customer, double amount,
            boolean waitForAuthorization) throws InvalidTelNumException,
            InvalidAccountException, InsufficientFundsException, DBException {
        switch(agent.getAccountType()) {
            case Agent:
            case AgentAndMerchant:
                break;
            default:
                throw new InvalidAccountException(
                    "Tel # '" + agent.getTelNum() + "' is not a Agent account");
        }
        String xid = generateXid();
        // Move $$ from customer's account into Agent's account
        transfer(xid,customer, agent, amount, 
                XLog.AccessMode.Agent, agent.getTelNum(),
                (waitForAuthorization ? XLog.Status.PendingAuth
                    : XLog.Status.Committed),
                (waitForAuthorization ? XLog.Status.Pending 
                    : XLog.Status.Committed));
        return xid;
    }
    @Transactional
    public String transfer(Account from, Account to, double amount,
            boolean waitForAuthorization) 
            throws InvalidTelNumException, InsufficientFundsException, 
                DBException{
        if ( from.getBalance() < amount) {
            throw new InsufficientFundsException("Not enough balance in "
                + "Account '" + from.getTelNum() + "' to transfer");
        }
        // Move $$ from customer's from account into to account
        String xid = generateXid();
        transfer(xid, from, to, amount, 
                XLog.AccessMode.Sms, from.getTelNum(),
                (waitForAuthorization ? XLog.Status.PendingAuth 
                    : XLog.Status.Committed),
                (waitForAuthorization ? XLog.Status.Pending
                    : XLog.Status.Committed));
        return xid;
    }
    public List<XLog> history(long accountID, int numRecords)
            throws InvalidAccountException {
        return null;
    }
    public double balance(String accountNum) throws InvalidTelNumException,
            DBException {
        Account acc = getAccount(accountNum);
        if ( acc == null ) {
            throw new InvalidTelNumException(
                "Account for # '" + acc + "' does not exist");
        }
        return acc.getBalance();
    }
}
