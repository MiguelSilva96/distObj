import java.util.List;

public class TransacTable {
    private String resourcesLocked;
    private int transacState; //-1--> done |  0 --> waiting  |  1 --> active | -2 --> UNKNOW
    private long transacTime; //DEPRECATED
    private int transacId; // request id
    private int senderId; //request sender id

    public TransacTable(String resourcesLocked, int transacId, int senderId){
        this.transacState = 1;
        this.resourcesLocked = resourcesLocked;
        this.transacId = transacId;
        this.transacTime = System.currentTimeMillis();
        this.senderId = senderId;
    }

    public String getResourcesLocked() {
        return resourcesLocked;
    }

    public void setResourcesLocked(String resourcesLocked) {
        this.resourcesLocked = resourcesLocked;
    }

    public int getTransacState() {
        return transacState;
    }

    public void setTransacState(int transacState) {
        this.transacState = transacState;
    }

    public long getTransacTime() {
        return transacTime;
    }

    public void setTransacTime(long transacTime) {
        this.transacTime = transacTime;
    }

    public int getTransacId() {
        return transacId;
    }

    public void setTransacId(int transacId) {
        this.transacId = transacId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }
}
