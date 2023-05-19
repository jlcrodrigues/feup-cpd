package server;

import server.store.SocketWrapper;
import server.store.Store;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

public class Server {

    public static void main(String[] args) {
        Store store = Store.getStore();

        try {
            registerServerSocketChannel();

            while (true) {
                Selector selector = store.getSelector();
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    if (key.isAcceptable()) {
                        ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientSocketChannel = socketChannel.accept();
                        SocketWrapper socket = new SocketWrapper(clientSocketChannel.socket());

                        store.log(Level.INFO, "New connection: " + socket);
                        ConnectionHandler handler = new ConnectionHandler(socket);
                        store.execute(handler);
                    } else if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        key.cancel();
                        socketChannel.configureBlocking(true);
                        ConnectionHandler handler = new ConnectionHandler(new SocketWrapper(socketChannel.socket()));
                        store.execute(handler);
                    }
                    iter.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registerServerSocketChannel() throws IOException {
        Store store = Store.getStore();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(store.getProperty("port")));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(store.getSelector(), SelectionKey.OP_ACCEPT);
        store.log(Level.INFO, "Server is listening on port " + store.getProperty("port"));
    }
}


