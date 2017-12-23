package bookstore;

import io.atomix.catalyst.concurrent.SingleThreadContext;


public class Util {
    public static Object makeRemote(SingleThreadContext tc, ObjRef res) {
        String clas = res.cls;
        Object ret = null;
        switch (clas) {
            case "cart":
                ret = new RemoteCart(tc, res.address, res.id);
                break;
            case "book":
                ret = new RemoteBook(tc, res.address, res.id);
                break;
        }
        return ret;
    }


}
