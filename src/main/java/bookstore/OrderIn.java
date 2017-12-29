package bookstore;

import java.sql.Timestamp;

public class OrderIn {
    public Object obj;
    public long timeIn;

    public OrderIn( Object obj, long timeIn){
        this.obj = obj;
        this.timeIn = timeIn;
    }

    public void updateTimestamp(long timestamp){
        this.timeIn = timestamp;
    }
}
