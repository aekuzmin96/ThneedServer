CS351 - Lab5: Thneed Store Server
Anton Kuzmin

> Summary: 
  This program uses client/server socket connections to simulate a store that sells and buys thneeds. This is the server part of the program.

> Run program as java -jar ServerMaster 5555

> Contents:
  .jar file: contains the source folder with all of the .java files, a README, 
	and the compiled .class files

> Main Class: ServerMaster.java

> Other comments:
- First, run the ServerMaster java file. This will start up the server and wait for a connection from a client
- Every time a client connects, a serverWorker is assigned to it, which listens to buy/sell messages
- If there is not enough money in the balance to buy or not enough thneeds to sell, then the transaction is rejected
- The methods in the Thneed Store are synchronised to avoid concurrent modification from different serverWorkers