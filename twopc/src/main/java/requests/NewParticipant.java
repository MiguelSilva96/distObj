package requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class NewParticipant implements CatalystSerializable {
    private int txid;
    private int participant;

    public NewParticipant() {}
    public NewParticipant(int txid) {
        this.txid = txid;
    }
    public NewParticipant(int txid, int participant) {
        this.txid = txid;
        this.participant = participant;
    }

    public int getTxid() {
        return txid;
    }

    public int getParticipant() {
        return participant;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(txid);
        bufferOutput.writeInt(participant);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        txid = bufferInput.readInt();
        participant = bufferInput.readInt();
    }

}
