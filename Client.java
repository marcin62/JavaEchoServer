import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Scanner;

public class Client {

    private static final String host = "localhost";
    private static final int portNumber = 4446;
    private String serverHost;
    private int serverPort;

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        Client client = new Client(host, portNumber);
        client.startClient(scan);
    }

    private Client (String host, int portNumber){
        this.serverHost = host;
        this.serverPort = portNumber;
    }

    private void startClient(Scanner scan){
        try{
            Socket socket = new Socket(serverHost, serverPort);
            Thread.sleep(1000); 

            ServerThread serverThread = new ServerThread(socket);
            Thread serverAccessThread = new Thread(serverThread);
            serverAccessThread.start();
            getDatafromConsole(scan,serverAccessThread,serverThread);
            
        }catch(IOException ex){
            System.err.println("Fatal Connection error!");
        }catch(InterruptedException ex){
            System.out.println("Interrupted");
        }

    }
    private void getDatafromConsole(Scanner scan,Thread serverAccessThread,ServerThread serverThread)
    {
    	while(serverAccessThread.isAlive()){
            if(scan.hasNextLine()){
               String tekst = scan.nextLine();
               serverThread.addNextMessage(tekst);
            }
        }
    }
}

 class ServerThread implements Runnable {
	PrintWriter serverOut;
    InputStream serverInStream;
    Scanner serverIn;
    
    private Socket socket;
    private final LinkedList<String> messagesToSend;
    private boolean hasMessages = false;

    public ServerThread(Socket socket) {
        this.socket = socket;
        messagesToSend = new LinkedList<String>();
    }

    public void addNextMessage(String message) {
        synchronized (messagesToSend) {
            hasMessages = true;
            messagesToSend.push(message);
        }
    }

    @Override
    public void run(){
        System.out.println("Local Port :" + socket.getLocalPort());

        try{
            getStream();
            boolean check=false;
            
            while(!socket.isClosed()&&check==false){
            	
            	check=printData();
                sendToServer();
                
            }
        }
        catch(IOException ex){
            System.out.println("Error: Problems with connection");
        }
        finally
        {
        	closeConnection();
        }

    }
    
    private boolean printData() throws IOException
    {
        if(serverInStream.available() > 0){
            if(serverIn.hasNextLine()){
            	String text=serverIn.nextLine();
            	System.out.println(text);
            	if(ifClosed(text))
            	{
            		return true;
            	}
            }
        }
       return false;
    }
    
    private void sendToServer()
    {
        String nextSend=getMessage();
        if( nextSend!=null){
            serverOut.println(nextSend);
            serverOut.flush();
        }
    }
    
    private void closeConnection()
    {
    	try {
    	serverOut.close();
    	serverIn.close();
    	socket.close();
        System.out.println("Disconected from server");
    	}
    	catch(IOException e)
    	{
    		System.out.println("Error: During closing the connection");
    	}
    }
    
    private void getStream() throws IOException
    {
    	serverOut = new PrintWriter(socket.getOutputStream(), false);
        serverInStream = socket.getInputStream();
        serverIn = new Scanner(serverInStream);
    }
    
    private boolean ifClosed(String type)
    {
 	   if(type.equals("CLOSE"))
 		   return true;
 	   return false;
    }
    
    private String getMessage()
    {
    	String nextSend = null;
    	if(hasMessages){
        synchronized(messagesToSend){
            nextSend = messagesToSend.pop();
            hasMessages = !messagesToSend.isEmpty();
        }
    	}
    	return nextSend;
    }
}