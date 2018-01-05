package bank;

import bank.requests.*;
import mudar.Util;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;

import java.util.concurrent.ExecutionException;

public class RemoteBank implements Bank {
    private final SingleThreadContext tc;
    private final Connection c;
    private final Address address;
    private int id;

    public RemoteBank(SingleThreadContext tc, Transport t, Address address) {
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
        id = 1; //TODO
    }

    @Override
    public Account search(String iban) {
        BankSearchRep r = null;
        try {
            r = (BankSearchRep) tc.execute(() ->
                    c.sendAndReceive(new BankSearchReq(iban, id))
            ).join().get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
        return (Account) Util.makeRemote(tc, r.ref, id, null);

    }
}