package bookstore;

public interface Store {

    public Book search(String title);
    public Cart newCart();
}
