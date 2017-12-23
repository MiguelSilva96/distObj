package bookstore;


import bookstore.requests.BookInfoRep;
import bookstore.requests.BookInfoReq;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.concurrent.ExecutionException;

public class RemoteBook implements Book {

	private final SingleThreadContext tc;
	private final Address address;
	private final Connection c;
	public int id;

	public RemoteBook(SingleThreadContext tc, Address address, int id) {
		this.tc = tc;
		this.id = id;
		this.address = address;
        Transport t = new NettyTransport();
        Connection connection = null;
        try {
            connection = tc.execute(() ->
                    t.client().connect(address)
            ).join().get();
        } catch(InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
        c = connection;
	}

	@Override
	public int getIsbn() {
		return 0;
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public String getAuthor() {
        BookInfoRep rep = null;
        try {
            rep = (BookInfoRep) tc.execute(() ->
                    c.sendAndReceive(new BookInfoReq(1, id))
                  ).join().get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
        if(rep == null) return null;
        return rep.author;
    }
}