import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;

import java.util.ArrayList;

public class _2PL {
    private static Clique c;
    public static void main(String args[]) throws Exception {

        Address[] addresses = {
                new Address("127.0.0.1", 12345),
                new Address("127.0.0.1", 12354),
                new Address("127.0.0.1", 54321)
        };
        int id = Integer.parseInt(args[0]);
        Transport t = new NettyTransport();
        int coordinator = 0;
        ArrayList<Integer> locked = new ArrayList<>();
        locked.forEach(i -> locked.add(0));
        final int[] nok = {0};
        ThreadContext tc = new SingleThreadContext("proto-%d", new Serializer());
        tc.execute(() -> {
            Log l = new Log("log" + id);
            //Ver como tratar isto de forma mais especifica em termos de OPs/Objs
            l.handler(Lock_request.class, (i, lr) -> {

                if(coordinator == id && locked.get(lr.whoiam) == 0) {
                    locked.set(lr.whoiam, 1);
                }else{
                    c.send(lr.whoiam,"Already locked once");
                }
            });

            l.handler(Unlock_request.class, (i, ur) -> {
                if(coordinator == id){
                    locked.set(ur.whoiam, 0);
                    c.send(ur.whoiam, "Unlocked");
                }
            });

            c.handler(String.class, (i,s) ->{
                if(s.equals("lock")){
                    Lock_request lr = new Lock_request(s, id);
                    l.append(lr);
                }
                if(s.equals("unlock")){
                    Unlock_request ur = new Unlock_request(s, id);
                    l.append(ur);
                }

            });


        });



        c.open().thenRun(() -> {
            c = new Clique(t, id, addresses);
            //coisas coisas
        });


        c.handler(Integer.class, (j, m) -> {

        }).onException(e -> {
            // exception handler
        });
    }

}
