/*
 * @author prasadm80@gmail.com
 */
package com.tengo.businesslogic;

import com.tengo.beans.*;
import com.tengo.exceptions.*;

interface Bank {
    
    String createReferenceNum();

    TransactionInfo deposit(long accountID, double amt, String referenceNum)
                throws InvalidAccountException, InsufficientFundsException,
                    InternalException;

    TransactionInfo withdraw(long accountID, double amt, String referenceNum)
                throws InvalidAccountException, InternalException;

    TransferInfo transfer(long fromId, long toId, double amt, 
            String referenceNum)
                throws InvalidAccountException, InsufficientFundsException, 
                    InternalException;

    double getBalance(long accountID) 
                throws InvalidAccountException, InternalException;
    double getAvailableFunds(long accountID) 
                throws InvalidAccountException, InternalException;

    ArrayList<XLogInfo> getHistory(long accountID, int maxEntries) 
                throws InvalidAccountException, InternalException;


    TranasactionInfo authorizeWithdrawal(long accountID, double amt, 
            String referenceNum) 
                throws InvalidAccountException, InsufficientFundsException,
                    InternalException;

    TransactionInfo settle(String referenceNum) 
                throws InvalidTransactionException, InternalException;

}
