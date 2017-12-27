public class Lock_request {
    public String lock;
    public int whoiam;

    public Lock_request(String s, int i){
        this.lock = s;
        this.whoiam = i;
    }

    public String getLock() {
        return lock;
    }

    public int getWhoiam() {
        return whoiam;
    }
}
