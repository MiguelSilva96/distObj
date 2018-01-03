package bank;

import java.util.ArrayList;
import java.util.List;

public class AccountImpl implements Account {

    private String iban;
    private String titular;
    private float balance;
    private List<Txn> transactions;


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
            transactions.add(new Txn(price));
            return true;
        }
        return false;
    }
}