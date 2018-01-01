
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

    public Transaction(Clique clique, Connection client) {
        votedCommit = 0;
        this.clique = clique;
        this.participants = new ArrayList<>();
        this.client = client;

    }

    public void addParticipant(int participant) {
        participants.add(participant);
    }

    public void firstPhase() {
        send(new Prepare());
    }

    public void voted(Vote vote, int voter) {
        if (vote.getVote().equals("ABORT")) {
            send(new Rollback(), voter);
            //class 4 this
            client.send("ABORTED:" + vote.getTxid());
        } else {
            synchronized (votedCommit) {
                votedCommit++;
                if (votedCommit == participants.size()) {
                    send(new Commit());
                    //class 4 this
                    client.send("COMMITED:" + vote.getTxid());
                }
            }
        }
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
