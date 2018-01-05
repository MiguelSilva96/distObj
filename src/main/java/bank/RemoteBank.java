package bank;

import bank.requests.*;
import mudar.Util;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import pt.haslab.ekit.Clique;

import java.util.concurrent.ExecutionException;

public class RemoteBank implements Bank {

    private final SingleThreadContext tc;
    private final Clique clique;
    private int id;

    public RemoteBank(SingleThreadContext tc, Clique clique) {
        this.tc = tc;
        this.clique = clique;

        id = 1; //TODO
    }

    @Override
    public Account search(String iban) {
        BankSearchRep r = null;
        try {
            r = (BankSearchRep) tc.execute(() ->
                    clique.sendAndReceive(1, new BankSearchReq(iban, id))
            ).join().get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
        return (Account) Util.makeRemote(tc, r.ref, 1, null, clique);
    }
}