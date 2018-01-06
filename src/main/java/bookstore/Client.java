package bookstore;

import Distribution.DistributedObjects;
import io.atomix.catalyst.transport.Address;
import utilities.ObjRef;

public class Client {
    public static void main(String[] args) throws Exception {
        /*############### MODO DE INICIALIZAÇAO #################*/
        int port = 10000;
        String host = "localhost";
        Address addr = new Address(host, port);
        DistributedObjects distObj = new DistributedObjects();
        Store store;
        store = (Store) distObj.importObj(new ObjRef(addr, 1, "store"));
        /* ##################################################### */

        /* EXEMPLO SEM TRANSAÇOES
           USA METODOS DA API QUE NAO PEDEM TXID
         */
        Cart cart = store.newCart();
        Book book = store.search("one");
        cart.add(book);
        System.out.println(book.getAuthor());

        /* EXEMPLO QUE USA TRANSAÇOES */
        int txid = distObj.beginTransaction();
        boolean res = cart.buy(txid, "PT12345");
        boolean result = distObj.commitTransaction(txid);
        if(result) //commit com sucesso
            System.out.println("Comprou = "+res);
        else //rollback
            System.out.println("Algo correu mal, tente novamente");
    }

}
