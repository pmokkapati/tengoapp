/*
 * @author prasadm80@gmail.com
 */
package com.tengo.server.sms;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tengo.msgs.*;
import com.tengo.exceptions.*;

public class SMSParser {

    /** 
     * create new account
     * <agent pin> new <customer #> <name>
     */
    private static final Pattern _pNew = Pattern.compile(
        "^([nN][eE][wW]) (.*)$");
    private static final Pattern _pNewSpanish = Pattern.compile(
        "^([nN][uU][eE][vV][oO]) (.*)$");
    /** 
     * Deposit cash
     * <agent pin> add <customer #> <amount> [customer Pin]
     */
    private static final Pattern _pDeposit = Pattern.compile(
        "^([0-9]{6,9}) ([aA][dD][dD]) ([0-9]*) ([0-9]*(\\.[0-9]{2}){0,1})( [0-9]{6,9}){0,1}$");
    private static final Pattern _pDepositSpanish = Pattern.compile(
        "^([0-9]{6,9}) ([sS][uU][mM][aA][rR]) ([0-9]*) ([0-9]*(\\.[0-9]{2}){0,1})( [0-9]{6,9}){0,1}$");
    /** 
     * Widthdraw cash
     * <agent pin> get <customer #> <amount> [customer Pin]
     */
    private static final Pattern _pWithdraw = Pattern.compile(
        "^([0-9]{6,9}) ([gG][eE][tT]) ([0-9]*) ([0-9]*(\\.[0-9]{2}){0,1})( [0-9]{6,9}){0,1}$");
    private static final Pattern _pWithdrawSpanish = Pattern.compile(
        "^([0-9]{6,9}) ([oO][bB][tT][eE][nN][eE][rR]) ([0-9]*) ([0-9]*(\\.[0-9]{2}){0,1})( [0-9]{6,9}){0,1}$");

    /** 
     * transfer cash
     * send <to #> <amount> [customer Pin]
     */
    private static final Pattern _pTransfer = Pattern.compile(
        "^([sS][eE][nN][dD]) ([0-9]*) ([0-9]*(\\.[0-9]{2}){0,1})( [0-9]{6,9}){0,1}$");
    private static final Pattern _pTransferSpanish = Pattern.compile(
        "^([eE][nN][vV][iI][aA][rR]) ([0-9]*) ([0-9]*(\\.[0-9]{2}){0,1})( [0-9]{6,9}){0,1}$");

    /** 
     * Get balance
     * balance [customer Pin]
     */
    private static final Pattern _pBalance = Pattern.compile(
        "^([bB][aA][lL][aA][nN][cC][eE])( [0-9]{6,9}){0,1}$");

    /** 
     * Get history
     * history [customer Pin]
     */
    private static final Pattern _pHistory = Pattern.compile(
        "^([hH][iI][sS][tT][oO][rR][yY])( [0-9]{6,9}){0,1}$");


    protected static double parseAmount(String prefix, String suffix) {
        System.out.println("Parsing amount: '" + prefix + "'");
        return Double.parseDouble(prefix);
        //return (suffix == null) ? Double.parseDouble(prefix)
        //    : Double.parseDouble(prefix + suffix);
    }
    protected static String parsePin(String val) {
        return (val == null) ? null : val.trim();
    }

    protected static Message parseNewAccount(String from, Matcher m) 
            throws InvalidMessage {
        return new NewAccountMsg(from, m.group(2));
    }
    protected static Message parseDeposit(String from, Matcher m) 
            throws InvalidMessage {
        return new DepositMsg(from, 
            m.group(1),  // Agent pin
            m.group(3),  // Customer #
            parseAmount(m.group(4), m.group(5)), // Amount
            parsePin(m.group(6)));
    }
    protected static Message parseWithdraw(String from, Matcher m) 
            throws InvalidMessage {
        return new WithdrawMsg(from, 
            m.group(1),  // Agent pin
            m.group(3),  // Customer #
            parseAmount(m.group(4), m.group(5)), // Amount
            parsePin(m.group(6)));
    }
    protected static Message parseTransfer(String from, Matcher m) 
            throws InvalidMessage {
        return new TransferMsg(from, 
            m.group(2),  // Transfer to #
            parseAmount(m.group(3), m.group(4)), // Amount
            parsePin(m.group(5)));
    }
    protected static Message parseBalance(String from, Matcher m) 
            throws InvalidMessage {
        return new BalanceMsg(from, parsePin(m.group(2)));
    }


    /**
     * Function to parse a SMS message 
     */
    public static Message parse(String from, String msg) 
                throws InvalidMessage {
        Matcher m = _pNew.matcher(msg);
        if ( m.matches()) {
            return parseNewAccount(from, m);
        }
        m = _pDeposit.matcher(msg);
        if ( m.matches()) {
            return parseDeposit(from, m);
        }
        m = _pWithdraw.matcher(msg);
        if ( m.matches()) {
            return parseWithdraw(from, m);
        }
        m = _pTransfer.matcher(msg);
        if ( m.matches()) {
            return parseTransfer(from, m);
        }
        m = _pBalance.matcher(msg);
        if ( m.matches()) {
            return parseBalance(from, m);
        }
        /*
        m = _pHistory.matcher(msg);
        if ( m.matches()) {
            return parseHistory(from, m);
        }
        */
        throw new InvalidMessage(msg);
    }
}
