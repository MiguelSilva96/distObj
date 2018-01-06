package bookstore;

import io.atomix.catalyst.transport.Address;
import utilities.ObjRef;

public class Client {
    public static void main(String[] args) throws Exception {
        int port = 10000;
        String host = "localhost";
        Address addr = new Address(host, port);
        DistributedObjects distObj = new DistributedObjects();
        Store store;
        store = (Store) distObj.importObj(new ObjRef(addr, 1, "store"));
        int txid = distObj.beginTransaction();
        Cart cart = store.newCart(txid);
        Book book = store.search("one", txid);
        cart.add(book, txid);
        boolean res = cart.buy(txid);
        boolean result = distObj.commitTransaction(txid);
        if(result)
            System.out.println("Comprou = "+res);
    }

}
