package bookstore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreImpl implements Store {
    //MAP <#isbn, < Books of, Book>>
    public Map<Integer,Stock> books = new HashMap<>();

    public StoreImpl() {
        books.put(1, new Stock(4, new BookImpl(1, "one", "someone")));
        books.put(2, new Stock(3, new BookImpl(2, "other", "someother")));
    }

    public Book get(int isbn) {
        return books.get(isbn).book;
    }

    public Book search(String title) {
        for(Stock b: books.values())
            if (b.book.getTitle().equals(title))
                return b.book;
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
            int nOk = 0;
            for(Book b : content){
                for(Stock s: books.values()){
                    if(s.book.equals(b) && s.nBooks > 0){
                        nOk++;
                        break;
                    }
                }
            }
            if(nOk == content.size())
                return true;
            else return false;
        }
    }
}
