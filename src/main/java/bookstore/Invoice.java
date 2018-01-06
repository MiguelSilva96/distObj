package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.util.ArrayList;
import java.util.List;

public class Invoice implements CatalystSerializable {
        //list of isbn from the books aquired
        List<Integer> booksAquired;
        int txid;

        public Invoice() {

        }

        public Invoice(List<Integer> booksAquired, int txid) {
            this.booksAquired = booksAquired;
            this.txid = txid;
        }

        @Override
        public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
            int size = booksAquired.size();
            bufferOutput.writeInt(size);
            for(Integer i : booksAquired) {
                bufferOutput.writeInt(i);
            }
            bufferOutput.writeInt(txid);
        }

        @Override
        public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
            int size = bufferInput.readInt();
            booksAquired = new ArrayList<>();
            for(int i = 0; i < size; i++) {
                booksAquired.add(bufferInput.readInt());
            }
            txid = bufferInput.readInt();
        }
    }

