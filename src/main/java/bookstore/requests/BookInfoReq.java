package bookstore.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BookInfoReq implements CatalystSerializable {
    public int storeid;
    public int bookid;
    public int infoReq;
    //infoReq = 0 --> isbn  infoReq = 1 --> title   infoReq = 2 --> Author;

    public BookInfoReq() {}

    public BookInfoReq(int storeid, int bookid, int infoReq) {
        this.storeid = storeid;
        this.bookid = bookid;
        this.infoReq = infoReq;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(storeid);
        bufferOutput.writeInt(bookid);
        bufferOutput.writeInt(infoReq);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        storeid = bufferInput.readInt();
        bookid = bufferInput.readInt();
        infoReq = bufferInput.readInt();
    }
}
