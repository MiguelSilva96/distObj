package bank;


public interface Account {

    public String getIban();
    //public String getTitular();
    //public float getBalance();
    public boolean buy(float price);
}