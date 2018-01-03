package bank.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BankTxnReq implements CatalystSerializable {
    public int id;
    public float price;

    public BankTxnReq() {}
    public BankTxnReq(int id, float price) {
        this.id = id;
        this.price = price;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(id);
        bufferOutput.writeFloat(price);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        id = bufferInput.readInt();
        price = bufferInput.readFloat();
    }
}