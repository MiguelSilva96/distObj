package twopc;

import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Address;

import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;
import twopc.requests.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Coordinator {
    private Log log;
    private Clique clique;
    private Integer txid;
    private ThreadContext tc;
    private Map<Integer, Transaction> transactions;

    /*
    * Doubts:
    *   - Does the log need synchronized access?
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
                    transactions.put(curId, new Transaction(clique));
                    log.append(rep);
                    return Futures.completedFuture(rep);
                });
                c.handler(StartCommit.class, (m) -> {
                    Transaction tr = transactions.get(m.getTxid());
                    CompletableFuture<Object> compFuture = new CompletableFuture<>();
                    tr.firstPhase(compFuture, m.getTxid());
                    List<Integer> part = tr.getParticipants();
                    TransactInfo tinf = new TransactInfo(m.getTxid(), part);
                    tinf.setCompletedCommit(compFuture);
                    StartCommit scLog = new StartCommit(tinf);
                    log.append(scLog);
                    return compFuture;
                });
            });
        });
    }

    private void handlers() {
        tc.execute(() -> {
            log.handler(Begin.class, (i, b) -> {
                TransactInfo ti = b.getTransactInfo();
                int txid = ti.getTxid();
                transactions.put(txid, new Transaction(clique));
            });
            log.handler(NewParticipant.class, (i, n) -> {
                Transaction t = transactions.get(n.getTxid());
                t.addParticipant(n.getParticipant());
            });
            log.handler(StartCommit.class, (i, sc) -> {
                TransactInfo tinfo = sc.getTransactInfo();
                Transaction t = transactions.get(tinfo.getTxid());
                t.setParticipants(tinfo.getParticipants());
                t.setCompletedCommit(tinfo.getCompletedCommit());
            });
            log.handler(Commit.class, (i, c) -> {
                // take info this can be trimmed from log
                transactions.remove(c.getTxid());
            });
            log.handler(Rollback.class, (i, c) -> {
                // take info this can be trimmed from log
                transactions.remove(c.getTxid());
            });
            log.open().thenRun(() -> {
                // restart current phase of each one;
                transactions.forEach((k, v) -> {
                    int phase = v.getPhase();
                    if(phase == 1)
                        // restart first phase
                        v.firstPhase(v.getCompletedCommit(), k);
                    else if(phase == 0)
                        // send abort to all the resources involved
                        v.abort(k);
                    // what to do about 2nd phase
                    // being on 2nd phase means it already
                    // commited/rolledback
                });
            });

            clique.handler(NewParticipant.class, (j, m) -> {
                Transaction tr = transactions.get(m.getTxid());
                tr.addParticipant(j);
                return Futures.completedFuture("ok");
            });

            clique.handler(Vote.class, (j, m) -> {
                Transaction tr = transactions.get(m.getTxid());
                Object res = tr.voted(m, j);
                if(res != null) {
                    // means commit or rollback happened
                    log.append(res);
                }
            });

            clique.open().thenRun(() -> System.out.println("open"));

        }).join();
    }

}
