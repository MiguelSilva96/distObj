package bank;

import java.util.ArrayList;
import java.util.List;

public class AccountImpl implements Account {

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

    public boolean buy(float price) {
        /*
        if (balance >= price) {
            balance -= price;
            transactions.add(new Txn(price));
            return true;
        }*/
        balance -= price;
        transactions.add(new Txn(price));
        return false;
    }
}