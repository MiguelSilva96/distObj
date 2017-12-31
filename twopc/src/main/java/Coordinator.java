import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;

public class Coordinator {
    private Log log;
    private Clique clique;
    private int id, prepared;
    private ThreadContext tc;

    public Coordinator(Clique clique, int id, ThreadContext tc) {
        this.id = id;
        this.clique = clique;
        this.log = new Log("log"+id);
        this.tc = tc;
        this.prepared = 0;
        defineHandlers();
    }

    private void defineHandlers() {
        tc.execute(() -> {
            log.handler(Integer.class, (i, status) -> {
                if(status == Simulation.START) {
                    //repeat request
                    send(new Prepare());
                }
            });
            log.open().thenRun(() -> {
                // do we need something here?
            });

            clique.handler(String.class, (j, m) -> {
                if(m.equals("Prep")) {
                    prepared++;
                    if(prepared == 2) {
                        send(new Commit());
                        log.append(Simulation.GLOBAL_COMMIT);
                        System.out.println("Commited!");
                    }
                    else if(m.equals("NotPrep")) {
                        send(new Rollback());
                        log.append(Simulation.ABORT);
                        System.out.println("ABORT!");
                    }
                }
            });
            clique.open().thenRun(() -> {
                System.out.println("started");
                log.append(Simulation.START);
                send(new Prepare());
            });
        }).join();
    }

    public void send(Object message) {
        clique.send(0, message);
        clique.send(1, message);
    }
}
