import java.util.ArrayList;
import java.util.List;

public class LockTable {

    private List<Integer> transacID_r;
    private List<Integer> transacID_w;
    private List<Integer> transacID_r_wait;
    private List<Integer> transacID_w_wait;

    public LockTable(){
        this.transacID_r = new ArrayList<>();
        this.transacID_w = new ArrayList<>();
        this.transacID_r_wait = new ArrayList<>();
        this.transacID_w_wait = new ArrayList<>();
    }

    public List<Integer> getTransacID_r() {
        return transacID_r;
    }

    public void setTransacID_r(int tID_r) {
        this.transacID_r.add(tID_r);
    }

    public List<Integer> getTransacID_w() {
        return transacID_w;
    }

    public void setTransacID_w(int tID_w) {
        this.transacID_w.add(tID_w);
    }

    public List<Integer> getTransacID_r_wait() {
        return transacID_r_wait;
    }

    public void setTransacID_r_wait(int tID_r_wait) {
        this.transacID_r_wait.add(tID_r_wait);
    }

    public List<Integer> getTransacID_w_wait() {
        return transacID_w_wait;
    }

    public void setTransacID_w_wait(int tID_w_wait) {
        this.transacID_w_wait.add(tID_w_wait);
    }

    public char remove(int id){
        char result = '0';
        if(this.transacID_w.contains(id)){
            this.transacID_w.remove(id);
            result = 'w';
        }else if(this.transacID_r.contains(id)){
            this.transacID_r.remove(id);
            result = 'r';
        }
        return result;
    }

}
