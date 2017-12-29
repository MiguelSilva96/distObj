
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;

import java.util.concurrent.atomic.AtomicInteger;

public class Simulation {
    private static int prepared = 0;
    private static final Integer GLOBAL_COMMIT = 1;
    private static final Integer ABORT = -1;
    private static final Integer START = 0;



    public static void main(String[] args) {
        Address[] addresses = new Address[] {
                new Address("localhost:12345"),
                new Address("localhost:12346"),
                new Address("localhost:12347")//coordinator
        };
        int id = Integer.parseInt(args[0]);
        Transport t = new NettyTransport();
        ThreadContext tc = new SingleThreadContext("proto-%d",
                                                    new Serializer());

        tc.serializer().register(Prepare.class);
        tc.serializer().register(Commit.class);
        tc.serializer().register(Rollback.class);

        tc.execute(() -> {
            Clique c = new Clique(t, id, addresses);
            Log l = new Log("log" + id);

            if(id != 2) {
                // log handlers for participants
                l.handler(Prepare.class, (i, p) -> {
                    // what to do to recover stuff
                    // how to check if it has voted
                });
                l.handler(Commit.class, (i, com) -> {
                    // nothing to do here
                });
                l.open().thenRun(() -> {
                    // dont know what to do here
                });
            }
            else {
                // log handlers for coordinator
                l.handler(Integer.class, (i, status) -> {
                   if(status == START) {
                       //repeat request
                       c.send(0, new Prepare());
                       c.send(1, new Prepare());
                   }
                });
                l.open().thenRun(() -> {
                    // dont know what to do here
                });
            }

            if(id != 2) {
                c.handler(Prepare.class, (j, m) -> {
                    c.send(j, "Prep");
                    l.append(m);
                });

                c.handler(Commit.class, (j, m) -> {
                    l.append(m);
                    //write stuff on disk
                });

                c.handler(Rollback.class, (j, m) -> {
                    //rollback
                });
            } else {
                c.handler(String.class, (j, m) -> {
                    if(m.equals("Prep")) {
                        prepared++;
                        if(prepared == 2) {
                            c.send(0, new Commit("cena"));
                            c.send(1, new Commit("cena"));
                            l.append(GLOBAL_COMMIT);
                            System.out.println("Commited!");
                        }
                        else if(m.equals("NotPrep")) {
                            if(j == 0)
                                c.send(1, new Rollback());
                            else
                                c.send(0, new Rollback());
                            l.append(ABORT);
                            System.out.println("THIS IS NOT A TEST; ABORT!!");
                        }
                    }
                });
            }

            c.open().thenRun(() -> {
                System.out.println("started");
                if(id == 2) {
                    l.append(START);
                    c.send(0, new Prepare("cena"));
                    c.send(1, new Prepare("cena"));
                }
            });

        }).join();
    }
}
