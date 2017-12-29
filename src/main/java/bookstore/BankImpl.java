package bookstore;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankImpl implements Bank {

    Map<Integer, Account> accounts = new HashMap<>();

    public BankImpl() {
        accounts.put(1, new AccountImpl("PT12345", "ze", 100));
        accounts.put(2, new AccountImpl("ES12345", "carlos", 1));
    }

    public Account search(String iban) {
        for (Account a : accounts.values())
            if (a.getIban().equals(iban))
                return a;
        return null;
    }

    public class AccountImpl implements Account {

        private String iban;
        private String titular;
        private float balance;
        private List<Thx> transactions;


        public AccountImpl(String iban, String titular, float balance) {
            this.iban = iban;
            this.titular = titular;
            this.balance = balance;
            this.transactions = new ArrayList<>();
        }

        public String getIban() {
            return iban;
        }

        public String getTitular() {
            return titular;
        }

        public float getBalance() {
            return balance;
        }

        public boolean buy(float price) {
            if (balance >= price) {
                balance -= price;
                transactions.add(new Thx(price));
                return true;
            }
            return false;
        }
    }

    public class Thx {

        private float price;
        private Timestamp date;

        public Thx(float price) {
            this.price = price;
            this.date = new Timestamp(System.currentTimeMillis());
        }
    }
}

