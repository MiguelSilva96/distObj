package bookstore;

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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedObjects {

    Map<Integer, Object> objs;
    AtomicInteger id;
    SingleThreadContext tc;
    Transport t;
    Address addressStore, addressCoord, addressBank;
    Address[] addresses;
    Clique clique;

    public DistributedObjects() {
        objs = new HashMap<>();
        id = new AtomicInteger(0);
        tc = new SingleThreadContext("srv-%d", new Serializer());
        t  = new NettyTransport();
        addressStore = new Address("localhost:10000");
        addressCoord = new Address("localhost:12348");

        //addresses for clique
        addresses = new Address[] {
                new Address("localhost:11111"), //store
                new Address("localhost:22222") //coord
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
    }

    public void initialize() {

        clique = new Clique(t, Clique.Mode.ANY, 0, addresses);

        tc.execute(() -> {
            StoreImpl store = (StoreImpl) objs.get(1);
            store.setConnection(clique, 1);
            t.server().listen(new Address(":10000"), (c) -> {
                c.handler(StoreSearchReq.class, (m) -> {
                    StoreImpl x = (StoreImpl) objs.get(m.id);
                    Book b = x.search(m.title, m.txid);
                    final int txid = m.txid;
                    int id_book = id.incrementAndGet();
                    objs.put(id_book, b);
                    ObjRef ref = new ObjRef(addressStore, id_book, "book");
                    CompletableFuture<Object> cf = x.getCf(m.txid);
                    CompletableFuture<StoreSearchRep> res = new CompletableFuture<>();
                    StoreSearchRep ssr = new StoreSearchRep(ref);
                    if(cf == null) return Futures.completedFuture(ssr);
                    cf.thenAccept((s) -> {
                        x.removeCf(txid);
                        res.complete(ssr);
                    });
                    return res;
                });
                c.handler(StoreMakeCartReq.class, (m) -> {
                    StoreImpl x = (StoreImpl) objs.get(m.id);
                    Cart cart = x.newCart(m.txid);
                    int idCart = id.incrementAndGet();
                    final int txid = m.txid;
                    objs.put(idCart, cart);
                    ObjRef ref = new ObjRef(addressStore, idCart, "cart");
                    CompletableFuture<Object> cf = x.getCf(m.txid);
                    CompletableFuture<StoreMakeCartRep> res = new CompletableFuture<>();
                    cf.thenAccept((s) -> {
                        x.removeCf(txid);
                        res.complete(new StoreMakeCartRep(ref));
                    });
                    return res;

                });
                c.handler(CartAddReq.class, (m) -> {
                    Cart cart = (Cart) objs.get(m.cartid);
                    Book book = (Book) objs.get(m.bookid);
                    cart.add(book, m.txid);
                    return Futures.completedFuture(new CartAddRep());
                });
                c.handler(CartBuyReq.class, (m) -> {
                    CartImpl cart = (CartImpl) objs.get(m.cartid);
                    cart.buy(m.txid);
                    CompletableFuture<Object> cf = cart.getCf(m.txid);
                    CompletableFuture<CartBuyRep> rep = new CompletableFuture<>();
                    cf.thenAccept((s) -> {
                        cart.removeCf(m.txid);
                        rep.complete(new CartBuyRep((Boolean) s));
                    });
                    return rep;
                });
                c.handler(BookInfoReq.class, (m) -> {
                    Book book = (Book) objs.get(m.bookid);
                    int isbn = book.getIsbn();
                    String title = book.getTitle();
                    String author = book.getAuthor();
                    BookInfoRep rep = new BookInfoRep(isbn, title, author);
                    return Futures.completedFuture(rep);
                });
            });
        });
    }



    public void initializeCoordinator() {
        clique = new Clique(t, Clique.Mode.ANY, 1, addresses);
        (new Coordinator(clique, 1, tc)).listen(new Address(":12348"));
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

        objs.put(id.incrementAndGet(), o);

        if(o instanceof Store)
            return new ObjRef(addressStore, id.get(), "store");
        else if(o instanceof Book)
            return new ObjRef(addressStore, id.get(), "book");
        else if(o instanceof Cart)
            return new ObjRef(addressStore, id.get(), "cart");

        return null;
    }

    public Object importObj(ObjRef o){

        if(o.cls.equals("store"))
            return new RemoteStore(tc, t, addressStore);
        return null;

    }
}
