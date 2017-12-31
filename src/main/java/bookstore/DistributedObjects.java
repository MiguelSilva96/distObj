package bookstore;

import bookstore.bank.*;
import bookstore.requests.*;

import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

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

        tc.serializer().register(AddTxnReq.class);
        tc.serializer().register(TokenReq.class);
        tc.serializer().register(AddTxnRep.class);
        tc.serializer().register(TxnReq.class);
        tc.serializer().register(TxnRep.class);

        tc.serializer().register(ObjRef.class);
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
            });
        });
    }

    public void bank_requests() {
        tc.execute(() -> {
            t.server().listen(new Address(":10002"), (c) -> {
                c.handler(TxnReq.class, (m) -> {
                    System.out.println("4. sou o banco e recebi cenas");

                    System.out.println(m.bankid +","+ m.iban +","+ m.price);
                    Bank x = (Bank) objs.get(m.bankid);
                    Account a = x.search(m.iban);
                    boolean res = a.buy(m.price);

                    System.out.println("5. sou o banco e tenho a resposta");
                    return Futures.completedFuture(new TxnRep(res));
                });
            });
        });
    }

    //comunica com o banco
    public void listen_rm(Queue<Txn> qq) {
        address = new Address("localhost", 10002);
        tc.execute(() -> {
            t.server().listen(new Address(":10001"), (c) -> {
                c.handler(TokenReq.class, (m) -> {
                    RemoteBank rb = new RemoteBank(tc, t, address);

                    System.out.println("2. recebi sinal da queue");
                    Txn x = qq.remove();

                    //envia para o banco
                    System.out.println("3. enviei cenas para o banco");
                    boolean res = rb.send_receive(1, x.iban, x.price);

                    //envia para a queue
                    System.out.println("6. enviei resposta do banco");
                    rb.send(res);
                });
            });
        });
    }

    //comunica com a loja
    public void listen_add(Queue<Txn> qq) {
        address = new Address("localhost", 10001);
        RemoteBank rb = new RemoteBank(tc, t, address);
/*
        tc.execute(() -> {
            t.server().listen(new Address(":10000"), (c) -> {

                //receber tnx da loja
                c.handler(AddTxnReq.class, (m) -> {
                    System.out.println("1. recebi uma ordem da loja");
                    qq.add(new Txn(m.iban, m.price));
                    rb.send(m.iban, m.price);
                });

                //enviar tnx para loja
                c.handler(AddTxnRep.class, (m) -> {
                    System.out.println("7. recebi a resposta final");
                    //return Futures.completedFuture(m);
                });
            });
        });
*/

        System.out.println("1. sou a loja e enviei cenas");
        qq.add(new Txn("PT12345", 2));
        rb.send();
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

        return null;
    }

    public Object importObj(ObjRef o){

        if(o.cls.equals("store"))
            return new RemoteStore(tc, t, address);

        return null;
    }
}
