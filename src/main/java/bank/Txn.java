package bank;

import java.sql.Timestamp;

public class Txn {

    public int bankid;
    public String iban;

    public float price;
    public Timestamp date;

    public Txn(float price) {
        this.price = price;
        this.date = new Timestamp(System.currentTimeMillis());
    }

    public Txn(int bankid, String iban, float price) {
        this.bankid = bankid;
        this.iban = iban;
        this.price = price;
    }
}