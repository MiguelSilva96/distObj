package bank.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BankSearchReq implements CatalystSerializable {
    public String iban;
    public int id;

    public BankSearchReq() {}

    public BankSearchReq(String iban, int id) {
        this.iban = iban;
        this.id = id;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(iban);
        bufferOutput.writeInt(id);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        iban = bufferInput.readString();
        id = bufferInput.readInt();
    }
}