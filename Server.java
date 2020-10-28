import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private static final int portNumber = 4446;
    private int serverPort;
    private List<ClientThread> clients; 
    
    public static void main(String[] args){
        Server server = new Server(portNumber);
        server.startServer();
    }

    public Server(int portNumber){
        this.serverPort = portNumber;
    }

    private void startServer(){
        clients = new ArrayList<ClientThread>();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(serverPort);
            acceptClients(serverSocket);
        } catch (IOException e){
            System.err.println("Could not listen on port: "+serverPort);
            System.exit(1);
        }
    }

    private void acceptClients(ServerSocket serverSocket){

        System.out.println("server starts port = " + serverSocket.getLocalSocketAddress());
        while(true){
            try{
                Socket socket = serverSocket.accept();
                System.out.println("accepts : " + socket.getRemoteSocketAddress());
                ClientThread client = new ClientThread(socket);
                Thread thread = new Thread(client);
                thread.start();
                clients.add(client);
            } catch (IOException ex){
                System.out.println("Accept failed on : "+serverPort);
            }
        }
    }
}

class ClientThread implements Runnable {
    private Socket socket;
    private PrintWriter clientOut;
    Scanner in;

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    private PrintWriter getWriter() {
        return clientOut;
    }

    @Override
    public void run() {
        try {
            getStream();
            getAndSendMessage();
        } catch (IOException e) {
            System.out.println("Error: Closing the connection to user on"+socket.getRemoteSocketAddress());
        }
        finally
        {
        	closeConnection();
        }
   }
    
   private void getAndSendMessage()
   {
       while (!socket.isClosed()) {
           if (in.hasNextLine()) {
                String input = in.nextLine();
                PrintWriter thatClientOut = this.getWriter();
                if(thatClientOut != null){
                input=input.toUpperCase();
                
                if(ifLeft(input))
                {
               	 System.out.println("Left User on: "+socket.getRemoteSocketAddress());
               	 sendToUser("CLOSE");
               	 closeConnection();
                }
                sendToUser(input);
                }
           }
       }
   }
    
   private void closeConnection()
   {
	   try {
	   in.close();
	   clientOut.close();
	   socket.close();
	   }
	   catch(IOException e)
	   {
		   System.out.println("Error: Closing connection goes wrong");
	   }
   }
   
   private void getStream() throws IOException
   {
       this.clientOut = new PrintWriter(socket.getOutputStream(), false);
       in = new Scanner(socket.getInputStream());
   }
    
   private boolean ifLeft(String type)
   {
	   if(type.equals("EXIT"))
		   return true;
	   return false;
   }
   
   private void sendToUser(String message)
   {
       clientOut.write(message + "\r\n");
       clientOut.flush();
   }
}