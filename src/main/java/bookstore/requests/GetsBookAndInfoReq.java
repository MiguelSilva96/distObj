package bookstore.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class GetsBookAndInfoReq implements CatalystSerializable{
    public int storeId;
    public String title;
    public int infoReq; // info type needed

    public GetsBookAndInfoReq(){}

    public GetsBookAndInfoReq(int storeId, String title, int infoReq){
        this.storeId = storeId;
        this.title = title;
        this.infoReq = infoReq;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(storeId);
        bufferOutput.writeString(title);
        bufferOutput.writeInt(infoReq);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        storeId = bufferInput.readInt();
        title = bufferInput.readString();
        infoReq = bufferInput.readInt();
    }
}
