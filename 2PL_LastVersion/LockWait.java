import java.util.concurrent.CompletableFuture;

public class LockWait {
    private CompletableFuture<Acquired> compF;

    public LockWait(CompletableFuture<Acquired> compF){
        this.compF = compF;
    }

    public CompletableFuture<Acquired> getCompF() {
        return compF;
    }
}
