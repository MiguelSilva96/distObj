package requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class Prepare implements CatalystSerializable {
    TransactInfo transactInfo;

    public Prepare() {}
    public Prepare(TransactInfo transactInfo) {
        this.transactInfo = transactInfo;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(transactInfo, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        transactInfo = serializer.readObject(bufferInput);
    }

    public TransactInfo getTransactInfo() {
        return transactInfo;
    }
}
