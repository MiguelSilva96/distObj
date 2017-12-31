package bookstore.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class TokenReq implements CatalystSerializable {

    public TokenReq() {}

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
    }
}
