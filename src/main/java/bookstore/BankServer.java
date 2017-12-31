package bookstore;

import java.util.LinkedList;
import java.util.Queue;

public class BankServer {
    public static void main(String[] args) throws Exception {

        Queue<Txn> qq = new LinkedList<>();

        Thread add = new BankQueueAdd(qq);
        Thread rm = new BankQueueRm(qq);
        qq.add(new Txn("PT12345", 2));
        add.start();
        rm.start();

        DistributedObjects distObj = new DistributedObjects();
        distObj.initialize_bank();
        Bank bank = new Bank();
        distObj.exportObj(bank);
    }

    private static class BankQueueAdd extends Thread {

        private Queue<Txn> qq;

        private BankQueueAdd(Queue<Txn> qq) {
            this.qq = qq;
        }

        @Override
        public void run() {

            //Address adr = new Address("localhost", 11112);
            DistributedObjects distObj = new DistributedObjects();
            distObj.listen_add(qq);
        }
    }

    private static class BankQueueRm extends Thread {

        private Queue<Txn> qq;

        private BankQueueRm(Queue<Txn>  qq) {
            this.qq = qq;
        }

        @Override
        public void run() {

            //Address adr = new Address("localhost", 11112);
            DistributedObjects distObj = new DistributedObjects();
            distObj.listen_rm(qq);
        }
    }
}
        /*
        Bank bank = (Bank) distObj.importObj(new ObjRef(addr, 1, "bank"));
        Account account = bank.search("ES12345");
        System.out.println(account.buy(1));
        */

