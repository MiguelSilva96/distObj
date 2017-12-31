package bookstore.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BankTokenRep implements CatalystSerializable {

    public int bankid;
    public String iban;
    public float price;

    public BankTokenRep() {}
    public BankTokenRep(int bankid, String iban, float price) {
        this.bankid = bankid;
        this.iban = iban;
        this.price = price;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(bankid);
        bufferOutput.writeString(iban);
        bufferOutput.writeFloat(price);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        bankid = bufferInput.readInt();
        iban = bufferInput.readString();
        price = bufferInput.readFloat();
    }
}
