package bank;

import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;
import twopc.requests.*;
import twopl.Acquired;
import twopl.Release;
import twopl.TwoPl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BankImpl implements Bank {

    Map<Integer, Account> accounts = new HashMap<>();
    private Map<Integer, Transaction> transactionsForCommit;
    Log log;
    Clique clique;
    TwoPl twoPl;
    int coordId;
    // helps to know when locks are aquired and the action is complete
    private Map<Integer, CompletableFuture<Object>> completablesForResp;

    public BankImpl() {
        log = new Log("banco");
        twoPl = new TwoPl();
        transactionsForCommit = new HashMap<>();
        completablesForResp = new HashMap<>();
        accounts.put(1, new AccountImpl("PT12345", 100));
        accounts.put(2, new AccountImpl("ES12345", 1));
    }

    public void setConnection(Clique clique, int coordId) {
        this.clique = clique;
        this.coordId = coordId;
        handlers();
    }

    public void handlers() {
        log.handler(NewParticipant.class, (i, p) -> {
            // to do
        });
        log.handler(Commit.class, (i, com) -> {
            // to do
        });
        log.handler(Vote.class, (i, vot) -> {
            // to do
        });
        log.open().thenRun(() -> {
            // to do
        });
        clique.handler(Prepare.class, (j, m) -> {
            log.append(m);
            int txid = m.getTransactInfo().getTxid();
            Transaction t = transactionsForCommit.get(txid);
            clique.send(j, new Vote("COMMIT", txid));
            t.voted = true;
            log.append(new Vote("COMMIT", txid));
        });

        clique.handler(Commit.class, (j, m) -> {
            log.append(m);
            Transaction t = transactionsForCommit.get(m.getTxid());
            for(CompletableFuture<Release> cfr : t.locks)
                cfr.complete(new Release());
            System.out.print("Commit");
        });

        clique.handler(Rollback.class, (j, m) -> {
            Transaction t = transactionsForCommit.get(m.getTxid());
            for(CompletableFuture<Release> cfr : t.locks)
                cfr.complete(new Release());
            System.out.println("Rollback");
        });
        clique.open().thenRun(() -> System.out.println("open"));
    }

    public Account search(String iban) {
        for (Account a : accounts.values())
            if (a.getIban().equals(iban))
                return a;
        return null;
    }

    public class AccountImpl implements Account {

        private String iban;
        private float balance;
        private List<Txn> transactions;

        public AccountImpl(String iban, float balance) {
            this.iban = iban;
            this.balance = balance;
            this.transactions = new ArrayList<>();
        }

        public String getIban() {
            return iban;
        }

        public boolean buy(float price, int txid) {
        /*
        if (balance >= price) {
            balance -= price;
            transactions.add(new Txn(price));
            return true;
        }*/
            CompletableFuture<Object> resp = new CompletableFuture<>();
            Transaction t = transactionsForCommit.get(txid);
            if(t == null) {
                t = new Transaction(txid);
                transactionsForCommit.put(txid, t);
            }
            final Transaction tr = t;
            completablesForResp.put(txid, resp);
            clique.sendAndReceive(coordId, new NewParticipant(txid))
                    .thenCompose((s) -> twoPl.lock(this))
                    .thenAccept((s) -> {
                        Acquired aq = (Acquired) s;
                        tr.locks.add(aq.getReleaseLock());
                        balance -= price;
                        transactions.add(new Txn(price));
                        resp.complete(true);
                    });
            // this return does not matter
            return true;
        }
        public CompletableFuture<Object> getCf(int txid) {
            CompletableFuture<Object> res = completablesForResp.get(txid);
            return res;
        }

        public void removeCf(int txid) {
            completablesForResp.remove(txid);
        }
    }

    class Transaction {
        boolean voted;
        int txid;
        List<CompletableFuture<Release>> locks;

        public Transaction(int txid) {
            locks = new ArrayList<>();
            voted = false;
            this.txid = txid;
        }

    }
}
