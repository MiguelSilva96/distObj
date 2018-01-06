package twopc.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TransactInfo implements CatalystSerializable {
    private int txid;
    private List<Integer> participants;
    private CompletableFuture<Object> completedCommit;

    public TransactInfo() {}
    public TransactInfo(int txid, List<Integer> participants) {
        this.txid = txid;
        this.participants = participants;
    }

    public void setCompletedCommit(CompletableFuture<Object> completedCommit) {
        this.completedCommit = completedCommit;
    }

    public CompletableFuture<Object> getCompletedCommit() {
        return completedCommit;
    }

    public int getTxid() {
        return txid;
    }


    public List<Integer> getParticipants() {
        return participants;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(txid);
        bufferOutput.writeInt(participants.size());
        for(Integer i : participants)
            bufferOutput.writeInt(i);
        serializer.writeObject(completedCommit, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        txid = bufferInput.readInt();
        int size = bufferInput.readInt();
        participants = new ArrayList<>();
        for(int i = 0; i < size; i++)
            participants.add(bufferInput.readInt());
        completedCommit = serializer.readObject(bufferInput);
    }
}
