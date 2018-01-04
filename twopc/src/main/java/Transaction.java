
import io.atomix.catalyst.transport.Connection;
import pt.haslab.ekit.Clique;
import requests.Commit;
import requests.Prepare;
import requests.Rollback;
import requests.Vote;

import java.util.ArrayList;
import java.util.List;

public class Transaction {

    private Clique clique;
    private Integer votedCommit;
    private Connection client;
    private List<Integer> participants;
    private int phase;

    public Transaction(Clique clique, Connection client) {
        votedCommit = 0;
        this.clique = clique;
        this.participants = new ArrayList<>();
        this.client = client;
        phase = 0;
    }

    public void addParticipant(int participant) {
        participants.add(participant);
    }

    public void firstPhase() {
        send(new Prepare());
        phase = 1;
    }

    public void setParticipants(List<Integer> participants) {
        this.participants = participants;
    }

    public void abort(int mytxid) {
        Rollback rb = new Rollback(mytxid);
        send(rb);
        client.send(rb);
    }

    public Object voted(Vote vote, int voter) {
        Object res = null;
        if (vote.getVote().equals("ABORT")) {
            res = new Rollback(vote.getTxid());
            send(res, voter);
            client.send(res);
        } else {
            synchronized (votedCommit) {
                votedCommit++;
                if (votedCommit == participants.size()) {
                    res = new Commit(vote.getTxid());
                    send(res);
                    client.send(res);
                    phase = 2;
                }
            }
        }
        return res;
    }

    public Connection getClient() {
        return client;
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
