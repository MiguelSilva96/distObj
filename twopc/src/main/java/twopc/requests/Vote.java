package twopc.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class Vote implements CatalystSerializable {
    private String vote;
    private int txid;

    public Vote() {

    }

    public Vote(String vote, int txid) {
        this.vote = vote;
        this.txid = txid;
    }

    public String getVote() {
        return vote;
    }

    public int getTxid() {
        return txid;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(vote);
        bufferOutput.writeInt(txid);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        vote = bufferInput.readString();
        txid = bufferInput.readInt();
    }

}
