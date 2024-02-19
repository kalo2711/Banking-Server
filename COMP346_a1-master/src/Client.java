
import java.util.Scanner;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.InputMismatchException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/** Client class
 *
 * @author Kerly Titus
 */

public class Client extends Thread{
    
    private static int numberOfTransactions;   		/* Number of transactions to process */
    private static int maxNbTransactions;      		/* Maximum number of transactions */
    private static Transactions [] transaction; 	/* Transactions to be processed */
    private static Network objNetwork;          	/* Client object to handle network operations */
    private String clientOperation;    				/* sending or receiving */
    
	/** Constructor method of Client class
 	 * 
     * @return 
     * @param
     */
     Client(String operation)
     { 
       if (operation.equals("sending"))
       { 
           System.out.println("\n Initializing client sending application ...");
           numberOfTransactions = 0;
           maxNbTransactions = 100;
           transaction = new Transactions[maxNbTransactions];  
           objNetwork = new Network("client");
           clientOperation = operation; 
           System.out.println("\n Initializing the transactions ... ");
           readTransactions();
           System.out.println("\n Connecting client to network ...");
           String cip = objNetwork.getClientIP();
           if (!(objNetwork.connect(cip)))
           {   System.out.println("\n Terminating client application, network unavailable");
               System.exit(0);
           }
       	}
       else
    	   if (operation.equals("receiving"))
           { 
    		   System.out.println("\n Initializing client receiving application ...");
    		   clientOperation = operation; 
           }
     }
           
    /** 
     * Accessor method of Client class
     * 
     * @return numberOfTransactions
     * @param
     */
     public int getNumberOfTransactions()
     {
         return numberOfTransactions;
     }
         
    /** 
     * Mutator method of Client class
     * 
     * @return 
     * @param nbOfTrans
     */
     public void setNumberOfTransactions(int nbOfTrans)
     { 
         numberOfTransactions = nbOfTrans;
     }
         
    /** 
     * Accessor method of Client class
     * 
     * @return clientOperation
     * @param
     */
     public String getClientOperation()
     {
         return clientOperation;
     }
         
    /** 
     * Mutator method of Client class
	 * 
	 * @return 
	 * @param operation
	 */
	 public void setClientOperation(String operation)
	 { 
	     clientOperation = operation;
	 }
         
    /** 
     * Reading of the transactions from an input file
     * 
     * @return 
     * @param
     */
     public void readTransactions()
     {
        Scanner inputStream = null;     /* Transactions input file stream */
        int i = 0;                      /* Index of transactions array */
        
        try
        {
        	inputStream = new Scanner(new FileInputStream("src/transaction.txt"));
        }
        catch(FileNotFoundException e)
        {
            System.out.println("File transaction.txt was not found");
            System.out.println("or could not be opened.");
            System.exit(0);
        }
        while (inputStream.hasNextLine( ))
        {
            try
            {   transaction[i] = new Transactions();
                transaction[i].setAccountNumber(inputStream.next());            /* Read account number */
                transaction[i].setOperationType(inputStream.next());            /* Read transaction type */
                transaction[i].setTransactionAmount(inputStream.nextDouble());  /* Read transaction amount */
                transaction[i].setTransactionStatus("pending");                 /* Set current transaction status */
                i++;
            }
             catch(InputMismatchException e)
            {
                System.out.println("Line " + i + "file transactions.txt invalid input");
                System.exit(0);
            }
            
        }
        setNumberOfTransactions(i);		/* Record the number of transactions processed */
        
        
        inputStream.close( );

     }
     
    /** 
     * Sending the transactions to the server 
     * 
     * @return 
     * @param
     */
     public void sendTransactions()
     {
         int i = 0;     /* index of transaction array */
         
         while (i < getNumberOfTransactions())
         {  
            // while( objNetwork.getInBufferStatus().equals("full") );     /* Alternatively, busy-wait until the network input buffer is available */
                                             	
            transaction[i].setTransactionStatus("sent");   /* Set current transaction status */
           
            
            objNetwork.send(transaction[i]);                            /* Transmit current transaction */
            i++;
         }
         
    }
         
 	/** 
  	 * Receiving the completed transactions from the server
     * 
     * @return 
     * @param transact
     */
     public void receiveTransactions(Transactions transact)
     {
         int i = 0;     /* Index of transaction array */

         while (i < getNumberOfTransactions())
         {
        	 // while( objNetwork.getOutBufferStatus().equals("empty"));  	/* Alternatively, busy-wait until the network output buffer is available */

            objNetwork.receive(transact);                               	/* Receive updated transaction from the network buffer */

            //System.out.println("\n DEBUG : Client.receiveTransactions() - receiving updated transaction on account " + transact.getAccountNumber());

            System.out.println(transact);                               	/* Display updated transaction */
            i++;
         }
    }
     
    /** 
     * Create a String representation based on the Client Object
     * 
     * @return String representation
     * @param 
     */
     public String toString() 
     {
    	 return ("\n client IP " + objNetwork.getClientIP() + " Connection status" + objNetwork.getClientConnectionStatus() + "Number of transactions " + getNumberOfTransactions());
     }
    
    /** Code for the run method
     * 
     * @return 
     * @param
     */
    // overrided this method
    public void run()
    {

    	Transactions transact = new Transactions();
        // Initialize all time variables at declaration, to prevent not initialized error
        long sendClientStartTime = 0, sendClientEndTime = 0, receiveClientStartTime = 0, receiveClientEndTime = 0;


        // Wait for the server to be connected
        while(!objNetwork.getServerConnectionStatus().equals("connected")) {
            Thread.yield();
        }

        if (clientOperation.equals("sending")) {
            // Mark the start time of sending transactions
            sendClientStartTime = System.currentTimeMillis();

            sendTransactions();  // Method to send transactions

            // Mark the end time of sending transactions
            sendClientEndTime = System.currentTimeMillis();
        } else if (clientOperation.equals("receiving")) {
            // Mark the start time of receiving transactions
            receiveClientStartTime = System.currentTimeMillis();

            receiveTransactions(transact);  // Method to receive transactions
            objNetwork.disconnect(objNetwork.getClientIP());  // Disconnect after receiving

            // Mark the end time of receiving transactions
            receiveClientEndTime = System.currentTimeMillis();
        } else {
            System.out.println("Unknown client operation mode.");
            System.exit(1);
        }

        // Determine and print the running time based on the operation mode
        if (clientOperation.equals("sending")) {
            System.out.println("\n Terminating " + clientOperation + " client thread - Running time: " + (sendClientEndTime - sendClientStartTime) + " ms");
        } else if (clientOperation.equals("receiving")) {
            System.out.println("\n Terminating " + clientOperation + " client thread - Running time: " + (receiveClientEndTime - receiveClientStartTime) + " ms");
        }
    }

}
