package bookstore;

import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import bookstore.requests.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedObjects {

    Map<Integer, OrderIn> objs;
    AtomicInteger id;
    SingleThreadContext tc;
    Transport t;
    Address address;

    public DistributedObjects() {
        objs = new HashMap<>();
        id = new AtomicInteger(0);
        tc = new SingleThreadContext("srv-%d", new Serializer());
        t  = new NettyTransport();
        address = new Address("localhost:10000");
        register();
        new MonitorObjs().start();


    }

    public void register() {
        tc.serializer().register(StoreSearchReq.class);
        tc.serializer().register(StoreSearchRep.class);
        tc.serializer().register(CartAddReq.class);
        tc.serializer().register(CartAddRep.class);
        tc.serializer().register(CartBuyReq.class);
        tc.serializer().register(CartBuyRep.class);
        tc.serializer().register(StoreMakeCartReq.class);
        tc.serializer().register(StoreMakeCartRep.class);
        tc.serializer().register(BookInfoReq.class);
        tc.serializer().register(BookInfoRep.class);
        tc.serializer().register(ObjRef.class);
        tc.serializer().register(GetsBookAndInfoReq.class);
    }

    public void initialize() {
        tc.execute(() -> {
            t.server().listen(new Address(":10000"), (c) -> {
                c.handler(StoreSearchReq.class, (m) -> {
                    OrderIn oI = objs.get(m.id);
                    oI.updateTimestamp(System.currentTimeMillis());
                    Store x = (Store) oI.obj;
                    Book b = x.search(m.title);
                    int id_book = id.incrementAndGet();
                    objs.put(id_book, new OrderIn(b, System.currentTimeMillis()));
                    ObjRef ref = new ObjRef(address, id_book, "book");
                    return Futures.completedFuture(new StoreSearchRep(ref));
                });
                c.handler(StoreMakeCartReq.class, (m) -> {
                    OrderIn oI = objs.get(m.id);
                    oI.updateTimestamp(System.currentTimeMillis());
                    Store x = (Store) oI.obj;
                    Cart cart = x.newCart();
                    int idCart = id.incrementAndGet();
                    objs.put(idCart, new OrderIn(cart,System.currentTimeMillis()));
                    ObjRef ref = new ObjRef(address, idCart, "cart");
                    return Futures.completedFuture(new StoreMakeCartRep(ref));
                });
                c.handler(CartAddReq.class, (m) -> {
                    OrderIn oICart = objs.get(m.cartid);
                    oICart.updateTimestamp(System.currentTimeMillis());
                    OrderIn oIBook = objs.get(m.bookid);
                    oIBook.updateTimestamp(System.currentTimeMillis());
                    Cart cart = (Cart) oICart.obj;
                    Book book = (Book) oIBook.obj;
                    cart.add(book);
                    return Futures.completedFuture(new CartAddRep());
                });
                c.handler(CartBuyReq.class, (m) -> {
                    OrderIn oI = objs.get(m.cartid);
                    oI.updateTimestamp(System.currentTimeMillis());
                    Cart cart = (Cart) oI.obj;
                    boolean res = cart.buy();
                    objs.remove(m.cartid);
                    return Futures.completedFuture(new CartBuyRep(res));
                });
                c.handler(BookInfoReq.class, (m) -> {
                    OrderIn oI = objs.get(m.bookid);
                    oI.updateTimestamp(System.currentTimeMillis());
                    Book book = (Book) oI.obj;
                    BookInfoRep rep = null;
                    if(m.infoReq == 0) {
                        rep = new BookInfoRep(book.getIsbn());
                    }else if(m.infoReq == 1){
                        rep = new BookInfoRep(book.getTitle());
                    }else if(m.infoReq == 2){
                        rep = new BookInfoRep(book.getAuthor());
                    }
                    return Futures.completedFuture(rep);
                });
                c.handler(GetsBookAndInfoReq.class, (m) -> {
                   OrderIn oI = objs.get(m.storeId);
                   oI.updateTimestamp(System.currentTimeMillis());
                   Store x = (Store) oI.obj;
                   Book b = x.search(m.title);
                   int bookId = id.incrementAndGet();
                   objs.put(bookId, new OrderIn(b,System.currentTimeMillis()));
                   BookInfoRep rep = null;
                   if(m.infoReq == 0) {
                        rep = new BookInfoRep(b.getIsbn());
                   }else if(m.infoReq == 1){
                        rep = new BookInfoRep(b.getTitle());
                   }else if(m.infoReq == 2){
                        rep = new BookInfoRep(b.getAuthor());
                   }
                   return Futures.completedFuture(rep);
                });
            });
        });
    }

    public ObjRef exportObj(Object o){

        objs.put(id.incrementAndGet(), new OrderIn(o, System.currentTimeMillis()));

        if(o instanceof Store)
            return new ObjRef(address, id.get(), "store");
        else if(o instanceof Book)
            return new ObjRef(address, id.get(), "book");
        else if(o instanceof Cart)
            return new ObjRef(address, id.get(), "cart");

        return null;
    }

    public Object importObj(ObjRef o){

        if(o.cls.equals("store"))
            return new RemoteStore(tc, t, address);
        return null;
    }

}


class MonitorObjs extends Thread {
    private DistributedObjects disObj;
    public MonitorObjs (){
        this.disObj = new DistributedObjects();
    }

    @Override
    public void run(){
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Map<Integer, OrderIn> mapObjs = disObj.objs;
            for(Map.Entry<Integer,OrderIn> entry : mapObjs.entrySet()){
                long diffTime = Long.MAX_VALUE;
                if(entry.getValue().obj instanceof Book)
                    diffTime = 100000;
                else if(entry.getValue().obj instanceof Cart)
                    diffTime = 15000;
                if(System.currentTimeMillis() - entry.getValue().timeIn >= diffTime)
                    mapObjs.remove(entry.getKey());
                }
            disObj.objs = mapObjs;
        }
    }
}