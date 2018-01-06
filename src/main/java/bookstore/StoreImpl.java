package bookstore;



import bank.Account;
import bank.Bank;
import bank.RemoteAccount;
import bank.RemoteBank;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;
import twopc.Participant;
import twopc.requests.*;
import twopl.Acquired;
import twopl.Release;
import twopl.TwoPl;
import utilities.ObjRef;

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
    private TwoPl twoPl;
    private RemoteBank bank;
    // helps to know when locks are aquired and the action is complete
    private Map<Integer, CompletableFuture<Object>> completablesForResp;

    public StoreImpl() {
        books.put(1, new Stock(4,
                new BookImpl(1, "one", "someone")));
        books.put(2, new Stock(3,
                new BookImpl(2, "other", "someother")));
        log = new Log("store1");
        history = new ArrayList<>();
        twoPl = new TwoPl();
        currentTransactions = new HashMap<>();
        completablesForResp = new HashMap<>();
    }

    public void setConnection(Clique clique, int coordId) {
        this.clique = clique;
        this.coordId = coordId;
        DistributedObjects distObj = new DistributedObjects();
        distObj.clique = clique;
        // no address because clique id is hardcoded, random address
        Address addr = new Address("localhost:12345");
        bank = (RemoteBank) distObj.importObj(new ObjRef(addr, 1, "bank"));
        handlers();
    }

    private void handlers() {
        log.handler(NewParticipant.class, (i, p) -> {
            Transaction t = new Transaction(p.getTxid());
            currentTransactions.put(p.getTxid(), t);
        });
        log.handler(LockLog.class, (i, l) -> {
            Transaction t = new Transaction(l.txid);
            t.locks.add(l.lock);
        });
        log.handler(Commit.class, (i, com) -> {
            Transaction t = currentTransactions.get(com.getTxid());
            for(Invoice inv : t.beforeCommit) {
                history.add(inv);
                for(Integer b : inv.booksAquired) {
                    books.get(b).nBooks--;
                }
            }
        });
        log.handler(Invoice.class, (i, inv) -> {
            Transaction t = currentTransactions.get(inv.txid);
            t.beforeCommit.add(inv);
        });
        log.handler(Vote.class, (i, vot) -> {
            Transaction t = currentTransactions.get(vot.getTxid());
            t.voted = true;
        });
        log.open().thenRun(() -> {
            for(Transaction t : currentTransactions.values()) {
                if(t.voted);
                else
                    clique.send(coordId, new Vote("ABORT", t.txid));
            }
        });
        clique.handler(Prepare.class, (j, m) -> {
            log.append(m);
            int txid = m.getTransactInfo().getTxid();
            Transaction t = currentTransactions.get(txid);
            clique.send(j, new Vote("COMMIT", txid));
            t.voted = true;
            log.append(new Vote("COMMIT", txid));
        });

        clique.handler(Commit.class, (j, m) -> {
            log.append(m);
            Transaction t = currentTransactions.get(m.getTxid());
            for(CompletableFuture<Release> cfr : t.locks)
                cfr.complete(new Release());
            System.out.print("Commit");
        });

        clique.handler(Rollback.class, (j, m) -> {
            Transaction t = currentTransactions.get(m.getTxid());
            for(Invoice inv : t.beforeCommit) {
                for (Integer in : inv.booksAquired) {
                    books.get(in).nBooks++;
                    history.remove(inv);
                }
            }
            for(CompletableFuture<Release> cfr : t.locks)
                cfr.complete(new Release());
            System.out.println("Rollback");
        });
        clique.open().thenRun(() -> System.out.println("open"));
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
        // to know buy result
        private Map<Integer, Boolean> doing;

        public CartImpl() {
            content = new ArrayList<>();
            doing = new HashMap<>();
        }

        public void add(Book b) {
            content.add(b);
        }

        private CompletableFuture<Boolean> sendToBank(String iban, int txid) {
            CompletableFuture<Boolean> result = new CompletableFuture<>();
            // this needs to be better
            // guarantee the inner cf isnt changed
            RemoteBank b = bank.clone();
            b.search = new CompletableFuture<>();
            b.search(iban);
            b.search.thenCompose((r) -> {
                if(r == null)
                    result.complete(false);
                r.buy = new CompletableFuture<>();
                r.buy(100, txid);
                return r.buy;
            }).thenAccept((r) -> {
                result.complete(r);
            });
            return result;
        }

        private CompletableFuture<?> treatBook(int txid, int c) {
            int i = -1;
            Stock st;
            Book anterior = new BookImpl(-1,
                            "fake", "fake");
            CompletableFuture<String> res = new CompletableFuture<>();
            for(Book b : content) {
                i++;
                if (i == c) {
                    st = books.get(b.getIsbn());
                    if (i == 0) log.append(new NewParticipant(txid));
                    if (i > 0)
                        if (books.get(anterior.getIsbn()).nBooks == 0) {
                            doing.put(txid, false);
                        }
                    CompletableFuture<Acquired> a = twoPl.lock(st);
                    a.thenAccept((aq) -> {
                        CompletableFuture<Release> rl = aq.getReleaseLock();
                        currentTransactions.get(txid).locks.add(rl);
                        log.append(new LockLog(rl, txid));
                    });
                    return a;
                }
                anterior = b;
            }
            return res;
        }

        private void auxBuy(CompletableFuture<?> res, int txid, int c, String iban) {
            //means all books have been checked
            if(c == content.size()) {
                boolean r = true;
                if(doing.get(txid) != null) {
                    r = false; doing.remove(txid);
                }
                final boolean rr = r;
                sendToBank(iban, txid).thenAccept((rb -> {
                    List<Integer> bs = new ArrayList<>();
                    if(rr && rb) {
                        for(Book b : content) {
                            bs.add(b.getIsbn());
                            // at this point all locks are acquired
                            books.get(b.getIsbn()).nBooks--;
                        }
                        Invoice inv = new Invoice(bs, txid);
                        currentTransactions.get(txid).beforeCommit.add(inv);
                        history.add(inv);
                        log.append(inv);
                    }
                    completablesForResp.get(txid).complete(rr && rb);
                }));
                return;
            }
            auxBuy(res.thenCompose((s) -> treatBook(txid, c)), txid, c + 1, iban);
        }

        public CompletableFuture<Object> getCf(int txid) {
            CompletableFuture<Object> res = completablesForResp.get(txid);
            return res;
        }

        public void removeCf(int txid) {
            completablesForResp.remove(txid);
        }

        public boolean buy(int txid, String iban) {
            Transaction t = currentTransactions.get(txid);
            CompletableFuture<Object> cf = new CompletableFuture<>();
            completablesForResp.put(txid, cf);
            if(t == null) {
                t = new Transaction(txid);
                currentTransactions.put(txid, t);
            }
            auxBuy(clique.sendAndReceive(coordId, new NewParticipant(txid)),
                    txid, 0, iban);
            //this return does not matter
            return false;
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
        List<Invoice> beforeCommit;
        boolean voted;
        int txid;
        List<CompletableFuture<Release>> locks;

        public Transaction(int txid) {
            beforeCommit = new ArrayList<>();
            locks = new ArrayList<>();
            voted = false;
            this.txid = txid;
        }

    }


}
