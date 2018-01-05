package bookstore.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class StoreSearchReq implements CatalystSerializable {
    public String title;
    public int id;
    public int txid;

    public StoreSearchReq() {}

    public StoreSearchReq(String title, int id, int txid) {
        this.title = title;
        this.id = id;
        this.txid = txid;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(title);
        bufferOutput.writeInt(id);
        bufferOutput.writeInt(txid);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        title = bufferInput.readString();
        id = bufferInput.readInt();
        txid = bufferInput.readInt();
    }
}
