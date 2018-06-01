/**
 * Created by Anton on 11/3/2016.
 */
public class ThneedStore
{
  private int inventory;
  private double balance;
  private ServerMaster serverMaster;
  
  public ThneedStore(ServerMaster serverMaster)
  {
    this.serverMaster = serverMaster;
    inventory = 0;
    balance = 1000;
  }
  
  public synchronized int getInventory()
  {
    return inventory;
  }
  
  public synchronized double getBalance()
  {
    return balance;
  }
  
  public synchronized boolean buyThneeds(int amount, double cost)
  {
    double totalCost = amount * cost;
    if(balance >= totalCost)
    {
      inventory += amount;
      balance -= totalCost;
      serverMaster.broadcast("inventory=" + inventory + " : treasury=" + balance);
      return true;
    }
    
    return false;
  }
  
  public synchronized boolean sellThneeds(int amount, double cost)
  {
    if(inventory >= amount)
    {
      inventory -= amount;
      balance += (amount * cost);
      serverMaster.broadcast("inventory=" + inventory + " : treasury=" + balance);
      return true;
    }
    
    return false;
  }
}
