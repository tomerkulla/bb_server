package bgu.spl181.net.api.bidi;

import bgu.spl181.net.api.MessageEncoderDecoder;
import bgu.spl181.net.srv.ActorThreadPool;
import bgu.spl181.net.srv.NonBlockingConnectionHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class BidiReactor<T> implements Server<T> {

    private final int port;
    private final Supplier<BidiMessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> readerFactory;
    private final ActorThreadPool pool;
    private Selector selector;
    private ConnectionsImpl<T> connections = new ConnectionsImpl<T>();
    private int activeClients;
    private Thread selectorThread;
    private final ConcurrentLinkedQueue<Runnable> selectorTasks = new ConcurrentLinkedQueue<>();
    private AtomicInteger numOfloggers = new AtomicInteger(0);

    public BidiReactor(
            int numThreads,
            int port,
            Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> readerFactory) {

        this.pool = new ActorThreadPool(numThreads);
        this.port = port;
        this.protocolFactory = protocolFactory;
        this.readerFactory = readerFactory;
    }

    public void serve() {
        selectorThread = Thread.currentThread();
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSock = ServerSocketChannel.open()) {

            this.selector = selector; //just to be able to close

            serverSock.bind(new InetSocketAddress(port));
            serverSock.configureBlocking(false);
            serverSock.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server started");

            while (!Thread.currentThread().isInterrupted()) {

                selector.select();
                runSelectionThreadTasks();

                for (SelectionKey key : selector.selectedKeys()) {

                    if (!key.isValid()) {
                        continue;
                    } else if (key.isAcceptable()) {
                        handleAccept(serverSock, selector);
                    } else {
                        handleReadWrite(key);
                    }
                }

                selector.selectedKeys().clear(); //clear the selected keys set so that we can know about new events

            }

        } catch (ClosedSelectorException ex) {
            //do nothing - server was requested to be closed
        } catch (IOException ex) {
            //this is an error
            ex.printStackTrace();
        }

        System.out.println("server closed!!!");
        pool.shutdown();
    }

    /*package*/ void updateInterestedOps(SocketChannel chan, int ops) {
        if(chan.isOpen()) {
            final SelectionKey key = chan.keyFor(selector);
            if (Thread.currentThread() == selectorThread) {
                key.interestOps(ops);
            } else {
                selectorTasks.add(() -> {
                    if(chan.isOpen())
                        key.interestOps(ops);
                });
                selector.wakeup();
            }
        }
    }


    private void handleAccept(ServerSocketChannel serverChan, Selector selector) throws IOException {
        int connectionId = numOfloggers.getAndIncrement();
        SocketChannel clientChan = serverChan.accept();
        clientChan.configureBlocking(false);
        BidiMessagingProtocol<T> newProtocol = protocolFactory.get();
        newProtocol.start(connectionId, connections);
        final BidiNonBlockingConnectionHandler handler = new BidiNonBlockingConnectionHandler(
                readerFactory.get(),
                newProtocol,
                clientChan,
                this);
        clientChan.register(selector, SelectionKey.OP_READ, handler);
        connections.addConnection(connectionId, handler);
    }

    private void handleReadWrite(SelectionKey key) {
        BidiNonBlockingConnectionHandler handler = (BidiNonBlockingConnectionHandler) key.attachment();

        if (key.isReadable()) {
            Runnable task = handler.continueRead();
            if (task != null) {
                pool.submit(handler, task);
            }
        }

        if (key.isValid() && key.isWritable()) {
            handler.continueWrite();
        }
    }

    private void runSelectionThreadTasks() {
        while (!selectorTasks.isEmpty()) {
            selectorTasks.remove().run();
        }
    }

    public void close() throws IOException {
        selector.close();
    }

}

