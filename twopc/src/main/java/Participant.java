import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;
import requests.*;

import java.util.List;

public class Participant {
    private Log log;
    private String resource;
    private Clique clique;
    private ThreadContext tc;
    private int coordId;
    private int currentTx;

    public Participant(Clique clique, int id, ThreadContext tc, int coordId) {
        this.log = new Log("log" + id);
        this.resource = "fakeStuff";
        this.clique = clique;
        this.coordId = coordId;
        this.tc = tc;
        defineHandlers();
    }

    public void listen(Address address) {
        Transport t = new NettyTransport();
        tc.execute(() -> {
            t.server().listen(address, (c) -> {
                c.handler(Integer.class, (m) -> {
                    currentTx = m;
                    System.out.println("Received from client");
                    clique.sendAndReceive(coordId, new NewParticipant(m))
                        .thenAccept(s -> c.send(s));
                });
            });
        });
    }


    private void defineHandlers() {
        tc.execute(() -> {
            // log handlers for participants
            log.handler(Prepare.class, (i, p) -> {
                //did not vote, lets send abort, dont know whats lost

            });
            log.handler(Commit.class, (i, com) -> {
                // nothing to do here
            });
            log.handler(String.class, (i, vot) -> {
                //means that has voted
                //wait for coord
            });
            log.open().thenRun(() -> {
                //do we need then run??
            });

            clique.handler(Prepare.class, (j, m) -> {
                log.append(m);
                System.out.println("Received prepare");
                clique.send(coordId, new Vote("COMMIT", currentTx));
                System.out.println("Voted");
                log.append("Voted");
            });

            clique.handler(Commit.class, (j, m) -> {
                log.append(m);
                System.out.print("Commited:"+resource);
            });

            clique.handler(Rollback.class, (j, m) -> {
                //rollback
                System.out.println("rollback");
            });
            clique.open().thenRun(() -> {
                System.out.println("testing");
            });
        }).join();
    }


}
