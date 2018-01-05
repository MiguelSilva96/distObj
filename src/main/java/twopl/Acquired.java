package twopl;
import java.util.concurrent.CompletableFuture;


public class Acquired {
    private CompletableFuture<Release> releaseLock;

    public Acquired (CompletableFuture<Release> rel){
        this.releaseLock = rel;
    }

    public CompletableFuture<Release> getReleaseLock() {
        return releaseLock;
    }
}
