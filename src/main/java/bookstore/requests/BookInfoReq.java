package bookstore.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BookInfoReq implements CatalystSerializable {
    public int storeid;
    public int bookid;

    public BookInfoReq() {}
    public BookInfoReq(int storeid, int bookid) {
        this.storeid = storeid;
        this.bookid = bookid;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(storeid);
        bufferOutput.writeInt(bookid);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        storeid = bufferInput.readInt();
        bookid = bufferInput.readInt();
    }
}
