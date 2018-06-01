import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import static java.lang.Character.isDigit;

public class Client
{
  private Socket clientSocket;
  private PrintWriter write;
  private BufferedReader reader;
  private long startNanoSec;
  private Scanner keyboard;
  private ClientListener listener;

  private volatile int thneedsInStore;
  private volatile double balance;

  public Client(String host, int portNumber)
  {
    startNanoSec = System.nanoTime();
    System.out.println("Starting Client: " + timeDiff());

    keyboard = new Scanner(System.in);

    while (!openConnection(host, portNumber))
    {
    }
    
    listener = new ClientListener();
    System.out.println("Client(): Starting listener = : " + listener);
    listener.start();
    listenToUserRequests();
    closeAll();
  }

  private boolean openConnection(String host, int portNumber)
  {
    try
    {
      clientSocket = new Socket(host, portNumber);
    }
    catch (UnknownHostException e)
    {
      System.err.println("Client Error: Unknown Host " + host);
      e.printStackTrace();
      return false;
    }
    catch (IOException e)
    {
      System.err.println("Client Error: Could not open connection to " + host
          + " on port " + portNumber);
      e.printStackTrace();
      return false;
    }

    try
    {
      write = new PrintWriter(clientSocket.getOutputStream(), true);
    }
    catch (IOException e)
    {
      System.err.println("Client Error: Could not open output stream");
      e.printStackTrace();
      return false;
    }
    
    try
    {
      reader = new BufferedReader(new InputStreamReader(
          clientSocket.getInputStream()));
    }
    catch (IOException e)
    {
      System.err.println("Client Error: Could not open input stream");
      e.printStackTrace();
      return false;
    }
    return true;
  }

  private void listenToUserRequests()
  {
    while (true)
    {
      System.out.println("Thneeds in Inventory = " + thneedsInStore);
      System.out.println("Enter Command (Buy: # | Sell: #):");
      String cmd = keyboard.nextLine();
      if (cmd == null) continue;
      if (cmd.length() < 1) continue;

      char c = cmd.charAt(0);
      if (c == 'q')
      {
        listener.interrupt();
      }
      if(cmd.equals("Inventory") || cmd.equals("inventory"))
      {
        System.out.println("Inventory: " + thneedsInStore + ": Balance: " + balance);
      }

      write.println(cmd);
    }
  }

  public void closeAll()
  {
    System.out.println("Client.closeAll()");

    if (write != null) write.close();
    if (reader != null)
    {
      try
      {
        reader.close();
        clientSocket.close();
      }
      catch (IOException e)
      {
        System.err.println("Client Error: Could not close");
        e.printStackTrace();
      }
    }
  }

  private String timeDiff()
  {
    long namoSecDiff = System.nanoTime() - startNanoSec;
    double secDiff = (double) namoSecDiff / 1_000_000_000.0;
    return String.format("%.6f", secDiff);
  }

  public static void main(String[] args)
  {
    String host = null;
    int port = 0;
   
    try
    {
      host = args[0];
      port = Integer.parseInt(args[1]);
      if (port < 1) throw new Exception();
    }
    catch (Exception e)
    {
      System.out.println("Usage: Client hostname portNumber");
      System.exit(0);
    }
    new Client(host, port);
  }
  
  class ClientListener extends Thread
  {
    public void run()
    {
      System.out.println("ClientListener.run()");
      while (true)
      {
        read();
      }
    }

    private void read()
    {
      try
      {
        System.out.println("Client: listening to socket");
        String msg = reader.readLine();
        if (msg.startsWith("Thneeds:"))
        {
          int idxOfNum = msg.indexOf(':') + 1;
          int n = Integer.parseInt(msg.substring(idxOfNum));
          thneedsInStore = n;
          System.out.println("Current Inventory of Thneeds (" + timeDiff()
              + ") = " + thneedsInStore);
        }
        else if (msg.startsWith("Initialize"))
        {
          thneedsInStore = 0;
          balance = 1000;
        }
        /*else if(isDigit(msg.charAt(0)))
        {
          System.out.println("Broadcast: " + msg);
          int amount = extractAmount(msg);
          double cost = extractCost(msg);
          thneedsInStore = amount;
          balance = cost;
        }*/
        else if (msg.startsWith("You just bought "))
        {
          System.out.println("Success: " + msg);
        }
        else if (msg.startsWith("You just sold "))
        {
          System.out.println("Success: " + msg);
        }
        else if (msg.startsWith("Error"))
        {
          System.out.println("Failed: " + msg);
        }
        else if(msg.startsWith("Disconnected"))
        {
          closeAll();
        }
        else
        {
          System.out.println("Unrecognized message from Server(" + timeDiff()
              + ") = " + msg);
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
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
}
