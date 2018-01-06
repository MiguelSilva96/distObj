package twopc.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.util.ArrayList;
import java.util.List;

public class StartCommit implements CatalystSerializable {
    private TransactInfo transactInfo;
    private int txid;

    public StartCommit() {
    }

    public StartCommit(TransactInfo transactInfo, int txid) {
        this.transactInfo = transactInfo;
        this.txid = txid;
    }

    public StartCommit(int txid) {
        this.txid = txid;
        List<Integer> ls = new ArrayList<>();
        ls.add(1);
        transactInfo = new TransactInfo(txid, ls);
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(transactInfo, bufferOutput);
        bufferOutput.writeInt(txid);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        transactInfo = serializer.readObject(bufferInput);
        txid = bufferInput.readInt();
    }

    public TransactInfo getTransactInfo() {
        return transactInfo;
    }

    public void setTransactInfo(TransactInfo transactInfo) {
        this.transactInfo = transactInfo;
    }

    public int getTxid() {
        return txid;
    }
}

