package bank;

import bank.requests.*;
import bookstore.requests.CartAddReq;
import bookstore.requests.StoreMakeCartReq;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.concurrent.ExecutionException;

public class RemoteAccount implements Account {

    private final SingleThreadContext tc;
    private Connection c;
    private Address address;
    public int id;

    public RemoteAccount(SingleThreadContext tc, Address address, int id) {
        this.tc = tc;
        this.address = address;
        Transport t = new NettyTransport();
        try {
            c = tc.execute(() ->
                    t.client().connect(address)
            ).join().get();
        } catch(InterruptedException|ExecutionException e) {
            e.printStackTrace();
            c = null;
        }
        this.id = id;
    }

    @Override
    public String getIban() {
        return "";
    }

    @Override
    public boolean buy(float price) {
        BankTxnRep rep = null;
        try {
            rep = (BankTxnRep) tc.execute(() ->
                    c.sendAndReceive(new BankTxnReq(id, price))
            ).join().get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
        return rep.result;
    }
}