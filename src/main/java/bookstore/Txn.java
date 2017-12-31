package bookstore;

import java.sql.Timestamp;

public class Txn {

    public String iban;

    public float price;
    public Timestamp date;

    public Txn(float price) {
        this.price = price;
        this.date = new Timestamp(System.currentTimeMillis());
    }

    public Txn(String iban, float price) {
        this.iban = iban;
        this.price = price;
    }
}