package bookstore;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;

import bookstore.requests.StoreMakeCartRep;
import bookstore.requests.StoreMakeCartReq;
import bookstore.requests.StoreSearchRep;
import bookstore.requests.StoreSearchReq;

import java.util.concurrent.ExecutionException;

public class RemoteStore implements Store {
    private final SingleThreadContext tc;
    private final Connection c;
    private final Address address;
    private int id;

    public RemoteStore(SingleThreadContext tc, Transport t, Address address) {
        this.tc = tc;
        this.address = address;
        Connection connection = null;
        try {
            connection = tc.execute(() ->
                    t.client().connect(address)
            ).join().get();
        } catch(InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
        c = connection;
        id = 1; //to be solved
    }

    public Book search(String title, int txid) {
        StoreSearchRep r = null;
        try {
            r = (StoreSearchRep) tc.execute(() ->
                    c.sendAndReceive(new StoreSearchReq("one", id, txid))
            ).join().get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
        return (Book) Util.makeRemote(tc, r.ref);
    }

    public Cart newCart(int txid) {
        StoreMakeCartRep r = null;
        try {
            r = (StoreMakeCartRep) tc.execute(() ->
                    c.sendAndReceive(new StoreMakeCartReq(id, txid))
            ).join().get();
        } catch(InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
        if(r == null) return null;
        return (Cart) Util.makeRemote(tc, r.ref);
    }

}
