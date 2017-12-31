package bookstore.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class AddTxnReq implements CatalystSerializable {
    public String iban;
    public float price;

    public AddTxnReq() {}
    public AddTxnReq(String iban, float price) {
        this.iban = iban;
        this.price = price;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(iban);
        bufferOutput.writeFloat(price);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        iban = bufferInput.readString();
        price = bufferInput.readFloat();
    }
}
