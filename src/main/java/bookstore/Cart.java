package bookstore;

public interface Cart {
    public void add(Book b);
    public boolean buy(int txid, String iban);
}
