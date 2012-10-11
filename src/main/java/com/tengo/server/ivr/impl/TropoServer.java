/*
 * @author prasadm80@gmail.com
 */
package com.tengo.server.ivr.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import com.voxeo.tropo.Tropo;
import com.voxeo.tropo.actions.Do;
import com.voxeo.tropo.actions.AskAction;
import com.voxeo.tropo.TropoSession;
import com.voxeo.tropo.ActionResult;
import com.voxeo.tropo.TropoResult;
import com.voxeo.tropo.Key;
import com.voxeo.tropo.enums.Mode;
import static com.voxeo.tropo.Key.*;
import static com.voxeo.tropo.enums.Mode.*;
import static com.voxeo.tropo.enums.Voice.*;


import com.tengo.exceptions.*;
import com.tengo.beans.*;
import com.tengo.businesslogic.Bank;
import com.tengo.server.ivr.IVR;
import com.tengo.server.sms.SMS;
import com.tengo.sqldb.DBException;
import static com.tengo.beans.AccountInfo.*;
import static com.tengo.beans.AccountInfo.Language.*;

/**
 * Class to handle (a) Incoming call (b) outbound calls and 
 * (c) Response to ask requests in calls
 * Incoming calls and sessions started for outbound calls use URL /ivr
 * where as responses to ask requests use URL /ivr/response
 */
@Singleton
public class TropoServer extends HttpServlet implements IVR {

    private Key _voice[] = {
        createKey("voice","Allison"),   // Unknown
        createKey("voice","Allison"),   // English
        createKey("voice","Esperanza")  // Spanish
    };

    private Key _terminator = createKey("terminator", "#");
    protected Bank _bank;

    protected String _token;
    protected SMS _sms;

    protected static final Locale _english = new Locale("en", "US");
    protected static final Locale _mexican = new Locale("es", "MX");

    protected static final String MESSAGES_BASENAME = 
            "com.tengo.server.ivr.impl.Messages";

    protected static final String MSG_TRANSACTION_TYPE = "Transactiontype";
    protected static final String MSG_SORRY_DID_NOT_HEAR = "SorryDidNotHear";
    protected static final String MSG_NOT_REQUESTED = "NotRequested";
    protected static final String MSG_INVALID_PIN = "InvalidPin";
    protected static final String MSG_ERROR_PROCESSING = "ErrorProcessing";
    protected static final String MSG_ACCOUNT_CREATE = "AccountCreate";
    protected static final String MSG_ENTER_PIN = "EnterPin";
    protected static final String MSG_CONFIRM_PIN = "ConfirmPin";
    protected static final String MSG_ACCOUNT_ACTIVE = "AccountActive";
    protected static final String MSG_AUTHORIZE_GET_PIN = "AuthorizeGetPin";
    protected static final String MSG_AUTHORIZE_WITHDRAW = "AuthorizeWithdraw";
    protected static final String MSG_AUTHORIZE_DEPOSIT = "AuthorizeDeposit";
    protected static final String MSG_AUTHORIZE_TRANSFER = "AuthorizeTransfer";
    protected static final String MSG_TRANSACTION_DONE = "TransactionDone";


    @Inject
    protected void init(@Named("TropoIVRToken") String token,
            Bank b, SMS sms) {
        _token = token;
        _bank = b;
        _sms = sms;
    }

    protected String getMessage(Language l, String label) {
        ResourceBundle b = null;
        switch (l) {
            case English:
                b = ResourceBundle.getBundle(MESSAGES_BASENAME, 
                        _english);
                break;
            case Spanish:
                b = ResourceBundle.getBundle(MESSAGES_BASENAME, 
                        _mexican);
                break;
            default:
                b = ResourceBundle.getBundle(MESSAGES_BASENAME, 
                        _english);
                break;
        }
        return (b == null ? null : b.getString(label));
    }
                

    /**
     * Ask user for Pin # for authorization
     */
    protected void askAuthorizePin(Tropo t, AccountInfo.Language l,
            HttpServletResponse res) { 
        AskAction ask = t.ask(ATTEMPTS(3), NAME("getpin"),
            BARGEIN(true), TIMEOUT(5f), REQUIRED(true));
        ask.choices(VALUE("[6 DIGITS]"), MODE(Mode.DTMF), _terminator);
        ask.and(Do.say(VALUE(getMessage(l, MSG_ENTER_PIN)),_voice[l.ordinal()]));
        t.on("continue", "/ivr/response/verify/pin/continue");
        t.on("incomplete", "/ivr/response/verify/pin/incomplete");
        t.on("error", "/ivr/response/verify/pin/error");
        t.on("hangup", "/ivr/response/verify/pin/hangup");
        t.render(res);
    }
    /**
     * Repeat the pin number typed by the user to confirm. If not correct
     * have them repeat.
     */
    protected void askVerifyPin(Tropo t, AccountInfo.Language l,
                HttpServletResponse res, String pin) { 
        int len = pin.length();
        StringBuilder str = new StringBuilder("");
        for (int i=0; i < len; i++) {
            str.append(pin.charAt(i) + " ");
        }
        String msg = MessageFormat.format(getMessage(l, MSG_CONFIRM_PIN),
                str);
        AskAction ask = t.ask(ATTEMPTS(3), NAME("verifyenteredpin"),
            BARGEIN(true), TIMEOUT(5f), REQUIRED(true));
        ask.choices(VALUE("1,2"), MODE(Mode.DTMF), _terminator);
        ask.and(Do.say(VALUE(getMessage(l, MSG_ENTER_PIN)),_voice[l.ordinal()]));
        t.on("continue", "/ivr/response/pin/verified/continue");
        t.on("incomplete", "/ivr/response/verify/pin/incomplete");
        t.on("error", "/ivr/response/verify/pin/error");
        t.on("hangup", "/ivr/response/verify/pin/hangup");
        t.render(res);
    }
    /**
     * Function to process the callback if the request is a callback request. 
     * I.E. If this request is part of a callback by Tropo in response to a 
     * session launch.
     * @param res HttpServletResponse object
     * @param t The Tropo Object
     * @param s The Tropo Sesssion
     * @return true if call is a callback. If it is a incoming SMS
     * request or a response to a ask returns false
     */
    protected boolean consume(HttpServletRequest req, 
            HttpServletResponse res, 
            Tropo t, TropoSession s) throws IOException, ServletException {
        HashMap<String, String> params = s.getParameters();
        String reason = null;
        String authStr = null;
        if ( params == null || (reason = params.get("Reason")) == null) {
            return false;
        }
        // Add the parameters to the session
        HttpSession hs = req.getSession();
        for (Map.Entry<String, String> m :params.entrySet()) {
            hs.setAttribute(m.getKey(), m.getValue());
        }
        try {
            switch (Enum.valueOf(AuthType.class, params.get("AuthType")) ) {
                case Deposit:
                    authorizeDeposit(res, t, params, hs);
                    break;
                case Withdraw:
                    authorizeWithdraw(res, t, params, hs);
                    break;
                case Transfer:
                    authorizeTransfer(res, t, params, hs);
                    break;
                /*
                case History:
                    consumeHistory(res, t, params, hs);
                    break;
                */
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * Tropo call in response to  outgoing call to authorize deposit. Asks the 
     * user for pin # to authorize the deposit.
     * @param res the HTTPResponse object
     * @param t Tropo object
     * @param t Tropo params
     * @param s HttpSession
     * @return true unless an exception occurred.
     */
    protected boolean authorizeDeposit(HttpServletResponse res, 
            Tropo t, HashMap<String, String> params, 
            HttpSession s) throws IOException, ServletException {
        String callNumber = params.get("CallNumber");
        String languageStr = params.get("Language");
        String name = params.get("Name");
        String xid = params.get("XID");
        String msg = null;

        Language l = (languageStr == null) ? English 
            : Enum.valueOf(Language.class, languageStr);
        XLog log = null;
        try {
            log = _bank.getPendingAuth(XLog.XType.Deposit, xid);
        }
        catch (Exception e) {
            e.printStackTrace();
            msg = getMessage(l, MSG_ERROR_PROCESSING);
            t.say(VALUE(msg), _voice[l.ordinal()]);
            t.hangup();
            t.render(res);
        }
        msg = MessageFormat.format(getMessage(l, MSG_AUTHORIZE_DEPOSIT), 
            log.getAmount());
        t.call(callNumber);
        t.say(VALUE(msg),_voice[l.ordinal()]);
        askAuthorizePin(t, l, res);
        return true;
    }
    /**
     * Tropo call in response to  outgoing call to authorize withdrawal. 
     * Asks the user for pin # to authorize the withdrawal.
     * @param res the HTTPResponse object
     * @param t Tropo object
     * @param t Tropo params
     * @param s HttpSession
     * @return true unless an exception occurred.
     */
    protected boolean authorizeWithdraw(HttpServletResponse res, 
            Tropo t, HashMap<String, String> params, 
            HttpSession s) throws IOException, ServletException {
        String callNumber = params.get("CallNumber");
        String languageStr = params.get("Language");
        String name = params.get("Name");
        String xid = params.get("XID");
        String msg = null;

        Language l = (languageStr == null) ? English 
            : Enum.valueOf(Language.class, languageStr);
        XLog log = null;
        try {
            log = _bank.getPendingAuth(XLog.XType.Withdraw, xid);
        }
        catch (Exception e) {
            e.printStackTrace();
            msg = getMessage(l, MSG_ERROR_PROCESSING);
            t.say(VALUE(msg), _voice[l.ordinal()]);
            t.hangup();
            t.render(res);
        }
        msg = MessageFormat.format(getMessage(l, MSG_AUTHORIZE_WITHDRAW), 
            log.getAmount());

        t.call(callNumber);
        t.say(VALUE(msg),_voice[l.ordinal()]);
        askAuthorizePin(t, l, res);
        return true;
    }
    /**
     * Tropo call in response to  outgoing call to authorize transfer. Asks the 
     * user for pin # to authorize the transfer.
     * @param res the HTTPResponse object
     * @param t Tropo object
     * @param t Tropo params
     * @param s HttpSession
     * @return true unless an exception occurred.
     */
    protected boolean authorizeTransfer(HttpServletResponse res, 
            Tropo t, HashMap<String, String> params, 
            HttpSession s) throws IOException, ServletException, DBException {
        String callNumber = params.get("CallNumber");
        String languageStr = params.get("Language");
        String name = params.get("Name");
        String xid = params.get("XID");
        String msg = null;

        Language l = (languageStr == null) ? English 
            : Enum.valueOf(Language.class, languageStr);
        List<XLog> list = null;
        try {
            list = _bank.getPending(xid);
        }
        catch (Exception e) {
            e.printStackTrace();
            msg = getMessage(l, MSG_ERROR_PROCESSING);
            t.say(VALUE(msg), _voice[l.ordinal()]);
            t.hangup();
            t.render(res);
        }
        AccountInfo to=null;
        double amount = 0;
        for (XLog log: list) {
            if ( log.getTxnType() == XLog.XType.Deposit) {
                to = _bank.getAccount(log.getAccountId());
                amount = log.getAmount();
                break;
            }
        }
        msg = MessageFormat.format(getMessage(l, MSG_AUTHORIZE_TRANSFER), 
                amount, to.getName() + to.getTelNum()); 

        t.call(callNumber);
        t.say(VALUE(msg),_voice[l.ordinal()]);
        askAuthorizePin(t, l, res);
        return true;
    }

    protected void processResponse(HttpServletRequest req, 
                HttpServletResponse res, String uri) throws IOException, 
                ServletException {
        Tropo t = new Tropo();
        TropoResult result = t.parse(req);
        HttpSession s = req.getSession();
        /*
        if ( uri.endsWith("newaccount/language/continue") ) {
            processNewAccountLanguage(res, result, t, s);
        }
        else if ( uri.endsWith("newaccount/pin/continue") ||
                uri.endsWith("verify/pin/continue") ) {
            processNewAccountPin(res, result, t, s);
        }
        */
        if ( uri.endsWith("deposit/pin") ||
                uri.endsWith("withdraw/pin") ||
                uri.endsWith("transfer/pin") ) {
            try {
                processAuthorizePin(res, result, t, s);
            }
            catch (Exception e) {
                t.say("Unknown Exception ");
                t.hangup();
                t.render(res);
            }
        }
        else if ( uri.endsWith("fail") ) {
            authFailed(res, result, t, s);
        }
        else {
            t.say("Unknown Response URI '" + uri + "'");
            t.hangup();
            t.render(res);
        }
    }
    /**
     * Got pin #. Repeat pin # and let user verify
     */
/*
    protected void processVerifyPin(HttpServletResponse res,
            TropoResult result, Tropo t, HttpSession s) {
        System.out.println("Inside processNewAccountLanguage");
        ActionResult act = result.getActions().get(0);
        int choice = Integer.parseInt(act.getValue());
        Language l = (choice==2) ? Spanish : English;
        s.setAttribute("Language", l.toString());
        askNewAccountPin(t, l, res);
    }
*/
    /**
     * Got response to get Pin Number for authorization
     */
    protected void processAuthorizePin(HttpServletResponse res,
            TropoResult result, Tropo t, HttpSession s) throws DBException {
        System.out.println("Inside processAuthorizePin");
        ActionResult act = result.getActions().get(0);
        String pin = act.getValue();
        String xid = (String)s.getAttribute("XID");
        String telNum = (String)s.getAttribute("CallNumber");
        String name = (String)s.getAttribute("Name");
        String languageStr = (String)s.getAttribute("Language");
        AuthType authType = Enum.valueOf(AuthType.class,
                (String)s.getAttribute("AuthType"));
        Language l =(languageStr == null) ? English 
                : Enum.valueOf(Language.class, languageStr);
        String msg = getMessage(l, MSG_TRANSACTION_DONE);
        List<XLog> list = null;
        try {
            list = _bank.getPending(xid);
        }
        catch (Exception e) {
            e.printStackTrace();
            msg = getMessage(l, MSG_ERROR_PROCESSING);
            t.say(VALUE(msg), _voice[l.ordinal()]);
            t.hangup();
            t.render(res);
        }
        String fromMsg=null, toMsg=null;
        AccountInfo from=null, to=null;
        double amount=0;
        for (XLog log: list) {
            switch (authType) {
                case Deposit:
                    if ( log.getTxnType() == XLog.XType.Deposit) {
                        to = _bank.getAccount(log.getAccountId());
                        amount = log.getAmount();
                    }
                    else if (log.getTxnType() == XLog.XType.Withdraw) {
                        from = _bank.getAccount(log.getAccountId());
                    }
                    break;
                case Transfer:
                case Withdraw:
                    if ( log.getTxnType() == XLog.XType.Withdraw) {
                        from = _bank.getAccount(log.getAccountId());
                        amount = -log.getAmount();
                    }
                    else if (log.getTxnType() == XLog.XType.Deposit) {
                        to = _bank.getAccount(log.getAccountId());
                    }
                    break;
            }
        }
                        
            
        try {
            _bank.commitPending(xid, pin); 
            t.say(VALUE(msg), _voice[l.ordinal()]);
        }
        catch (IllegalAccessError e) {
            e.printStackTrace();
            msg = getMessage(l, MSG_INVALID_PIN);
            t.say(VALUE(msg), _voice[l.ordinal()]);
            _sms.sendMsg(telNum, msg);
            t.hangup();
            t.render(res);
            return;
        }
        catch (Exception e) {
            e.printStackTrace();
            msg = getMessage(l, MSG_ERROR_PROCESSING);
            t.say(VALUE(msg), _voice[l.ordinal()]);
            _sms.sendMsg(telNum, msg);
            t.hangup();
            t.render(res);
            return;
        }
        switch (authType) {
            case Deposit:
                fromMsg = amount + " has been deposited into "
                    + from.getName() + "(" + from.getTelNum() + ") account. "
                    + "Your (Agent) balance is " + from.getBalance();
                toMsg = amount + " has been deposited into your account. "
                    + "Your balance is " + to.getBalance();
                break;
            case Withdraw:
                fromMsg = amount + " has been withdrawn from "
                    + from.getName() + "(" + from.getTelNum() + ") account. "
                    + "Your (Agent) balance is " + from.getBalance();
                toMsg = amount + " has been withdrawn from your account. "
                    + "Your balance is " + to.getBalance();
                break;
            case Transfer:
                fromMsg = amount + " has been transfered to "
                    + from.getName() + "(" + from.getTelNum() + "). "
                    + "Your  balance is " + from.getBalance();
                toMsg = amount + " has been deposited into your account "
                    + "by " + from.getName() + "(" 
                    + from.getTelNum() + "). Your balance is " 
                    + to.getBalance();
                break;
        }
        //t.say(VALUE(toMsg),_voice[l.ordinal()]);
        _sms.sendMsg(from.getTelNum(),fromMsg);
        _sms.sendMsg(to.getTelNum(),toMsg);
        t.hangup();
        t.render(res);
    }

    /**
     * Failed to process
     */
    protected void authFailed(HttpServletResponse res,
            TropoResult result, Tropo t, HttpSession s) {
        System.out.println("Inside processFail");
        AuthType authType = Enum.valueOf(AuthType.class, 
                (String)s.getAttribute("AuthType"));
        String telNum = (String)s.getAttribute("CallNumber");
        if ( telNum != null) {
            _sms.sendMsg(telNum, getMessage(English, MSG_ERROR_PROCESSING));
        }
        t.hangup();
        t.render(res);
    }
        
    /**
     * A voice servlet which is called when a session is launched in response 
     * to a IVR call to be made and/or an incoming call came in.
     * @param req the request
     * @param res the response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) 
            throws ServletException, IOException {
        String uri=req.getRequestURI();
        if ( uri.startsWith("/ivr/response") ) {
            // Called in response to a user input/timeout ..
            processResponse(req, res, uri);
            return;
        }
        // Figure out if it is a incoming or outgoing call
        Tropo t = new Tropo();
        TropoSession s = t.session(req);
        if (!consume(req, res, t, s) ) {
            incomingCall(req, res, t, s);
        }
    }

    /**
     * Incoming call
     * @param req the servlet request
     * @param res the servlet response
     * @param t tropo object
     * @param s tropo session
     */
    protected void incomingCall(HttpServletRequest req, 
            HttpServletResponse res, Tropo t, TropoSession s) 
            throws IOException {
        /* TODO
        t.say(VALUE(WELCOME_MSG),_voice[0]);
        //t.say(VALUE(ASK_INCOMING), _voice);
        t.ask(NAME("userchoice"), BARGEIN(true), MODE(DTMF),
                TIMEOUT(10f), ATTEMPTS(3), _voice[0])
            .and(Do.say(VALUE(TIMEOUT_MSG[0]),
                EVENT("timeout")).say(ASK_INCOMING[0]),
                Do.choices(VALUE("[1 DIGIT]")));
        t.on(EVENT("continue"), NEXT("/ivr/response/userchoice"));
        */
        t.render(res);
    }

    protected void userChoice(HttpServletRequest req, 
            HttpServletResponse res) throws IOException {
        Tropo t = new Tropo();
        //TropoSession s = t.session(req);
        TropoResult result = t.parse(req);
        ActionResult act = result.getActions().get(0);
        int choice = Integer.parseInt(act.getValue());
        /*
        switch (choice) {
            case 1: 
                newAccount(req, res, t, s);
                break;
            case 2:
                transfer(req, res, t, s);
                break;
            case 3:
                balance(req, res, t, s);
                break;
            case 4:
                history(req, res, t, s);
                break;
            case 5:
                changePin(req, res, t, s);
                break;
            default:
                t.say(VOICE("Soledad", "Please bear with us. One of our "
                    + "agents will respond to your call very shortly."
                agent(req, res, t, s);
        }
        */
        t.say("Your choice was #" + choice + ", Good bye");
        t.hangup();
        t.render(res);
    }
    /**
     * Function to get authorization. Results in a call made to get 
     * authorization. The way calls are made is by launching a new session
     * and parameters passed to it. I.E. A https call is made to tropo so it 
     * create a new session and call back to this server (voice URL link in 
     * in Tropo) along with parameters passed on the session launch.
     * @param t Type of authorization
     * @param a The account to get authorization from
     * @param xid the related transaction id that is logged in XLog.
     * @param from the telephone number (agent ??) that initiated this request
     */
    public void authorize(AuthType t, AccountInfo a, String xid, String from) {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("Reason", "Authorize");
        params.put("AuthType", t.toString());
        params.put("CallNumber", a.getTelNum());
        params.put("FromNumber", from);
        params.put("Language", a.getLanguage().toString());
        params.put("Name", a.getName());
        params.put("XID", xid);
        new Tropo().launchSession(_token, params);
    }
    /**
     * Function to create a new account. Results in a call made to get 
     * pin #. The way calls are made is by launching a new session
     * and parameters passed to it. I.E. A https call is made to tropo so it 
     * create a new session and call back to this server (voice URL link in 
     * in Tropo) along with parameters passed on the session launch.
     * @param telNum telNum of account
     * @param name Name of person creating the account
     * @param xid the related transaction id that is logged in XLog.
     */
    public void newAccount(String telNum, String name, String xid) {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("Reason", "NewAccount");
        params.put("CallNumber", telNum);
        params.put("Name", name);
        params.put("XID", xid);
        new Tropo().launchSession(_token, params);
    }
}
