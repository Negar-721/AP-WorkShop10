import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static List<Handler> clients = new ArrayList<>();
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_YELLOW = "\u001B[33m";


    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress("0.0.0.0", 5757));
            System.out.println(ANSI_BLUE + "Server is listening on port 5757...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println(ANSI_YELLOW + "New user connected");

                Handler clientThread = new Handler(socket);
                clients.add(clientThread);
                clientThread.start();
            }
        }
    }

    static void broadcastMessage(String message, Handler thisUser) {
        for (Handler aUser : clients) {
            if (aUser != thisUser) {
                aUser.sendMessage(message);
            }
        }
    }

    static class Handler extends Thread {
        private Socket socket;
        private PrintWriter writer;
        private BufferedReader reader;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                InputStream input = socket.getInputStream();
                reader = new BufferedReader(new InputStreamReader(input));

                OutputStream output = socket.getOutputStream();
                writer = new PrintWriter(output, true);

                printUsers();

                String userName = reader.readLine();
                broadcastMessage(userName + " has joined.", this);

                String serverMessage;

                do {
                    serverMessage = reader.readLine();
                    if (serverMessage != null) {
                        System.out.println(ANSI_GREEN + userName + ": " + serverMessage + ANSI_RESET);
                        broadcastMessage("[" + userName + "]: " + serverMessage, this);
                    }
                } while (!serverMessage.equals("#exit"));

                broadcastMessage(userName + " has left.", this);
                socket.close();

                clients.remove(this);
                System.out.println(ANSI_RED + "Connection with user closed");
            } catch (IOException ex) {
                System.out.println("Error in Handler: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        void sendMessage(String message) {
            writer.println(message);
        }

        void printUsers() {
            if (clients.size() > 1) {
                writer.println(ANSI_GREEN + "Connected users: ");
                for (Handler aUser : clients) {
                    if (aUser != this) {
                        writer.println(aUser.socket.getInetAddress().getHostName());
                    }
                }
            } else {
                writer.println("No other users connected");
            }
        }
    }
}
