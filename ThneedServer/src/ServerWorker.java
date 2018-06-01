import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerWorker extends Thread
{
  private Socket client;
  private PrintWriter clientWriter;
  private BufferedReader clientReader;
  private ServerMaster serverMaster;
  private ThneedStore thneedStore;

  public ServerWorker(Socket client, ServerMaster serverMaster, ThneedStore thneedStore)
  {
    this.client = client;
    this.serverMaster = serverMaster;
    this.thneedStore = thneedStore;

    try
    {
      //PrintWriter(OutputStream out, boolean autoFlushOutputBuffer)
      clientWriter = new PrintWriter(client.getOutputStream(), true);
    } catch (IOException e)
    {
      System.err.println("Server Worker: Could not open output stream");
      e.printStackTrace();
    }
    try
    {
      clientReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

    } catch (IOException e)
    {
      System.err.println("Server Worker: Could not open input stream");
      e.printStackTrace();
    }
  }

  //Called by ServerMaster
  public void send(String msg)
  {
    System.out.println("ServerWorker.send(" + msg + ")");
    clientWriter.println(msg);
  }

  public void read()
  {
    try
    {
      String message = clientReader.readLine();

      if (message.startsWith("buy") || message.startsWith("Buy"))
      {
        int amount = extractAmount(message);
        double cost = extractCost(message);

        if (thneedStore.buyThneeds(amount, cost))
        {
          send("You just bought " + amount + " thneeds for " + cost + " each");
        }
        else
        {
          send("Error: not enough money.");
        }
      }
      else if (message.startsWith("sell") || message.startsWith("Sell"))
      {
        int amount = extractAmount(message);
        double cost = extractCost(message);

        if (thneedStore.sellThneeds(amount, cost))
        {
          send("You just sold " + amount + " thneeds for " + cost + " each");
        } else
        {
          send("Error: Not enough thneeds in inventory.");
        }
      }
      else if (message.startsWith("q") || message.startsWith("Q"))
      {
        send("Disconnected.");
        interrupt();
      }
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private int extractAmount(String message)
  {
    String[] s = message.split(" ");
    return Integer.parseInt(s[1]);
  }

  private double extractCost(String message)
  {
    String[] s = message.split(" ");
    return Double.parseDouble(s[2]);
  }
  
  public void run()
  {
    send("Initialize " +  thneedStore.getInventory() + " " + thneedStore.getBalance());
    
    while(!interrupted())
    {
      read();
    }
    
    if(clientWriter != null)
    {
      clientWriter.close();
    }
    
    try
    {
      if(clientReader != null)
      {
        clientReader.close();
      }
      
      if(client != null)
      {
        client.close();
      }
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    
    serverMaster.cleanConnectionList(this);
    System.out.println("Connection disconnected");
  }
}
