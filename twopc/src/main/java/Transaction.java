
import io.atomix.catalyst.transport.Connection;
import pt.haslab.ekit.Clique;
import requests.Commit;
import requests.Prepare;
import requests.Rollback;
import requests.Vote;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Transaction {

    private Clique clique;
    private Integer votedCommit;
    private List<Integer> participants;
    private int phase;
    private CompletableFuture<Object> completedCommit;

    public Transaction(Clique clique) {
        votedCommit = 0;
        this.clique = clique;
        this.participants = new ArrayList<>();
        phase = 0;
    }

    public void addParticipant(int participant) {
        participants.add(participant);
    }

    public void firstPhase(CompletableFuture<Object> completedCommit) {
        this.completedCommit = completedCommit;
        send(new Prepare());
        phase = 1;
    }

    public void setParticipants(List<Integer> participants) {
        this.participants = participants;
    }

    public void abort(int mytxid) {
        Rollback rb = new Rollback(mytxid);
        send(rb);
        completedCommit.complete(rb);
    }

    public Object voted(Vote vote, int voter) {
        Object res = null;
        if (vote.getVote().equals("ABORT")) {
            res = new Rollback(vote.getTxid());
            send(res, voter);
            completedCommit.complete(res);
        } else {
            synchronized (votedCommit) {
                votedCommit++;
                if (votedCommit == participants.size()) {
                    res = new Commit(vote.getTxid());
                    send(res);
                    completedCommit.complete(res);
                    phase = 2;
                }
            }
        }
        return res;
    }

    public List<Integer> getParticipants() {
        return participants;
    }

    public int getPhase() {
        return phase;

    }

    private void send(Object message) {
        for(Integer i : participants) {
            clique.send(i, message);
        }
    }
    private void send(Object message, int except) {
        for(Integer i : participants) {
            if(i != except)
                clique.send(i, message);
        }
    }

}
