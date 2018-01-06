package bookstore.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BookInfoRep implements CatalystSerializable {
    public int isbn;
    public String titAuth;

    public BookInfoRep() {}

    public BookInfoRep(int isbn, String titAuth) {
        this.isbn = isbn;
        this.titAuth = titAuth;
    }

    public BookInfoRep(int isbn){
        this.isbn = isbn;
    }

    public BookInfoRep(String titAuth){
        this.titAuth = titAuth;

    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(isbn);
        bufferOutput.writeString(titAuth);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        isbn = bufferInput.readInt();
        titAuth = bufferInput.readString();

    }
}

