package bank;

import bookstore.DistributedObjects;

public class BankServer {
    public static void main(String[] args) throws Exception {

        DistributedObjects distObj = new DistributedObjects();
        BankImpl bank = new BankImpl();
        distObj.exportObj(bank);
        distObj.initialize_bank();
    }
}


