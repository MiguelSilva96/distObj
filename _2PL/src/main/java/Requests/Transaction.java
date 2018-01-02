package Requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class Transaction implements CatalystSerializable{
    public String resources;
    public int RW; // 0--> acquire_read |  1 --> acquire_write | -1 --> release

    public Transaction(){}

    public Transaction(String resources, int rw){
        this.resources = resources;
        this.RW = rw;
    }
    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(resources);
        bufferOutput.writeInt(RW);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        resources = bufferInput.readString();
        RW = bufferInput.readInt();
    }
}
