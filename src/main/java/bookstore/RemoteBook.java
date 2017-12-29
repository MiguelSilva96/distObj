package bookstore;


import bookstore.requests.BookInfoRep;
import bookstore.requests.BookInfoReq;
import bookstore.requests.GetsBookAndInfoReq;
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
	private int storeId;
	private long timeout;
	private long timeCreate;
    private String bookTitle;

	public RemoteBook(SingleThreadContext tc, Address addr, int id, long tC, int idS, String title) {
		this.tc = tc;
		this.id = id;
		this.address = addr;
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
        this.timeout = 100000;
        this.timeCreate = tC;
        this.storeId = idS;
        this.bookTitle = title;
	}

	@Override
	public int getIsbn() {
        BookInfoRep rep = null;
        if(System.currentTimeMillis() - timeCreate >= timeout){
            try{
                rep = (BookInfoRep) tc.execute(() ->
                    c.sendAndReceive(new GetsBookAndInfoReq(storeId, bookTitle, 0))
                ).join().get();
            }catch (InterruptedException|ExecutionException e){
                e.printStackTrace();
            }
        }else {
            try {
                rep = (BookInfoRep) tc.execute(() ->
                        c.sendAndReceive(new BookInfoReq(1, id, 0))
                ).join().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

		return rep.isbn;
	}

	@Override
	public String getTitle() {
        BookInfoRep rep = null;
        if(System.currentTimeMillis() - timeCreate >= timeout){
            try{
                rep = (BookInfoRep) tc.execute(() ->
                        c.sendAndReceive(new GetsBookAndInfoReq(storeId, bookTitle, 1))
                ).join().get();
            }catch (InterruptedException|ExecutionException e){
                e.printStackTrace();
            }
        }else {
            try {
                rep = (BookInfoRep) tc.execute(() ->
                        c.sendAndReceive(new BookInfoReq(1, id, 1))
                ).join().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return rep.titAuth;
	}

	@Override
	public String getAuthor() {
        BookInfoRep rep = null;
        if(System.currentTimeMillis() - timeCreate >= timeout){
            try{
                rep = (BookInfoRep) tc.execute(() ->
                        c.sendAndReceive(new GetsBookAndInfoReq(storeId, bookTitle, 2))
                ).join().get();
            }catch (InterruptedException|ExecutionException e){
                e.printStackTrace();
            }
        }else {
            try {
                rep = (BookInfoRep) tc.execute(() ->
                        c.sendAndReceive(new BookInfoReq(1, id, 2))
                ).join().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return rep.titAuth;
    }
}