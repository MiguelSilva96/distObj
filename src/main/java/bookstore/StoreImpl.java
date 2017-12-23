package bookstore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreImpl implements Store {
    public Map<Integer, Book> books = new HashMap<>();

    public StoreImpl() {
        books.put(1, new BookImpl(1, "one", "someone"));
        books.put(2, new BookImpl(2, "other", "someother"));
    }

    public Book get(int isbn) {
        return books.get(isbn);
    }

    public Book search(String title) {
        for(Book b: books.values())
            if (b.getTitle().equals(title))
                return b;
        return null;
    }

    public Cart newCart() {
        return new CartImpl();
    }

    public class CartImpl implements Cart {
        private List<Book> content;

        public void add(Book b) {
            content.add(b);
        }

        public boolean buy() {
            return true;
        }
    }
}
