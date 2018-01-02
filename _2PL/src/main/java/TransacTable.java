import java.util.List;

public class TransacTable {
    private String resourcesLocked;
    private int transacState; //-1--> done |  0 --> waiting  |  1 --> active | -2 --> UNKNOW
    private long transacTime;
    private int transacId;

    public TransacTable(String resourcesLocked, int transacId){
        this.transacState = 1;
        this.resourcesLocked = resourcesLocked;
        this.transacId = transacId;
        this.transacTime = System.currentTimeMillis();
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
}
