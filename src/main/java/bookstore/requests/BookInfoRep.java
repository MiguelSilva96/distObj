package bookstore.requests;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BookInfoRep implements CatalystSerializable {
    public int isbn;
    public String title;
    public String author;

    public BookInfoRep() {}
    public BookInfoRep(int isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(isbn);
        bufferOutput.writeString(title);
        bufferOutput.writeString(author);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        isbn = bufferInput.readInt();
        title = bufferInput.readString();
        author = bufferInput.readString();
    }
}
