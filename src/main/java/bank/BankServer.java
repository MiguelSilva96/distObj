package bank;

import bookstore.DistributedObjects;

public class BankServer {
    public static void main(String[] args) throws Exception {

        DistributedObjects distObj = new DistributedObjects();
        Bank bank = new Bank();
        distObj.exportObj(bank);
        distObj.initialize_bank();
    }
}


