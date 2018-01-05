package twopc.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BeginRep implements CatalystSerializable {

    private int txid;

    public BeginRep() {}

    public BeginRep(int txid) {
        this.txid = txid;
    }

    public int getTxid() {
        return txid;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(txid);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        txid = bufferInput.readInt();
    }
}
