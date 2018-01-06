package bookstore;

import Distribution.DistributedObjects;

public class Server {
    public static void main(String[] args) throws Exception {
        DistributedObjects distObj = new DistributedObjects();
        StoreImpl store = new StoreImpl();
        distObj.exportObj(store);
        distObj.initialize();
    }
}
