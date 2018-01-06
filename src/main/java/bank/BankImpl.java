package bank;

import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankImpl implements Bank {

    Map<Integer, Account> accounts = new HashMap<>();
    Log log;
    Clique clique;

    public BankImpl() {
        log = new Log("banco");
        this.clique = clique;
        accounts.put(1, new AccountImpl("PT12345", 100));
        accounts.put(2, new AccountImpl("ES12345", 1));
    }

    public Account search(String iban) {
        for (Account a : accounts.values())
            if (a.getIban().equals(iban))
                return a;
        return null;
    }

    class AccountImpl implements Account {

        private String iban;
        private float balance;
        private List<Txn> transactions;

        public AccountImpl(String iban, float balance) {
            this.iban = iban;
            this.balance = balance;
            this.transactions = new ArrayList<>();
        }

        public String getIban() {
            return iban;
        }

        public boolean buy(float price, int txid) {
        /*
        if (balance >= price) {
            balance -= price;
            transactions.add(new Txn(price));
            return true;
        }*/
            balance -= price;
            transactions.add(new Txn(price));
            return true;
        }
    }
}
