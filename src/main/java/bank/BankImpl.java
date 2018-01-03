package bank;

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
}
