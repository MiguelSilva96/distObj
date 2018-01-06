package bank;

import bank.requests.*;
import utilities.ObjRef;
import utilities.Util;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import pt.haslab.ekit.Clique;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RemoteBank implements Bank {

    private final SingleThreadContext tc;
    private final Clique clique;
    private int id;
    public CompletableFuture<RemoteAccount> search;

    public RemoteBank(SingleThreadContext tc, Clique clique) {
        this.tc = tc;
        this.clique = clique;
        id = 1; //TODO
    }

    @Override
    public Account search(String iban) {
        BankSearchRep r = null;
        CompletableFuture<BankSearchRep> rep = new CompletableFuture<>();
        clique.sendAndReceive(1, new BankSearchReq(iban, id))
                .thenAccept(s -> {
                    rep.complete((BankSearchRep)s);
                    RemoteAccount acc = (RemoteAccount) Util.makeRemote(tc,
                                        ((BankSearchRep) s).ref, clique);
                    search.complete(acc);
                });
        return null;
    }

    public RemoteBank clone() {
        return new RemoteBank(tc, clique);
    }
}
