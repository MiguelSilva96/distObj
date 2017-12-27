public class Unlock_request {
    public String unlock;
    public int whoiam;

    public Unlock_request (String s, int i){
        this.unlock = s;
        this.whoiam = i;
    }

    public int getWhoiam() {
        return whoiam;
    }

    public String getUnlock() {
        return unlock;
    }
}
