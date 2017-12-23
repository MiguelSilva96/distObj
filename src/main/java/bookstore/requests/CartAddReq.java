package bookstore.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class CartAddReq implements CatalystSerializable {
    public int cartid;
    public int bookid;

    public CartAddReq() {}
    public CartAddReq(int cartid, int bookid) {
        this.cartid = cartid;
        this.bookid = bookid;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(cartid);
        bufferOutput.writeInt(bookid);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        cartid = bufferInput.readInt();
        bookid = bufferInput.readInt();
    }
}
