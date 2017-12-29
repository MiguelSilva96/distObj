package bookstore;

public class BankServer {
    public static void main(String[] args) throws Exception {

        DistributedObjects distObj = new DistributedObjects();
        distObj.initialize();
        BankImpl bank = new BankImpl();
        distObj.exportObj(bank);

    }
}
