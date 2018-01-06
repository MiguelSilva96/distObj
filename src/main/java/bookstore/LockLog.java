package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import twopl.Release;

import java.util.concurrent.CompletableFuture;

public class LockLog implements CatalystSerializable {
        CompletableFuture<Release> lock;
        int txid;

        public LockLog() {}
        public LockLog(CompletableFuture<Release> lock, int txid) {
            //this.lock = lock;
            this.txid = txid;
        }

        @Override
        public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
            //serializer.writeObject(lock, bufferOutput);
            bufferOutput.writeInt(txid);
        }

        @Override
        public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
            //lock = serializer.readObject(bufferInput);
            txid = bufferInput.readInt();
        }
    }

