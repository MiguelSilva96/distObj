package bank;

public interface Account {

    String getIban();
    boolean buy(float price, int txid);
}