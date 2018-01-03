package bank;

import bank.requests.*;
import bookstore.DistributedObjects;
import bookstore.ObjRef;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.netty.channel.AdaptiveRecvByteBufAllocator;

import java.util.concurrent.ExecutionException;

public class Queues {
    public static void main(String[] args) throws Exception {

        int id = Integer.parseInt(args[0]);

        SingleThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());
        Transport t = new NettyTransport();

        register(tc);

        Address listen, request, reply;
        if (id == 1) {
            System.out.println("Sou da Store");
            listen = new Address(":10001"); //store queue
            request = new Address("localhost", 10002); //requests bank queue
            reply = new Address("localhost", 10000); //replies to store

            initialize_store(tc, t, listen, request, reply);
        } else {
            System.out.println("Sou do Banco");
            listen = new Address(":10002"); //bank queue
            request = new Address("localhost", 10003); //requests bank
            reply = new Address("localhost", 10001); //replies to store queue

            initialize_bank(tc, t, listen, request, reply);
        }

    }

    public static void register(SingleThreadContext tc) {
        tc.serializer().register(TxnReq.class);
        tc.serializer().register(TxnRep.class);
    }

    public static void initialize_store(SingleThreadContext tc, Transport t, Address listen, Address request, Address reply) {
        tc.execute(() -> {
            t.server().listen(listen, (c) -> {
                c.handler(TxnReq.class, (m) -> {
                    Txn x = new Txn(m.bankid, m.iban, m.price);
                    send_req(t, request, x);
                });

                c.handler(TxnRep.class, (m) -> {
                    System.out.println("resposta do banco: " + m.result);
                    send_rep(t, reply, m.result);
                });
            });
        });
    }

    public static void initialize_bank(SingleThreadContext tc, Transport t, Address listen, Address request, Address reply) {
        tc.execute(() -> {
            t.server().listen(listen, (c) -> {
                c.handler(TxnReq.class, (m) -> {
                    DistributedObjects distObj = new DistributedObjects(request);
                    Bank bank = (Bank) distObj.importObj(new ObjRef(request, m.bankid, "bank"));
                    Account account = bank.search(m.iban);
                    boolean res = account.buy(m.price);
                    System.out.println("resposta do banco: " + res);
                    send_rep(t, reply, res);
                });
            });
        });
    }

    public static void send_req(Transport t, Address adr, Txn x) {

        SingleThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());

        Connection c = null;
        try {
            c = tc.execute(() ->
                    t.client().connect(adr)
            ).join().get();
        } catch(InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }

        try {
            Connection cc = c;
            tc.execute(() ->
                    cc.send(new TxnReq(x.bankid, x.iban, x.price))
            ).join().get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void send_rep(Transport t, Address adr, boolean res) {

        SingleThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());

        Connection c = null;
        try {
            c = tc.execute(() ->
                    t.client().connect(adr)
            ).join().get();
        } catch(InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }

        try {
            Connection cc = c;
            tc.execute(() ->
                    cc.send(new TxnRep(res))
            ).join().get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
    }
}


