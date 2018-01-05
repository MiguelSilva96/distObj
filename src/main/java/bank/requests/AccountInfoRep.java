package bank.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class AccountInfoRep implements CatalystSerializable {
    public String iban;

    public AccountInfoRep() {}

    public AccountInfoRep(String iban) {
        this.iban = iban;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(iban);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        iban = bufferInput.readString();
    }
}