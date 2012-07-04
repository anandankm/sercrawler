import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.locks.*;
import java.util.StringTokenizer;
import java.net.*;

@SuppressWarnings("unchecked")
public class broker {
		
	public static final BufferedReader systemIn  = new BufferedReader(new InputStreamReader(System.in));
	
	public static ArrayList<String[]> RegisterBuffer = new ArrayList<String[]>(); // persistent broker database
	
	public static final ReentrantReadWriteLock LockRegister = new ReentrantReadWriteLock(true); // Lock for the database
	
	public static final ReentrantReadWriteLock LockShutdown = new ReentrantReadWriteLock(true); // Lock when attempting to terminate
	
	public static final ReentrantReadWriteLock LockchildThreads = new ReentrantReadWriteLock(true); // Lock on childThread counts
	
	public static ServerSocket welcomeSocket = null;
	
	public static boolean Shutdown = false; 
	
	public static int childThreads = 0; // child thread count
	
	public static String DatabaseFile = null;					
	
	public static String BindName = null;						
	
	
	/*
	 * Initialize serverPortNo before getting from config.ini
	 */
	private static int serverPortNo = 0;				
	
	public static void main(String args[]) throws IOException {
		
		int clientPortNo = 0;				// Initialize clientPortNo before getting from TCP connection
		
		String serverName;
		
		String clientName;

		/*
		 * Get the broker port Number, bind name and database file name from the config.ini file
		 */
		ServerConfig();
		
		ObjectInputStream DatabaseFromFile;
		
		File f = new File(DatabaseFile);
		
		if (f.exists()) {
			
			System.out.println("Populating broker Database from file  '" + DatabaseFile + "'...");
			
			try {
				
				DatabaseFromFile = new ObjectInputStream(new FileInputStream(DatabaseFile));
				
				RegisterBuffer = (ArrayList<String[]>)(DatabaseFromFile.readObject());
				
				DatabaseFromFile.close();
				
			} catch (IOException ioe) {
				
				System.out.println("Database input stream not closed/created");
				//ioe.printStackTrace();
				
			} catch (ClassNotFoundException CNF) {
				
				System.out.println("Database input stream class not found");
				
			}
			
		}
		
		/*
		 * Declaring server socket with broker port number.
		 */
		
		try {
			welcomeSocket = new ServerSocket(serverPortNo);
			
		} catch (IOException ioe) {
			
			System.out.println("Error Code: 0x001;\n" + 
					"System Exit: Could not create new Server Socket--> IOException --> "+ioe.getLocalizedMessage());
			
			//ioe.printStackTrace();
			
			welcomeSocket.close();
			
			System.exit(1);
		}
		
		/*
		 * Just to display the name of the server being started... :)
		 */
		serverName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
		
		System.out.println("Broker [" + serverName +"] Started... Listening on Port " + serverPortNo + "...");
		
		/*
		 * Shutdown thread that polls the command line prompt for the command "exit".
		 */
		BrokerShutdown shut = new BrokerShutdown();
		shut.start();
		
		/*
		 * Broker main thread listens to new service providers until "exit" is received from
		 * the console command line.
		 */
		while (!Shutdown) {
			
			Socket connectionSocket = null;
			
			System.out.println("Waiting for service providers...");	
			
			try {
				
				connectionSocket = welcomeSocket.accept();
				
				System.out.println("Accepted connection from a service provider...");
				
			} catch (IOException ioe) {
				
				if (Shutdown) {
					
					break;
				}
				
				System.out.println("Error Code: 0x004;\n" + 
						"System Exit: Could not start welcomeSocket.accept--> ServerSocket error --> IOException --> "+ioe.getLocalizedMessage());
				
				//ioe.printStackTrace();
				
				welcomeSocket.close();
				
				System.exit(1);
			}
			/*
			 * Just to display server's port number and name.
			 */
			clientPortNo = connectionSocket.getPort();
			
			clientName = connectionSocket.getInetAddress().getHostName();
			
			System.out.println("Incoming connection from the server :" + clientName + ", Port " +clientPortNo);
			
			serverChildThread child = new serverChildThread(connectionSocket);
			
			child.start();
			
			LockchildThreads.writeLock().lock();
			
			try {
				
				broker.childThreads++;
				
			} finally {
				
				LockchildThreads.writeLock().unlock();
				
			}
			
		}
		
		/*
		 * Terminate the server once all the childThreads handling the clients 
		 * are terminated.
		 */
		
		System.out.println("Waiting for child threads to terminate...");
		
		while (childThreads != 0) {
			
			try {
				
				Thread.sleep(1000);
				
			} catch (InterruptedException ie) {
				
				System.out.println("Interrupted when waiting for child threads to close..");
				
				//ie.printStackTrace();
				
			}  
			
		}
		
		ObjectOutputStream DatabaseToFile;
		
		System.out.println("Saving Server Database to file '" + DatabaseFile + "'...");
		
		try {
			
			DatabaseToFile = new ObjectOutputStream(new FileOutputStream(DatabaseFile));
			
			DatabaseToFile.writeObject(RegisterBuffer);
			
			DatabaseToFile.close();
			
		} catch (IOException ioe) {
			
			System.out.println("Database output stream not closed/created");
			//ioe.printStackTrace();
			
		}
		
		System.out.println("All the child threads are terminated...");
		
		System.out.println("Terminating the broker Main Thread...");
		
		System.exit(1);

	}
	/*
	 * Retrieval of broker port Number, bind name and database file name from config.ini file.
	 */
	private static void ServerConfig() throws IOException { 
		
		String ConfigFile = "config.ini";
		
		FileInputStream inputstream;
		
		BufferedReader in = null;
		
		StringTokenizer token = null;
		
		try {
			inputstream = new FileInputStream(ConfigFile);
			
			in = new BufferedReader(new InputStreamReader(new DataInputStream(inputstream)));
			
			String line;
			
			line = in.readLine();
			
			while (line != null) {
			
				token = new StringTokenizer(line);
				
				if (token.countTokens() <= 0) {
					
					 line = in.readLine();
				 
					 continue;
					 
				 }
				
				String token1 = token.nextToken();
			
				if (token1.equalsIgnoreCase("ServerPortNumber")) {
					
					serverPortNo = Integer.parseInt(token.nextToken());
					
					line = in.readLine();
					 
					continue;
					
				}
				
				if (token1.length() > 9) {
					
					if (token1.substring(0,8).equalsIgnoreCase("DATABASE")) {
						
						DatabaseFile = token1.substring(9);
						
						line = in.readLine();
						 
						continue;
						
					}
					
				}
				
				line = in.readLine();
			}
			
			inputstream.close();
			
		} catch (IOException e) {
			
			System.out.println("Config file input stream not closed/created");
			//e.printStackTrace();
			
		}
		
	}
	
}
