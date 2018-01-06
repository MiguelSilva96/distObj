package bank.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BankTxnReq implements CatalystSerializable {
    public int accountid;
    public float price;
    public int txid;

    public BankTxnReq() {}

    public BankTxnReq(int accountid, float price, int txid) {
        this.accountid = accountid;
        this.price = price;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(accountid);
        bufferOutput.writeFloat(price);
        bufferOutput.writeInt(txid);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        accountid = bufferInput.readInt();
        price = bufferInput.readFloat();
        txid = bufferInput.readInt();
    }
}