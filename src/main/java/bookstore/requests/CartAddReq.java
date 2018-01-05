package bookstore.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class CartAddReq implements CatalystSerializable {
    public int cartid;
    public int bookid;
    public int txid;

    public CartAddReq() {}
    public CartAddReq(int bookid, int cartid, int txid) {
        this.cartid = cartid;
        this.bookid = bookid;
        this.txid = txid;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(cartid);
        bufferOutput.writeInt(bookid);
        bufferOutput.writeInt(txid);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        cartid = bufferInput.readInt();
        bookid = bufferInput.readInt();
        txid = bufferInput.readInt();
    }
}
