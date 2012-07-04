import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class Shutdown extends Thread{
	/*
	 * This thread polls the command line prompt for the 
	 * string "exit". Once received the thread terminates the 
	 * serversocket that crawler created, so that it cannot accept anymore
	 * connections from service providers.
	 */
	private static BufferedReader systemIn;
	
	public Shutdown(){
		
		systemIn = new BufferedReader(new InputStreamReader(System.in));
		
	}
	public void run() {
		try {
			
			while(!systemIn.readLine().equalsIgnoreCase("EXIT")) {
	        	
	        	System.out.println("\t...[Please type 'EXIT' to terminate broker...]");
	        	
	        }
			
		} catch (IOException ie) {
		
			ie.printStackTrace();
			
		}
		crawler.LockShutdown.writeLock().lock();
		
		try {
			
			if (!crawler.Shutdown) {
				
				crawler.Shutdown = true;
				
				System.out.println("Terminating listen...");
				
				crawler.welcomeSocket.close();
						
			}
			
		} catch (IOException ie) {
				
				ie.printStackTrace();
				 	
		} finally {
			
			crawler.LockShutdown.writeLock().unlock();
			
		} 
		
	}
}
