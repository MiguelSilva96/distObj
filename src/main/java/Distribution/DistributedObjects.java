package Distribution;

import bank.Account;
import bank.Bank;
import bank.BankImpl;
import bank.RemoteBank;
import bank.requests.BankSearchRep;
import bank.requests.BankSearchReq;
import bank.requests.BankTxnRep;
import bank.requests.BankTxnReq;
import bookstore.*;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import bookstore.requests.*;
import pt.haslab.ekit.Clique;
import twopc.Coordinator;
import twopc.requests.*;
import bookstore.StoreImpl.CartImpl;
import bank.BankImpl.AccountImpl;
import utilities.ObjRef;
import utilities.OrderIn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedObjects {

    Map<Integer, OrderIn> objs;
    AtomicInteger id;
    SingleThreadContext tc;
    Transport t;
    Address addressStore, addressCoord, addressBank;
    Address[] addresses;
    public Clique clique;

    public DistributedObjects() {
        objs = new HashMap<>();
        id = new AtomicInteger(0);
        tc = new SingleThreadContext("srv-%d", new Serializer());
        t  = new NettyTransport();
        addressStore = new Address("localhost:10000");
        addressCoord = new Address("localhost:12348");

        //addresses for clique
        addresses = new Address[] {
                new Address("localhost:11111"),//store
                new Address("localhost:22222"),//bank
                new Address("localhost:33333") //coord
        };
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
        tc.serializer().register(GetsBookAndInfoReq.class);

        tc.serializer().register(BankSearchReq.class);
        tc.serializer().register(BankSearchRep.class);
        tc.serializer().register(BankTxnReq.class);
        tc.serializer().register(BankTxnRep.class);

        tc.serializer().register(ObjRef.class);

        tc.serializer().register(Prepare.class);
        tc.serializer().register(Commit.class);
        tc.serializer().register(Rollback.class);
        tc.serializer().register(Begin.class);
        tc.serializer().register(StartCommit.class);
        tc.serializer().register(TransactInfo.class);
        tc.serializer().register(Vote.class);
        tc.serializer().register(BeginRep.class);
        tc.serializer().register(NewParticipant.class);
        tc.serializer().register(Invoice.class);
        tc.serializer().register(LockLog.class);
    }

    public void initialize() {
        new MonitorObjs(objs).start();
        clique = new Clique(t, Clique.Mode.ANY, 0, addresses);

        tc.execute(() -> {
            StoreImpl store = (StoreImpl) objs.get(1).obj;
            store.setConnection(clique, 2);
            t.server().listen(new Address(":10000"), (c) -> {
                c.handler(StoreSearchReq.class, (m) -> {
                    OrderIn oI = objs.get(m.id);
                    oI.updateTimestamp(System.currentTimeMillis());
                    Store x = (Store) oI.obj;
                    Book b = x.search(m.title);
                    ObjRef ref = exportObj(b);
                    StoreSearchRep ssr = new StoreSearchRep(ref);
                    return Futures.completedFuture(ssr);
                });
                c.handler(StoreMakeCartReq.class, (m) -> {
                    OrderIn oI = objs.get(m.id);
                    oI.updateTimestamp(System.currentTimeMillis());
                    StoreImpl x = (StoreImpl) oI.obj;
                    Cart cart = x.newCart();
                    ObjRef ref = exportObj(cart);
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
                    CartImpl cart = (CartImpl) oI.obj;
                    cart.buy(m.txid, m.iban);
                    CompletableFuture<Object> cf = cart.getCf(m.txid);
                    CompletableFuture<CartBuyRep> rep = new CompletableFuture<>();
                    cf.thenAccept((s) -> {
                        cart.removeCf(m.txid);
                        rep.complete(new CartBuyRep((Boolean) s));
                    });
                    System.out.println("retornei");
                    return rep;
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
                    System.out.println("hello");
                    return Futures.completedFuture(rep);
                });
            });
        });
    }

    public void initialize_bank() {
        clique = new Clique(t, Clique.Mode.ANY, 1, addresses);

        tc.execute(() -> {
            BankImpl bi = (BankImpl) objs.get(1).obj;
            bi.setConnection(clique, 2);
            clique.handler(BankSearchReq.class, (j, m) -> {
                Bank x = (Bank) objs.get(m.id).obj;
                Account a = x.search(m.iban);
                ObjRef ref = exportObj(a);
                return Futures.completedFuture(new BankSearchRep(ref));
            });

            clique.handler(BankTxnReq.class, (j, m) -> {
                OrderIn oIAccount = objs.get(m.accountid);
                oIAccount.updateTimestamp(System.currentTimeMillis());
                AccountImpl a = (AccountImpl) oIAccount.obj;
                //AccountImpl a = (AccountImpl) objs.get(m.accountid);
                a.buy(m.price, m.txid);
                CompletableFuture<Object> cf = a.getCf(m.txid);
                CompletableFuture<BankTxnRep> rep = new CompletableFuture<>();
                cf.thenAccept((s) -> {
                    a.removeCf(m.txid);
                    rep.complete(new BankTxnRep((Boolean) s));
                });
                return rep;
            });

        }).join();

    }


    public void initializeCoordinator() {
        clique = new Clique(t, Clique.Mode.ANY, 2, addresses);

        (new Coordinator(clique, 2, tc)).listen(new Address(":12348"));
    }

    public int beginTransaction() {
        Connection connection;
        try {
            connection = tc.execute(() ->
                    t.client().connect(addressCoord)
            ).join().get();
            BeginRep rep = (BeginRep) tc.execute(() ->
                    connection.sendAndReceive(new Begin())
            ).join().get();
            return rep.getTxid();
        } catch (ExecutionException|InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean commitTransaction(int txid) {
        Connection connection;
        try {
            connection = tc.execute(() ->
                    t.client().connect(addressCoord)
            ).join().get();
            Object o = tc.execute(() ->
                    connection.sendAndReceive(new StartCommit(txid))
            ).join().get();
            if(o instanceof Commit) return true;
            else return false;
        } catch (ExecutionException|InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ObjRef exportObj(Object o){

        objs.put(id.incrementAndGet(), new OrderIn(o, System.currentTimeMillis()));

        if(o instanceof Store)
            return new ObjRef(addressStore, id.get(), "store");
        else if(o instanceof Book)
            return new ObjRef(addressStore, id.get(), "book");
        else if(o instanceof Cart)
            return new ObjRef(addressStore, id.get(), "cart");

        else if(o instanceof Bank)
            return new ObjRef(addressBank, id.get(), "bank");
        else if(o instanceof Account)
            return new ObjRef(addressBank, id.get(), "account");

        return null;
    }

    public Object importObj(ObjRef o){

        if(o.cls.equals("store"))
            return new RemoteStore(tc, t, addressStore);

        if(o.cls.equals("bank")){
            return new RemoteBank(tc, clique);
        }

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

            it = objs.entrySet().iterator();

            while (it.hasNext()) {
                entry = it.next();
                long diffTime = Long.MAX_VALUE;

                if(entry.getValue().obj instanceof Book)
                    diffTime = 3600000 ;
                else if(entry.getValue().obj instanceof Cart)
                    diffTime = 3600000;

                if(System.currentTimeMillis() - entry.getValue().timeIn >= diffTime){
                    System.out.println(objs.values().toString());
                    it.remove();
                }
            }
        }
    }
}

