package bookstore;

public interface Store {

    public Book get(int isbn);
    public Book search(String title);
    public Cart newCart();
}
