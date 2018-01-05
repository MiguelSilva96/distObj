package bookstore;

public interface Cart {
    public void add(Book b, int txid);
    public boolean buy(int txid);
}
