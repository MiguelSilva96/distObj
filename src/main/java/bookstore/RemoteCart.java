package bookstore;

import bank.Account;
import bank.Bank;
import bookstore.requests.CartBuyRep;
import bookstore.requests.CartBuyReq;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import bookstore.requests.CartAddRep;
import bookstore.requests.CartAddReq;
import mudar.DistributedObjects;
import mudar.ObjRef;

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

        try {
            r = (CartBuyRep) tc.execute(() ->
                    c.sendAndReceive(new CartBuyReq(id))
            ).join().get();
        } catch (InterruptedException|ExecutionException e){
            e.printStackTrace();
        }
        System.out.println("ola");

        if(r.result == false) return false;
        else return send2bank();
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

    private boolean send2bank() {
        Address bank_address = new Address("localhost", 10003);

        DistributedObjects distObj = new DistributedObjects(bank_address);
        Bank bank = (Bank) distObj.importObj(new ObjRef(bank_address, 1, "bank"));
        Account account = bank.search("PT12345");
        boolean res = account.buy(100);
        System.out.println("resposta do banco: " + res);
        return res;
    }
}