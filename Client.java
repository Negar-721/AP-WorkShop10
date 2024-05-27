import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Scanner scanner;
    private static final String ANSI_PURPLE = "\u001B[35m";

    public Client(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));

            scanner = new Scanner(System.in);
        } catch (IOException ex) {
            System.out.println("Error connecting to the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void execute() {
        new ReadThread().start();
        new WriteThread().start();
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    String response = reader.readLine();
                    if (response == null) break;
                    System.out.println("\n" + response);
                } catch (IOException ex) {
                    System.out.println("Error reading from server: " + ex.getMessage());
                    break;
                }
            }
        }
    }

    private class WriteThread extends Thread {
        @Override
        public void run() {
            System.out.println(ANSI_PURPLE + "*!*!*!*!*!*!*!*! WELCOME HOME !*!*!*!*!*!*!*!* ");
            System.out.print(ANSI_PURPLE + "Enter your name: ");
            String userName = scanner.nextLine();
            writer.println(userName);

            String text;

            do {
                text = scanner.nextLine();
                writer.println(text);

                if (text.equals("#exit")) {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        System.out.println("Error writing to server: " + ex.getMessage());
                    }
                }
            } while (!text.equals("#exit"));
        }
    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 5757);
        client.execute();
    }

}
