package ru.kpfu.itis;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class AppClient implements Closeable {

    public static void main(String[] args) {
        try (
                AppClient client = new AppClient(InetAddress.getByName("127.0.0.1"), Protocol.PORT)
        ) {
            client.connect();
            byte[] data = ByteBuffer.allocate(8).putInt(2).putInt(2).array();
            // Message request = Message.createMessage(-1, data);// Bad request
            Message request = Message.createMessage(Message.TYPE1, data);
            System.out.println("Request");
            System.out.println(Message.toString(request));
            System.out.println();
            Message response = client.sendMessage(request);
            System.out.println("Response");
            System.out.println(Message.toString(response));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected final InetAddress address;
    protected final int port;
    protected Socket socket;

    public AppClient(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void connect() throws IOException {
        socket = new Socket(address, port);
    }

    public Message sendMessage(Message message) throws IOException {
        socket.getOutputStream().write(Message.getBytes(message));
        socket.getOutputStream().flush();
        return Message.readMessage(socket.getInputStream());
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
    }
}
