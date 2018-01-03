package bookstore;

public class Server {
    public static void main(String[] args) throws Exception {

        DistributedObjects distObj = new DistributedObjects();
        distObj.initialize();
        StoreImpl store = new StoreImpl();
        distObj.exportObj(store);

    }
}
