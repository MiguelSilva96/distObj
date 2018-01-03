package bank.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BankTxnRep implements CatalystSerializable {
    public boolean result;

    public BankTxnRep() { }
    public BankTxnRep(boolean result) {
        this.result = result;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeBoolean(result);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        result = bufferInput.readBoolean();
    }
}