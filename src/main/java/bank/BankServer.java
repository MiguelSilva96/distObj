package bank;

import io.atomix.catalyst.transport.Address;
import mudar.DistributedObjects;

public class BankServer {
    public static void main(String[] args) throws Exception {

        DistributedObjects distObj = new DistributedObjects(new Address("localhost:20000"));
        distObj.initialize_bank();
        BankImpl bank = new BankImpl();
        distObj.exportObj(bank);
    }
}


