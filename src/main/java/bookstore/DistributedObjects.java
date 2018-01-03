package bookstore;

import bank.*;
import bank.requests.*;
import bookstore.requests.*;

import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedObjects {

    Map<Integer, Object> objs;
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
    }

    public DistributedObjects(Address address) {
        objs = new HashMap<>();
        id = new AtomicInteger(0);
        tc = new SingleThreadContext("srv-%d", new Serializer());
        t  = new NettyTransport();
        this.address = address;
        register();
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

        tc.serializer().register(BankSearchReq.class);
        tc.serializer().register(BankSearchRep.class);
        //tc.serializer().register(AccountInfoReq.class);
        //tc.serializer().register(AccountInfoRep.class);
        tc.serializer().register(BankTxnReq.class);
        tc.serializer().register(BankTxnRep.class);

        tc.serializer().register(ObjRef.class);

        tc.serializer().register(TxnReq.class);
        tc.serializer().register(TxnRep.class);
    }

    public void initialize() {
        tc.execute(() -> {
            t.server().listen(new Address(":10000"), (c) -> {
                c.handler(StoreSearchReq.class, (m) -> {
                    Store x = (Store) objs.get(m.id);
                    Book b = x.search(m.title);
                    int id_book = id.incrementAndGet();
                    objs.put(id_book, b);
                    ObjRef ref = new ObjRef(address, id_book, "book");
                    return Futures.completedFuture(new StoreSearchRep(ref));
                });
                c.handler(StoreMakeCartReq.class, (m) -> {
                    StoreImpl x = (StoreImpl) objs.get(m.id);
                    Cart cart = x.newCart();
                    int idCart = id.incrementAndGet();
                    objs.put(idCart, cart);
                    ObjRef ref = new ObjRef(address, idCart, "cart");
                    return Futures.completedFuture(new StoreMakeCartRep(ref));
                });
                c.handler(CartAddReq.class, (m) -> {
                    Cart cart = (Cart) objs.get(m.cartid);
                    Book book = (Book) objs.get(m.bookid);
                    cart.add(book);
                    return Futures.completedFuture(new CartAddRep());
                });
                c.handler(CartBuyReq.class, (m) -> {
                    Cart cart = (Cart) objs.get(m.cartid);
                    boolean res = cart.buy();
                    return Futures.completedFuture(new CartBuyRep(res));
                });
                c.handler(BookInfoReq.class, (m) -> {
                    Book book = (Book) objs.get(m.bookid);
                    int isbn = book.getIsbn();
                    String title = book.getTitle();
                    String author = book.getAuthor();
                    BookInfoRep rep = new BookInfoRep(isbn, title, author);
                    return Futures.completedFuture(rep);
                });

                c.handler(TxnRep.class, (m) -> {
                    System.out.println("O banco disse: " + m.result);
                });

            });
        });
    }

    public void initialize_bank() {
        address = new Address("localhost:10003");
        tc.execute(() -> {
            t.server().listen(new Address(":10003"), (c) -> {
                c.handler(BankSearchReq.class, (m) -> {
                    Bank x = (Bank) objs.get(m.id);
                    Account a = x.search(m.iban);
                    int id_account = id.incrementAndGet();
                    objs.put(id_account, a);
                    ObjRef ref = new ObjRef(address, id_account, "account");
                    return Futures.completedFuture(new BankSearchRep(ref));
                });
                /*
                c.handler(AccountInfoReq.class, (m) -> {
                    System.out.println("ola");
                    Account a = (Account) objs.get(m.accountid);
                    String iban = a.getIban();
                    //String titular = a.getTitular();
                    //float price = a.getBalance();
                    return Futures.completedFuture(new AccountInfoRep(iban));
                });
                */
                c.handler(BankTxnReq.class, (m) -> {
                    Account a = (Account) objs.get(m.id);
                    boolean res = a.buy(m.price);
                    return Futures.completedFuture(new BankTxnRep(res));
                });
                /*
                c.handler(TxnReq.class, (m) -> {
                    Bank x = (Bank) objs.get(m.bankid);
                    Account a = x.search(m.iban);
                    boolean res = a.buy(m.price);

                    send_rep(t, new Address("localhost", 10002), res);
                });
                */
            });
        });
    }

    public ObjRef exportObj(Object o){

        objs.put(id.incrementAndGet(), o);

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
