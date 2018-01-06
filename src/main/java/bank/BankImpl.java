package bank;

import java.util.HashMap;
import java.util.Map;

public class BankImpl implements Bank {

    Map<Integer, Account> accounts = new HashMap<>();

    public BankImpl() {
        accounts.put(1, new AccountImpl("PT12345", 100));
        accounts.put(2, new AccountImpl("ES12345", 1));
    }

    public Account search(String iban) {
        for (Account a : accounts.values())
            if (a.getIban().equals(iban))
                return a;
        System.out.println("chgeuei aqui");
        return null;
    }
}
