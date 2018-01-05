package bookstore;

public interface Store {

    public Book search(String title, int txid);
    public Cart newCart(int txid);
}
