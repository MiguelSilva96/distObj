package bookstore.bank;

import java.util.HashMap;
import java.util.Map;

public class Bank {

    Map<Integer, Account> accounts = new HashMap<>();

    public Bank() {
        accounts.put(1, new Account("PT12345", "ze", 100));
        accounts.put(2, new Account("ES12345", "carlos", 1));
    }

    public Account search(String iban) {
        for (Account a : accounts.values())
            if (a.getIban().equals(iban))
                return a;
        return null;
    }
}

