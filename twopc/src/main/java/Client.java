import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import requests.*;

import java.util.concurrent.ExecutionException;



public class Client {

    public static void register(ThreadContext tc) {
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

    public static void main(String[] args) {
        Address coordAddr = new Address("localhost:12348");
        Address res1 = new Address("localhost:12349");
        Address res2 = new Address("localhost:12341");
        ThreadContext tc = new SingleThreadContext("client",
                                                new Serializer());
        Transport t  = new NettyTransport();
        Connection connection, connection1, connection2;
        register(tc);
        try {
            connection = tc.execute(() ->
                    t.client().connect(coordAddr)
            ).join().get();
            tc.execute(() -> {
               connection.handler(Commit.class, (c) -> {
                   System.out.println("Transaction commit");
               });
               connection.handler(Rollback.class, (c) -> {
                   System.out.println("Transaction rollback");
               });
            });
            // BEGIN
            BeginRep rep = (BeginRep) tc.execute(() ->
                    connection.sendAndReceive(new Begin())
            ).join().get();
            int txid = rep.getTxid();
            connection1 = tc.execute(() ->
                    t.client().connect(res1)
            ).join().get();
            connection2 = tc.execute(() ->
                    t.client().connect(res2)
            ).join().get();

            tc.execute(() -> {
               connection1.handler(String.class, (s) -> {
                   //access second resource
                   connection2.send(txid);
               });
               connection2.handler(String.class, (s) -> {
                   //commit
                   connection.send(new StartCommit(txid));
               });
            });

            // SIMULATE ACCESS TO ONE RESOURCE
            tc.execute(() ->
                    connection1.send(txid)
            );

        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
            return;
        }


    }
}