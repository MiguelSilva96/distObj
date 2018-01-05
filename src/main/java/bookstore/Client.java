package bookstore;

import io.atomix.catalyst.transport.Address;
import mudar.DistributedObjects;
import mudar.ObjRef;

public class Client {
    public static void main(String[] args) throws Exception {
        int port = 11111;
        String host = "localhost";
        Address addr = new Address(host, port);
        DistributedObjects distObj = new DistributedObjects();
        Store store;
        store = (Store) distObj.importObj(new ObjRef(addr, 1, "store"));
        Cart cart = store.newCart();
        Book book = store.search("one");
        System.out.println(cart.buy());
        System.out.println(book.getAuthor());
    }

}
