package bookstore;



import io.atomix.catalyst.concurrent.Futures;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;
import twopc.Participant;
import twopc.requests.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StoreImpl implements Store {
    private Map<Integer,Stock> books = new HashMap<>();
    private List<Invoice> history;
    private Map<Integer, Transaction> currentTransactions;
    private Log log;
    private Clique clique;
    private int coordId;
    // only to guarantee we only reply to client when coord responds
    private Map<Integer, CompletableFuture<Object>> completablesForResp;

    public StoreImpl() {
        books.put(1, new Stock(4,
                new BookImpl(1, "one", "someone")));
        books.put(2, new Stock(3,
                new BookImpl(2, "other", "someother")));
        log = new Log("store1");
        history = new ArrayList<>();
        currentTransactions = new HashMap<>();
        completablesForResp = new HashMap<>();
    }

    public void setConnection(Clique clique, int coordId) {
        this.clique = clique;
        this.coordId = coordId;
        handlers();
    }

    private void handlers() {
        log.handler(Prepare.class, (i, p) -> {

        });
        log.handler(Commit.class, (i, com) -> {
            // nothing to do here
        });
        log.handler(String.class, (i, vot) -> {
            //means that has voted
            //wait for coord
        });
        log.open().thenRun(() -> {
            //do we need then run??
        });
        clique.handler(Prepare.class, (j, m) -> {
            log.append(m);
            int txid = m.getTransactInfo().getTxid();
            System.out.println("received prepare");
            clique.send(j, new Vote("COMMIT", txid));
            currentTransactions.get(txid).voted = true;
            log.append("Voted");
        });

        clique.handler(Commit.class, (j, m) -> {
            log.append(m);
            Transaction t = currentTransactions.get(m.getTxid());
            // free locks
            System.out.print("Commit");
        });

        clique.handler(Rollback.class, (j, m) -> {
            Transaction t = currentTransactions.get(m.getTxid());
            for(Object o : t.beforeCommit) {
                if(o instanceof Stock) {
                    Stock s = (Stock) o;
                    books.remove(s.book.getIsbn());
                    books.put(s.book.getIsbn(), s);
                } else if(o instanceof Invoice) {
                    history.remove(o);
                }
            }
            // free locks
            System.out.println("Rollback");
        });
        clique.open().thenRun(() -> System.out.println("open"));
    }

    public Book get(int isbn) {
        return books.get(isbn).book;
    }

    public CompletableFuture<Object> getCf(int txid) {
        CompletableFuture<Object> res = completablesForResp.get(txid);
        return res;
    }

    public void removeCf(int txid) {
        completablesForResp.remove(txid);
    }

    public Book search(String title, int txid) {
        Transaction t = currentTransactions.get(txid);
        CompletableFuture<Object> cf = new CompletableFuture<>();
        if(t == null) {
            t = new Transaction(txid);
            currentTransactions.put(txid, t);
            completablesForResp.put(txid, cf);
            clique.sendAndReceive(coordId, new NewParticipant(txid))
                    .thenAccept(s -> {cf.complete(s);});
        }
        for(Stock b: books.values())
            if (b.book.getTitle().equals(title))
                return b.book;
        return null;
    }

    public Cart newCart(int txid) {
        Transaction t = currentTransactions.get(txid);
        CompletableFuture<Object> cf = new CompletableFuture<>();
        if(t == null) {
            t = new Transaction(txid);
            currentTransactions.put(txid, t);
            completablesForResp.put(txid, cf);
            clique.sendAndReceive(coordId, new NewParticipant(txid))
                    .thenAccept(s -> {cf.complete(s);});
        }
        return new CartImpl();
    }

    public class CartImpl implements Cart {
        private List<Book> content;

        public void add(Book b, int txid) {
            content.add(b);
        }

        public boolean buy(int txid) {
            Transaction t = currentTransactions.get(txid);
            CompletableFuture<Object> cf = new CompletableFuture<>();
            boolean newTransaction = false;
            if(t == null) {
                t = new Transaction(txid);
                currentTransactions.put(txid, t);
                newTransaction = true;
            }
            int nOk = 0;
            Map<Integer, Stock> temp = books;
            for (Book b : content) {
                for (Stock s : books.values()) {
                    if (s.book.equals(b) && s.nBooks > 0) {
                        nOk++;
                        t.beforeCommit.add(s.clone());
                        s.nBooks--;
                        break;
                    }
                }
            }
            if(newTransaction == true) {
                completablesForResp.put(txid, cf);
                clique.sendAndReceive(coordId, new NewParticipant(txid))
                        .thenAccept(s -> {cf.complete(s);});
            }
            if (nOk == content.size())
                return true;
            else {
                books = temp;
                return true;
            }
        }

    }

    class Stock {
        public int nBooks;
        public Book book;

        public Stock(int nBooks, Book book){
            this.book = book;
            this.nBooks = nBooks;
        }

        public Stock clone() {
            return new Stock(this.nBooks, this.book);
        }
    }

    class Transaction {
        List<Object> beforeCommit;
        boolean voted;
        int txid;

        public Transaction(int txid) {
            beforeCommit = new ArrayList<>();
            this.txid = txid;
        }

    }

    class Invoice {
        List<Book> booksAquired;
    }
}
