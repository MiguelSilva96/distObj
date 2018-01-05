package bookstore;

public class BookImpl implements Book {

    public BookImpl(int isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    public int getIsbn() {

        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    private int isbn;
    private String title, author;
}
