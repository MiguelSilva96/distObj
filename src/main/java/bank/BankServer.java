package bookstore.bank;

import bookstore.DistributedObjects;

import java.util.LinkedList;
import java.util.Queue;

public class BankServer {
    public static void main(String[] args) throws Exception {

        DistributedObjects distObj = new DistributedObjects();
        Bank bank = new Bank();
        distObj.exportObj(bank);
        distObj.bank_requests();
    }
}


