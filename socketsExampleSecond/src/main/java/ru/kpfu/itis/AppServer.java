package ru.kpfu.itis;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class AppServer implements Closeable {
    public static void main(String[] args) {
        try (
                AppServer server = new AppServer(Protocol.PORT)
        ) {
            System.out.println("Starting server...");
            server.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected int port;
    protected ServerSocket server;
    protected boolean started;
    // Better to encapsulate to List<Connection>
    protected List<Socket> sockets;

    public AppServer(int port) {
        this.port = port;
        this.sockets = new ArrayList<>();
        this.started = false;
    }

    public void start() throws IOException {
        // Start server
        server = new ServerSocket(this.port);
        started = true;

        // Process connections
        while (true) {
            Socket socket = server.accept();
            // If some connection comes, it will wait for previous connection handling in a queue.
            sockets.add(socket);
            int connectionId = sockets.lastIndexOf(socket);
            // Every connection will wait creating and handling of message
            Message message = Message.readMessage(socket.getInputStream());
            System.out.println("New message:");
            System.out.println(Message.toString(message));
            // Could be more optimized and safer
            if (Message.TYPE1 == message.getType()) {
                IntBuffer buffer = ByteBuffer.wrap(message.getData()).asIntBuffer();
                int summ = buffer.get(0) + buffer.get(1);
                Message answer = Message.createMessage(Message.TYPE1, ByteBuffer.allocate(4).putInt(summ).array());
                this.sendMessage(connectionId, answer);
            } else {
                throw new RuntimeException(String.format(
                        "Unsupported message type: %d",
                        message.getType()
                ));
            }
        }
    }

    public void sendMessage(int connectionId, Message message) throws IOException {
        if (!started) {
            throw new IllegalStateException("Server hasn't been started yet.");
        }
        //ToDo: check if this socket is from our pull
        Socket socket = sockets.get(connectionId);
        socket.getOutputStream().write(Message.getBytes(message));
        socket.getOutputStream().flush();
    }

    public void sendBroadCastMessage(Message message) throws IOException {
        if (!started) {
            throw new IllegalStateException("Server hasn't been started yet.");
        }
        byte[] rawMessage = Message.getBytes(message);
        for (Socket socket : sockets) {
            socket.getOutputStream().write(rawMessage);
            socket.getOutputStream().flush();
        }
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedEncodingException();
    }
}
