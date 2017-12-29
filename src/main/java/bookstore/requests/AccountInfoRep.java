package bookstore.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class AccountInfoRep implements CatalystSerializable {
    public String iban;
    public String titular;
    public float price;

    public AccountInfoRep() {}
    public AccountInfoRep(String iban, String titular, float price) {
        this.iban = iban;
        this.titular = titular;
        this.price = price;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(iban);
        bufferOutput.writeString(titular);
        bufferOutput.writeFloat(price);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        iban = bufferInput.readString();
        titular = bufferInput.readString();
        price = bufferInput.readFloat();
    }
}
