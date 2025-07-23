import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    
    int port;
    
    ArrayList clientOutputStreams;
    
    class ClientHandler implements Runnable {
        BufferedReader reader;
        Socket sock;
        
        public ClientHandler(Socket clientSocket) {
            try {
                sock = clientSocket;
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
                // 客户端断开连接时的处理
                System.out.println("A client disconnected");
            } finally {
                // 清理资源
                try {
                    if (reader != null) reader.close();
                    if (sock != null) sock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 从输出流列表中移除该客户端的输出流
                synchronized (clientOutputStreams) {
                    Iterator it = clientOutputStreams.iterator();
                    while (it.hasNext()) {
                        try {
                            PrintWriter writer = (PrintWriter) it.next();
                            if (writer.checkError()) {  // 检查流是否已关闭
                                it.remove();
                            }
                        } catch (Exception e) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }
    
    public void tell(String msg) {
        synchronized (clientOutputStreams) {
            Iterator it = clientOutputStreams.iterator();
            while (it.hasNext()) {
                try {
                    PrintWriter writer = (PrintWriter) it.next();
                    writer.println(msg);
                    writer.flush();
                    if (writer.checkError()) {  // 检查流是否已关闭
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
            clientOutputStreams = new ArrayList();
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
                System.out.println("Got a connection");
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
