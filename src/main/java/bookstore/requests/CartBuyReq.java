package bookstore.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class CartBuyReq implements CatalystSerializable{
    public int cartid;
    public int txid;
    public String iban;

    public CartBuyReq() { }

    public CartBuyReq(int cartid, int txid, String iban) {
        this.cartid = cartid;
        this.txid = txid;
        this.iban = iban;
    }


    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(cartid);
        bufferOutput.writeInt(txid);
        bufferOutput.writeString(iban);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        cartid = bufferInput.readInt();
        txid = bufferInput.readInt();
        iban = bufferInput.readString();
    }
}
