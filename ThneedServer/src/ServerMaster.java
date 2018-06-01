import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class ServerMaster
{
  private ServerSocket serverSocket;
  private LinkedList<ServerWorker> allConnections = new LinkedList<>();
  ThneedStore thneedStore;
  private double startTime;

  public ServerMaster(int portNumber)
  {
    startTime = System.nanoTime();
    try
    {
      serverSocket = new ServerSocket(portNumber);
      thneedStore = new ThneedStore(this);
    }
    catch (IOException e)
    {
      System.err.println("Server error: Opening socket failed.");
      e.printStackTrace();
      System.exit(-1);
    }

    waitForConnection(portNumber);
  }

  public void waitForConnection(int port)
  {
    String host = "";
    try
    {
      host = InetAddress.getLocalHost().getHostName();
    }
    catch (UnknownHostException e)
    {
      e.printStackTrace();
    }
    while (true)
    {
      System.out.println("ServerMaster(" + host +
              "): waiting for Connection on port: " + port);
      try
      {
        Socket client = serverSocket.accept();
        ServerWorker worker = new ServerWorker(client, this, thneedStore);
        worker.start();
        System.out.println("ServerMaster: *********** new Connection");
        allConnections.add(worker);
        worker.send("ServerMaster says hello!");
      }
      catch (IOException e)
      {
        System.err.println("Server error: Failed to connect to client.");
        e.printStackTrace();
      }
    }
  }

  public void cleanConnectionList(ServerWorker serverWorker)
  {
    allConnections.remove(serverWorker);
  }

  public void broadcast(String s)
  {
    for (ServerWorker workers : allConnections)
    {
      workers.send(timeDifference() + ": " + s);
    }
  }
  
  private String timeDifference()
  {
    double difference = System.nanoTime() - startTime;
    double seconds = difference / 1_000_000_000.0;
    return String.format("%.3f", seconds);
  }
  
  public static void main(String args[])
  {
    //Valid port numbers are Port numbers are 1024 through 65535.
    //  ports under 1024 are reserved for system services http, ftp, etc.
    int port = 5555; //default
    if (args.length > 0)
    try
    {
      port = Integer.parseInt(args[0]);
      if (port < 1) throw new Exception();
    }
    catch (Exception e)
    {
      System.out.println("Usage: ServerMaster portNumber");
      System.exit(0);
    }
    
    new ServerMaster(port);
  }
}
