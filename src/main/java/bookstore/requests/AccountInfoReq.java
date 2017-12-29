package bookstore.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class AccountInfoReq implements CatalystSerializable {
    public int bankid;
    public int accountid;

    public AccountInfoReq() {}
    public AccountInfoReq(int bankid, int accountid) {
        this.bankid = bankid;
        this.accountid = accountid;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(bankid);
        bufferOutput.writeInt(accountid);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        bankid = bufferInput.readInt();
        accountid = bufferInput.readInt();
    }
}
