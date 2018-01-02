import Requests.Transaction;
import io.atomix.catalyst.concurrent.ThreadContext;
import pt.haslab.ekit.Clique;
import java.util.*;

/*
need to check deadlocks
check queue removal (not done)
check wait id's on LockTable
*/
public class Coordinator {
    private Clique c;
    private ThreadContext tc;
    private Map<String, LockTable> lockTable;
    private Map<Integer, TransacTable> transacTable;
    private Map<String, ArrayList<Integer>> waitingQ;
    private int tId;

    public Coordinator(Clique c, ThreadContext tc){
        this.c = c;
        this.tc = tc;
        this.lockTable = new HashMap<>();
        this.transacTable = new HashMap<>();
        this.waitingQ = new HashMap<>();
        this.tId = 0;
        handlers();
    }

    public void handlers(){
        this.tc.execute(() -> {
            c.handler(Transaction.class, (i, m) -> {
                int transacID = tId;
                tId++;
                TransacTable tt = new TransacTable(m.resources, transacID);
                transacTable.put(transacID, tt);
                int state = -2;
                switch (m.RW){
                    case -1:
                        char removedLock = release(m.resources, transacID);
                        actionRealease(m.resources, removedLock, i);
                        state = -1;
                        break;
                    case 0:
                        state = read(m.resources, transacID);
                        break;
                    case 1:
                        state = write(m.resources, transacID);
                        break;
                }
                if(state == 1){
                    c.send(i, "Lock acquired");
                }else if(state == -1){
                    c.send(i,"Lock released");
                }
            });
        });
    }

    public void actionRealease(String resources, char removedLock, int sender){
        ArrayList<Integer> tIDs = waitingQ.get(resources);
        int state;
        if(removedLock == 'r'){
            for(int id : tIDs){
                state = read(resources, id);
                if(state == 1)
                    c.send(sender, "Lock acquired");
            }
        }
        if(removedLock == 'w'){
            for(int id : tIDs){
                state = write(resources, id);
                if(state == 1)
                    c.send(sender, "Lock acquired");
            }
        }
    }

//release lockTable transaction needed
    public char release (String resources, int tID) {
        transacTable.get(tID).setResourcesLocked("");
        transacTable.get(tID).setTransacState(-1);//-1 means transaction done
        char c = lockTable.get(resources).remove(tID);
        return c;
        //lockedQ.remove(tID);
    }

    public int read(String resources, int tID){
        int result = 1;
        if(!lockTable.containsKey(resources)){
            LockTable lt = new LockTable();
            lt.setTransacID_r(tID);
            lockTable.put(resources, lt);
        }else{
            LockTable lt = lockTable.get(resources);
            if(lt.getTransacID_w().size() != 0){
                if(waitingQ.containsKey(resources)){
                    waitingQ.get(resources).add(tID);
                }else{
                    ArrayList<Integer> a = new ArrayList<>();
                    a.add(tID);
                    waitingQ.put(resources, a);
                }
                transacTable.get(tID).setTransacState(0);
                lt.setTransacID_w_wait(tID);
                result = 0;
            }

        }
        return result;
    }

    public int write(String resources, int tID){
        int result = 1;

        if(!lockTable.containsKey(resources)){
            LockTable lt = new LockTable();
            lt.setTransacID_w(tID);
            lockTable.put(resources, lt);
        }else{
            LockTable lt = lockTable.get(resources);
            if(lt.getTransacID_w().size() != 0){
                if(waitingQ.containsKey(resources)){
                    waitingQ.get(resources).add(tID);
                }else{
                    ArrayList<Integer> a = new ArrayList<>();
                    a.add(tID);
                    waitingQ.put(resources, a);
                }
                transacTable.get(tID).setTransacState(0);
                lt.setTransacID_w_wait(tID);
                result = 0;
            }
            if(lt.getTransacID_r().size() != 0){
                if(waitingQ.containsKey(resources)){
                    waitingQ.get(resources).add(tID);
                }else{
                    ArrayList<Integer> a = new ArrayList<>();
                    a.add(tID);
                    waitingQ.put(resources, a);
                }
                transacTable.get(tID).setTransacState(0);
                lt.setTransacID_r_wait(tID);
                result = 0;
            }
        }
        return result;
    }

}
