import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class BrokerShutdown extends Thread{
	/*
	 * This thread polls the command line prompt for the 
	 * string "exit". Once received the thread terminates the 
	 * serversocket that broker created, so that it cannot accept anymore
	 * connections from service providers.
	 */
	private static BufferedReader systemIn;
	
	public BrokerShutdown(){
		
		systemIn = new BufferedReader(new InputStreamReader(System.in));
		
	}
	public void run() {
		try {
			
			while(!systemIn.readLine().equalsIgnoreCase("EXIT")) {
	        	
	        	System.out.println("\t...[Please type 'EXIT' to terminate broker...]");
	        	
	        }
			
		} catch (IOException ie) {
		
			System.out.println("Standard Input not readable");
			//ie.printStackTrace();
			
		}
		broker.LockShutdown.writeLock().lock();
		
		try {
			
			if (!broker.Shutdown) {
				
				broker.Shutdown = true;
				
				System.out.println("Terminating listen...");
				
				broker.welcomeSocket.close();
						
			}
			
		} catch (IOException ie) {
			
			System.out.println("Broker welcomesocket not closed");
				
				//ie.printStackTrace();
				 	
		} finally {
			
			broker.LockShutdown.writeLock().unlock();
			
		} 
		
	}
}
