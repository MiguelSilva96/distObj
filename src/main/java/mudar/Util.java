package mudar;

import bank.RemoteAccount;
import bookstore.RemoteBook;
import bookstore.RemoteCart;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import pt.haslab.ekit.Clique;

public class Util {
    public static Object makeRemote(SingleThreadContext tc, ObjRef res, int storeId, String title, Clique clique) {
        String clas = res.cls;
        Object ret = null;
        switch (clas) {
            case "cart":
                ret = new RemoteCart(tc, res.address, res.id);
                break;
            case "book":
                ret = new RemoteBook(tc, res.address, res.id, System.currentTimeMillis(), storeId, title);
                break;
            case "account":
                ret = new RemoteAccount(tc, clique, res.id);
                break;
        }
        return ret;
    }


}
