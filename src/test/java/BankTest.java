import org.junit.Test;
import junit.framework.Assert;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import com.tengo.beans.*;
import com.tengo.businesslogic.*;
import com.tengo.sqldb.*;
import com.tengo.sqldb.inject.module.TransactionModule;


public class BankTest {

    @Test
    public void doTest() throws Exception {
        Injector inj = Guice.createInjector(new DBModule(),
            new TransactionModule());
        DBManager mgr = inj.getInstance(DBManager.class);
        Bank b = inj.getInstance(Bank.class);
        Account customer = b.newAccount(
            Account.AccountType.Customer, Account.Language.English,
            "14155314937", 
            "Prasad Mokkapati", "123456", XLog.AccessMode.Sms);
        System.out.println("Customer id: '" + customer.getId() + "'");
        Account agent = b.newAccount(Account.AccountType.Agent, 
            Account.Language.English,
            "14155315824", "Corina Mokkapati", "789012", XLog.AccessMode.Sms);
        b.deposit(agent, customer, 50, false);
        System.out.println("Balance: " + customer.getBalance());
        long cid = customer.getId();
        long aid = agent.getId();
        System.out.println("Customer id: " + cid
            + ", Agent id: " + aid);
        customer.setBalance(99);

        customer = mgr.get(Account.class, 
            "select * from Account where id=" + cid);
        Assert.assertNotNull(customer);
        System.out.println("Balance: " + customer.getBalance());

        mgr.delete(agent);
        mgr.delete(customer);
        mgr.executeSQL("delete from XLog where accountid "
            + " in (" + cid + ", " + aid + ")");
    }
}
        
