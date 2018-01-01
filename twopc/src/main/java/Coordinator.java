
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;
import requests.*;

import java.util.HashMap;
import java.util.Map;

public class Coordinator {
    private Log log;
    private Clique clique;
    private Integer txid;
    private ThreadContext tc;
    private Map<Integer, Transaction> transactions;

    /*
    * Doubts:
    *   - Do we trim this log after commit??
    *   - if so we need to count appends maybe
    *   - Does the log need synchronized access?
    * IMPORTANT:
    *   - This version dows not have all logs
    *   - Transaction needs access to log
    *
    * */


    public Coordinator(Clique clique, int id, ThreadContext tc) {
        this.clique = clique;
        this.log = new Log("log"+id);
        this.tc = tc;
        this.txid = 0;
        this.transactions = new HashMap<>();
        handlers();
    }

    public void listen(Address address) {
        Transport t = new NettyTransport();
        tc.execute(() -> {
            t.server().listen(address, (c) -> {
                c.handler(Begin.class, (m) -> {
                    BeginRep rep;
                    int curId;
                    synchronized (txid) {
                        curId = txid;
                        rep = new BeginRep(txid++);
                    }
                    transactions.put(curId, new Transaction(clique, c));
                    log.append(rep); //rep because it has the txid, do we need this?
                    return Futures.completedFuture(rep);
                });
                c.handler(StartCommit.class, (m) -> {
                    Transaction tr = transactions.get(m.getTxid());
                    tr.firstPhase();
                    log.append(m);
                });
            });
        });
    }

    private void handlers() {
        tc.execute(() -> {
            log.handler(Integer.class, (i, status) -> {
                if(status == Simulation.START) {
                    //repeat request
                 //   send(new requests.Prepare());
                }
            });
            log.open().thenRun(() -> {
                // do we need something here?
            });

            clique.handler(NewParticipant.class, (j, m) -> {
                Transaction tr = transactions.get(m.getTxid());
                tr.addParticipant(j);
                return Futures.completedFuture("ok");
            });

            clique.handler(Vote.class, (j, m) -> {
                Transaction tr = transactions.get(m.getTxid());
                tr.voted(m, j);
            });

            clique.open().thenRun(() -> {
                System.out.println("started");
            });
        }).join();
    }

}
