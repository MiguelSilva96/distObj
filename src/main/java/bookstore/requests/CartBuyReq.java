package bookstore.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class CartBuyReq implements CatalystSerializable{
    public int cartid;
    public int storeid;

    public CartBuyReq() { }

    public CartBuyReq(int cartid, int storeid) {
        this.cartid = cartid;
        this.storeid = storeid;
    }


    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(cartid);
        bufferOutput.writeInt(storeid);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        cartid = bufferInput.readInt();
        storeid = bufferInput.readInt();
    }
}
