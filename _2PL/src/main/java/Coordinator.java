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
    private Map<Integer, Integer> allowLocks;
    private int tId;

    public Coordinator(Clique c, ThreadContext tc){
        this.c = c;
        this.tc = tc;
        this.lockTable = new HashMap<>();
        this.transacTable = new HashMap<>();
        this.waitingQ = new HashMap<>();
        this.tId = 0;
        this.allowLocks = new HashMap<>();
        handlers();
    }

    public void handlers(){
        this.tc.execute(() -> {
            c.handler(Transaction.class, (i, m) -> {
                int transacID = tId;
                tId++;
                TransacTable tt = new TransacTable(m.resources, transacID, i);
                transacTable.put(transacID, tt);
                int state = -2;
                switch (m.RW){
                    case 0:
                        if(!allowLocks.containsKey(i))
                            allowLocks.put(i, 1);

                        if(allowLocks.get(i) == 1){
                            state = acquire_lock(m.resources, transacID);
                            if(state == 1)
                                c.send(i, "Lock acquired");
                            else c.send(i, "Waiting for lock to be available");
                        }else c.send(i, "Lock request after release lock");

                        break;
                    case 1:
                        release(m.resources, transacID);
                        action_release(m.resources);
                        c.send(i, "Lock released");
                        allowLocks.put(i, 0);
                        break;
                }
            });
        });
    }


    public void action_release(String resources){
        ArrayList<Integer> tIDs = waitingQ.get(resources);
        int state;
        for(int id : tIDs){
            state = acquire_lock(resources, id);
            if(state == 1){
                c.send(transacTable.get(id).getSenderId(), "Lock acquired");
                break;
            }
        }
    }

    public void release (String resources, int tID) {
        transacTable.get(tID).setResourcesLocked("");
        transacTable.get(tID).setTransacState(-1);//-1 means transaction done
        lockTable.get(resources).settId(-1);

    }

    public int acquire_lock(String resources, int tID){
        int result = 1;
        if(!lockTable.containsKey(resources)){
            LockTable lt = new LockTable();
            lt.settId(tID);
            lockTable.put(resources, lt);
            transacTable.get(tID).setTransacState(1);
        }else{
            LockTable lt = lockTable.get(resources);
            if(lt.gettId() != -1){
                if(waitingQ.containsKey(resources)){
                    waitingQ.get(resources).add(tID);
                }else{
                    ArrayList<Integer> a = new ArrayList<>();
                    a.add(tID);
                    waitingQ.put(resources, a);
                }
                transacTable.get(tID).setTransacState(0);
                lt.addtId_wait(tID);
                result = 0;
            }
            if(lt.gettId() == -1 && waitingQ.containsKey(resources)){
                waitingQ.get(resources).remove(tID);
                transacTable.get(tID).setTransacState(1);
                lt.settId(tID);
                lt.removetId_wait(tID);
                /*if(waitingQ.containsKey(resources)){
                    waitingQ.get(resources).remove(tID);
                    transacTable.get(tID).setTransacState(1);
                    lt.settId(tID);
                    lt.removetId_wait(tID);
                }*/
            }

        }
        return result;
    }


/*
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
*/
}
