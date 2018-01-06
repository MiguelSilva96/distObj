package bank.requests;

import bank.Txn;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import utilities.ObjRef;

import java.util.List;

public class ViewHistoryRep implements CatalystSerializable {

	public List<Txn> list;

	public ViewHistoryRep() {}
	public ViewHistoryRep(List<Txn> list) {
		this.list = list;
	}

	@Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
		int size = list.size();
		bufferOutput.writeInt(size);
		for(int i = 0; i < size; i++)
			serializer.writeObject(list.get(i), bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
		int size = bufferInput.readInt();
		for(int i = 0; i < size; i++)
			list.add(serializer.readObject(bufferInput));
    }

}
