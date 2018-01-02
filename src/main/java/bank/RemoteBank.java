package bank;

import bank.requests.AddTxnRep;
import bank.requests.TokenReq;
import bank.requests.TxnRep;
import bank.requests.TxnReq;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;

import java.util.concurrent.ExecutionException;

public class RemoteBank {
    private final SingleThreadContext tc;
    private final Connection c;
    private final Address address;

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
    }

    public void send() {
        try {
            tc.execute(() ->
                    c.send(new TokenReq())
            ).join().get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void send(boolean res) {
        try {
            tc.execute(() ->
                    c.send(new AddTxnRep(res))
            ).join().get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
    }

    public boolean send_receive(int bankid, String iban, float price) {
        TxnRep rep = null;
        try {
            rep = (TxnRep) tc.execute(() ->
                    c.sendAndReceive(new TxnReq(bankid, iban, price))
            ).join().get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
        if(rep == null) return false;
        return rep.result;
    }
}
