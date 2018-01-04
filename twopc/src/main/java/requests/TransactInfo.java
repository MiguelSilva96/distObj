package requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Connection;
import pt.haslab.ekit.Clique;

import java.util.ArrayList;
import java.util.List;

public class TransactInfo implements CatalystSerializable {
    private int txid;
    private Connection client;
    private List<Integer> participants;

    public TransactInfo() {}
    public TransactInfo(int txid, Connection client, List<Integer> participants) {
        this.txid = txid;
        this.client = client;
        this.participants = participants;
    }

    public int getTxid() {
        return txid;
    }

    public Connection getClient() {
        return client;
    }

    public List<Integer> getParticipants() {
        return participants;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(txid);
        serializer.writeObject(client, bufferOutput);
        bufferOutput.writeInt(participants.size());
        for(Integer i : participants)
            bufferOutput.writeInt(i);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        txid = bufferInput.readInt();
        client = serializer.readObject(bufferInput);
        int size = bufferInput.readInt();
        participants = new ArrayList<>();
        for(int i = 0; i < size; i++)
            participants.add(bufferInput.readInt());
    }
}
