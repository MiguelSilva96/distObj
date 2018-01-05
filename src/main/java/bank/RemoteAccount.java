package bank;

import bank.requests.*;

import io.atomix.catalyst.concurrent.SingleThreadContext;

import pt.haslab.ekit.Clique;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RemoteAccount implements Account {

    private final SingleThreadContext tc;
    private final Clique clique;
    public int id;

    public RemoteAccount(SingleThreadContext tc, Clique clique, int id) {
        this.tc = tc;
        this.clique = clique;
        this.id = id;
    }

    @Override
    public String getIban() {
        return "";
    }

    @Override
    public boolean buy(float price) {
        BankTxnRep r = null;
        CompletableFuture<BankTxnRep> rep = new CompletableFuture<>();
        clique.sendAndReceive(1, new BankTxnReq(id, price))
                .thenAccept(s -> rep.complete((BankTxnRep) s));
        try {
            r = rep.get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
        return r.result;
    }
}