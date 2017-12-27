
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;

import java.util.concurrent.atomic.AtomicInteger;

public class Participant {
    private static int prepared = 0, commited = 0;

    public static void main(String[] args) {
        Address[] addresses = new Address[] {
                new Address("localhost:12345"),
                new Address("localhost:12346"),
                new Address("localhost:12347")//coordenador
        };
        int id = Integer.parseInt(args[0]);
        final AtomicInteger leader = new AtomicInteger(-1);
        Transport t = new NettyTransport();
        ThreadContext tc = new SingleThreadContext("proto-%d", new Serializer());

        tc.serializer().register(Prepare.class);
        tc.serializer().register(Commit.class);

        tc.execute(() -> {
            Clique c = new Clique(t, id, addresses);
            Log l = new Log("log" + id);

            l.handler(Prepare.class, (i, p) -> {
                //
            });
            l.handler(Commit.class, (i, com) -> {
                //
            });
            l.open().thenRun(() -> {
                //
            });

            c.handler(Prepare.class, (j, m) -> {
                l.append(m);
                c.send(j, "Prep");
            });

            c.handler(Commit.class, (j, m) -> {
                l.append(m);
            });

            c.handler(Rollback.class, (j, m) -> {

            });

            c.handler(String.class, (j, m) -> {
                if(m.equals("T"))
                    c.send(j, new Prepare());
                else if(m.equals("Prep")) {
                    prepared++;
                    if (prepared == 2) {
                        c.send(0, new Commit());
                        c.send(1, new Commit());
                        prepared = 0;
                    }
                }
                else if(m.equals("NotPrep")) {
                    if(j == 0)
                        c.send(1, new Rollback());
                    else
                        c.send(0, new Rollback());
                }
            }).onException(e -> {
                // exception handler
            });

            c.open().thenRun(() -> {
                if(id < 2) {
                    c.send(2, "T");
                }
            });

        }).join();
    }
}
