package twopl;

import io.atomix.catalyst.concurrent.Futures;

import java.util.HashMap;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class TwoPl {

    private Map<Object, Queue<LockWait>> lockMap;

    public TwoPl(){
        lockMap = new HashMap<>();
    }

    public CompletableFuture<Acquired> lock(Object o){
        CompletableFuture<Acquired> compF = new CompletableFuture<>();
        if(!lockMap.containsKey(o)) {
            PriorityQueue<LockWait> pq = new PriorityQueue<>();
            lockMap.put(o, pq);
            CompletableFuture<Release> compRel = new CompletableFuture<>();
            Acquired aq = new Acquired(compRel);
            compRel.thenAccept(s -> release(o));
            return Futures.completedFuture(aq);
        }else {
            LockWait lw = new LockWait(compF);
            lockMap.get(o).add(lw);
            return compF;
        }

    }

    public CompletableFuture release (Object o){
        LockWait lw = lockMap.get(o).remove();
        if(lockMap.get(o).isEmpty())
            lockMap.remove(o);
        CompletableFuture<Release> compRel = new CompletableFuture<>();
        Acquired aq = new Acquired(compRel);
        compRel.thenAccept(s -> release(o));
        return Futures.completedFuture(aq);
    }

}
