package bookstore;

public class Stock {
    public int nBooks;
    public Book book;

    public Stock(int nBooks, Book book){
        this.book = book;
        this.nBooks = nBooks;
    }
}
