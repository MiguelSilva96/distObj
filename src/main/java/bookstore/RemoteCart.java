package bookstore;

import bookstore.requests.CartBuyRep;
import bookstore.requests.CartBuyReq;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import bookstore.requests.CartAddRep;
import bookstore.requests.CartAddReq;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.concurrent.ExecutionException;

public class RemoteCart implements Cart {
    private final SingleThreadContext tc;
    private Connection c;
    private Address address;
    private int id;

    public RemoteCart(SingleThreadContext tc, Address address, int id) {
        this.tc = tc;
        this.address = address;
        Transport t = new NettyTransport();
        try {
            c = tc.execute(() ->
                    t.client().connect(address)
            ).join().get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
            c = null;
        }
        this.id = id;
    }

    public boolean buy() {
        CartBuyRep r = null;
        try{
            r = (CartBuyRep) tc.execute(()->
                    c.sendAndReceive(new CartBuyReq(id))
            ).join().get();
        }catch (InterruptedException|ExecutionException e){
            e.printStackTrace();
        }
        return r.result;
    }

    public void add(Book b) {
        RemoteBook book = (RemoteBook) b;
        try {
            CartAddRep r = (CartAddRep) tc.execute(() ->
                    c.sendAndReceive(new CartAddReq(book.id, id))
            ).join().get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
    }
}