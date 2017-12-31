import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;

public class Participant {
    private Log log;
    private String resource;
    private Clique clique;
    private ThreadContext tc;
    private int coordId;

    public Participant(Clique clique, int id, ThreadContext tc, int coordId) {
        this.log = new Log("log" + id);
        this.resource = "fakeStuff";
        this.clique = clique;
        this.coordId = coordId;
        this.tc = tc;
        defineHandlers();
    }

    private void defineHandlers() {
        tc.execute(() -> {
            // log handlers for participants
            log.handler(Prepare.class, (i, p) -> {
                //did not vote, lets send abort, dont know whats lost
                clique.send(coordId, "NotPrep");
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
                clique.send(j, "Prep");
                log.append("Voted");
            });

            clique.handler(Commit.class, (j, m) -> {
                log.append(m);
                System.out.print("Commited:"+resource);
            });

            clique.handler(Rollback.class, (j, m) -> {
                //rollback
            });
            clique.open().thenRun(() -> {
                System.out.println("testing");
            });
        }).join();
    }


}
