import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    
    int port;
    
    ArrayList<PrintWriter> clientOutputStreams;
    
    class ClientHandler implements Runnable {
        BufferedReader reader;
        Socket sock;
        String clientIP;
        
        public ClientHandler(Socket clientSocket) {
            try {
                sock = clientSocket;
                clientIP = sock.getInetAddress().getHostAddress();
                System.out.println("Client connected from: " + clientIP);
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        public void run() {
            String msg;
            try {
                while ((msg = reader.readLine()) != null) {
                    tell(msg);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + clientIP);
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (sock != null) sock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientOutputStreams) {
                    Iterator<PrintWriter> it = clientOutputStreams.iterator();
                    while (it.hasNext()) {
                        try {
                            PrintWriter writer = it.next();
                            if (writer.checkError()) {
                                it.remove();
                            }
                        } catch (Exception e) {
                            it.remove();
                        }
                    }
                }
                tell("System: A client from " + clientIP + " has left the chat.");
            }
        }
    }
    
    public void tell(String msg) {
        synchronized (clientOutputStreams) {
            Iterator<PrintWriter> it = clientOutputStreams.iterator();
            while (it.hasNext()) {
                try {
                    PrintWriter writer = it.next();
                    writer.println(msg);
                    writer.flush();
                    if (writer.checkError()) {
                        it.remove();
                    }
                } catch (Exception e) {
                    it.remove();
                }
            }
        }
    }
    
    public void go() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Input the port you want to use");
        port = sc.nextInt();
        try {
            clientOutputStreams = new ArrayList<PrintWriter>();
            ServerSocket serverSock = new ServerSocket(port);
            System.out.println("Successfully started server on port " + port);
            while (true) {
                Socket clientSocket = serverSock.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                synchronized (clientOutputStreams) {
                    clientOutputStreams.add(writer);
                }
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
                String clientIP = clientSocket.getInetAddress().getHostAddress();
                tell("System: A client from " + clientIP + " has joined the chat.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        Server s = new Server();
        s.go();
    }
}

