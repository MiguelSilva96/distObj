package mudar;

import bank.*;
import bank.requests.*;
import bookstore.*;
import bookstore.requests.*;

import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedObjects {

    public Map<Integer, OrderIn> objs;
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
        new MonitorObjs(objs).start();
    }

    public DistributedObjects(Address address) {
        objs = new HashMap<>();
        id = new AtomicInteger(0);
        tc = new SingleThreadContext("srv-%d", new Serializer());
        t  = new NettyTransport();
        this.address = address;
        register();
        new MonitorObjs(objs).start();
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
        tc.serializer().register(GetsBookAndInfoReq.class);

        tc.serializer().register(BankSearchReq.class);
        tc.serializer().register(BankSearchRep.class);
        tc.serializer().register(BankTxnReq.class);
        tc.serializer().register(BankTxnRep.class);

        tc.serializer().register(ObjRef.class);
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

    public void initialize_bank() {
        tc.execute(() -> {
            t.server().listen(new Address(":10003"), (c) -> {
                c.handler(BankSearchReq.class, (m) -> {
                    OrderIn oI = objs.get(m.id);
                    oI.updateTimestamp(System.currentTimeMillis());
                    Bank x = (Bank) oI.obj;
                    Account a = x.search(m.iban);
                    int id_account = id.incrementAndGet();
                    objs.put(id_account, new OrderIn(a, System.currentTimeMillis()));
                    ObjRef ref = new ObjRef(address, id_account, "account");
                    return Futures.completedFuture(new BankSearchRep(ref));
                });
                c.handler(BankTxnReq.class, (m) -> {
                    OrderIn oIAccount = objs.get(m.accountid);
                    oIAccount.updateTimestamp(System.currentTimeMillis());
                    Account a = (Account) oIAccount;
                    boolean res = a.buy(m.price);
                    objs.remove(m.accountid);
                    return Futures.completedFuture(new BankTxnRep(res));
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

        else if(o instanceof Bank)
            return new ObjRef(address, id.get(), "bank");
        else if(o instanceof Account)
            return new ObjRef(address, id.get(), "account");

        return null;
    }

    public Object importObj(ObjRef o){

        if(o.cls.equals("store"))
            return new RemoteStore(tc, t, address);
        if(o.cls.equals("bank"))
            return new RemoteBank(tc, t, address);

        return null;
    }

}


class MonitorObjs extends Thread {

    private Map<Integer, OrderIn> objs;

    private Iterator<Map.Entry<Integer, OrderIn>> it;
    private Map.Entry<Integer, OrderIn> entry;

    public MonitorObjs (Map<Integer, OrderIn> objs){
        this.objs = objs;
    }

    @Override
    public void run(){
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(objs.size());

            it = objs.entrySet().iterator();

            while (it.hasNext()) {
                entry = it.next();
                long diffTime = Long.MAX_VALUE;

                if(entry.getValue().obj instanceof Book)
                    diffTime = 100000;
                else if(entry.getValue().obj instanceof Cart)
                    diffTime = 15000;

                if(System.currentTimeMillis() - entry.getValue().timeIn >= diffTime){
                    System.out.println("vais embora");
                    it.remove();
                }
            }
        }
    }
}
