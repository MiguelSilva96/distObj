package bank;

import bank.requests.*;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.concurrent.ExecutionException;

public class FakeClient {
    public static void main(String[] args) throws Exception {

        SingleThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());
        Transport t = new NettyTransport();
        Address adr = new Address("localhost", 10002);

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
                    cc.send(new TxnReq(1, "PT12345", 123))
            ).join().get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
    }

}
