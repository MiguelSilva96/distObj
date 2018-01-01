package requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class Prepare implements CatalystSerializable {
    int txid;

    public Prepare() {}
    public Prepare(int txid) {
        this.txid = txid;
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
