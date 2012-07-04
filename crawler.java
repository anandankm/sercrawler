import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.locks.*;
import java.util.StringTokenizer;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;


public class crawler {
		
	public static ArrayList<String[]> RegisterBuffer = new ArrayList<String[]>();
	/*
	 * 1. Cube Root Databases and Locks
	 */
	public static ArrayList<String> CubeRootHosts = new ArrayList<String>();
	
	public static ArrayList<String> CubeRootSA = new ArrayList<String>();
	
	public static final ReentrantReadWriteLock LockCubeRootHosts = new ReentrantReadWriteLock(true);
	
	public static final ReentrantReadWriteLock LockCubeRootSA = new ReentrantReadWriteLock(true);
	/*
	 * 2. Square Root Databases and Locks
	 */
	public static ArrayList<String> SquareRootHosts = new ArrayList<String>();
	
	public static ArrayList<String> SquareRootSA = new ArrayList<String>();
	
	public static final ReentrantReadWriteLock LockSquareRootHosts = new ReentrantReadWriteLock(true);
	
	public static final ReentrantReadWriteLock LockSquareRootSA = new ReentrantReadWriteLock(true);
	/*
	 * 3. Server Time Database and Lock
	 */
	public static ArrayList<String> ServerTimeHosts = new ArrayList<String>();
	
	public static final ReentrantReadWriteLock LockServerTimeHosts = new ReentrantReadWriteLock(true);
	/*
	 * 4. SHA Digest Databases and Locks
	 */
	public static ArrayList<String> SHADigestHosts = new ArrayList<String>();
	
	public static ArrayList<String> SHADigestSA = new ArrayList<String>();
	
	public static final ReentrantReadWriteLock LockSHADigestHosts = new ReentrantReadWriteLock(true);
	
	public static final ReentrantReadWriteLock LockSHADigestSA = new ReentrantReadWriteLock(true);
	/*
	 * 5. Golden Ratio Databases and Locks
	 */
	public static ArrayList<String> GoldenRatioHosts = new ArrayList<String>();
	
	public static ArrayList<String> GoldenRatioSA = new ArrayList<String>();
	
	public static final ReentrantReadWriteLock LockGoldenRatioHosts = new ReentrantReadWriteLock(true);
	
	public static final ReentrantReadWriteLock LockGoldenRatioSA = new ReentrantReadWriteLock(true);
	
	/*
	 *  Terminator Locks
	 */ 
	
	public static final ReentrantReadWriteLock LockShutdown = new ReentrantReadWriteLock(true);
	
	public static final ReentrantReadWriteLock LockchildThreads = new ReentrantReadWriteLock(true);
	
	public static final ReentrantReadWriteLock LockcrawlerClientThreads = new ReentrantReadWriteLock(true);
	
	public static final ReentrantReadWriteLock LockcrawlerClientEnd = new ReentrantReadWriteLock(true);
	
	/*
	 * *********--------************
	 */
	public static ServerSocket welcomeSocket = null;
	/*
	 *  Terminators
	 */
	public static boolean Shutdown = false;
	
	public static boolean CrawlerClientEnd = false;
	
	public static int crawlerClientThreads = 0;
	
	public static int childThreads = 0;
	/*
	 * *********--------************
	 */
	public static int crawlerPortNo = 0;				// Initialize CrawlerPortNo before getting from config.ini
	
	private static int brokerPortNo = 0;				// Initialize brokerPortNo before getting from command line
	
	private static String brokerName = null;			// Initialize brokerIP before getting from command line
	
	private static String crawlerName = null;			// Initialize brokerIP before getting from command line
	
	public static int CrawlerPeriodicity = 0;
	
	public static void main(String args[]) throws IOException {
		
		if (args.length < 2) {
			
			System.out.println("Please enter brokerIP, broker Port number as input arguments...");
			
			System.exit(1);
			
		} else {
			
			brokerName = args[0];
			
			brokerPortNo = Integer.parseInt(args[1]);
		}
		
		crawlerName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
		
		byte[] thisip = InetAddress.getLocalHost().getAddress();
		
		/*
		 * Retrieve Crawler Listening Port Number from config.ini. At this port number crawler listens
		 * to clients. Retrieve Crawler periodicity.
		 */
		
		ConfigRetrieve();
		
		System.out.println("Trying to connect to broker [" + brokerName +"]...");
		
		Socket crawlerClientSocket = null;
		
		/* 
		 * Declaring crawler client socket with broker host name, broker port number,
		 * crawler client inetaddress["java.net.InetAddress.getByAddress(thisip)"], and crawler client port number.
		 */
		
		int crawlerClientPorts = crawlerPortNo;
		
		String[] services = {
				
				"cube-root",
				"square-root",
				"sha-digest",
				"golden-ratio",
				"server-time"
				
		};
		/*
		 * Establish connection with broker for each service.
		 * Each crawler client queries the broker, updates the crawler database.
		 * 
		 */
		for (int i = 0; i < 5; i++) {
			
			crawlerClientPorts++;
			/*
			 * Each crawler client establishing connection with broker listens in incremented port numbers from 
			 * that of crawler main thread listening for clients.
			 */
			try {
				
				crawlerClientSocket = new Socket(brokerName,brokerPortNo,java.net.InetAddress.getByAddress(thisip),crawlerClientPorts);
				
			} catch (IOException ioe) {
				
				System.out.println("Error Code: 0x004;\n" + 
						"System Exit: Could not connect to broker --> crawlerClientSocket error --> IOException --> " + 
						ioe.getLocalizedMessage());
				
				//ioe.printStackTrace();
				
				crawlerClientSocket.close();
				
				System.exit(1);
			}
			
			crawlerClient crawlerClientThread = new crawlerClient(crawlerClientSocket, services[i]);
			
			crawlerClientThread.start();
			
			LockcrawlerClientThreads.writeLock().lock();
			
			try {
				
				crawlerClientThreads++;
				
			} finally {
				
				LockcrawlerClientThreads.writeLock().unlock();
				
			}
			
		}
		
		System.out.println("Crawler: [" + crawlerName +"] Established connection with [" + brokerName + "]");

		
		/*
		 * Declaring server socket with crawler port number.
		 */
		
		
		try {
			
			welcomeSocket = new ServerSocket(crawlerPortNo);
			
		} catch (IOException ioe) {
			
			System.out.println("Error Code: 0x001;\n" + 
					"System Exit: Could not create new Server Socket--> IOException --> "+ioe.getLocalizedMessage());
			
			//ioe.printStackTrace();
			
			welcomeSocket.close();
			
			System.exit(1);
		}
		
		/*
		 * Just to display the name of the Crawler being started... :)
		 */ 
		
		System.out.println("Crawler [" + crawlerName +"] Started... Listening on Port " + crawlerPortNo + "...");
		
		/*
		 * Thread that polls the command line prompt for "exit"
		 */
		
		Shutdown shut = new Shutdown();
		shut.start();
		
		/*
		 * Crawler main thread listens to new clients until "exit" 
		 * is received from command line prompt.
		 */
		
		while (!Shutdown) {
			
			Socket connectionSocket = null;
			
			System.out.println("Waiting for clients...");	
			
			try {
				
				connectionSocket = welcomeSocket.accept();
				
				System.out.println("Accepted connection from a client...");
				
			} catch (IOException ioe) {
				
				if (Shutdown) {
					/*
					 * "exit" is issued at the command prompt.
					 * exiting listening to clients
					 */
					break;
				}
				
				System.out.println("Error Code: 0x004;\n" + 
						"System Exit: Could not start welcomeSocket.accept--> ServerSocket error --> IOException --> "+ioe.getLocalizedMessage());
				
				//ioe.printStackTrace();
				
				welcomeSocket.close();
				
				System.exit(1);
			}
			/*
			 * Just to display client's port number and name.
			 */
			int clientPortNo = connectionSocket.getPort();
			
			String clientName = connectionSocket.getInetAddress().getHostName();
			
			System.out.println("Incoming connection from the client :" + clientName + ", Port " + clientPortNo);
			
			crawlerChildThread child = new crawlerChildThread(connectionSocket);
			
			child.start();
			
			LockchildThreads.writeLock().lock();
			
			try {
				/*
				 * child thread count -  to keep track of how many clients are connected
				 */
				childThreads++;
				
			} finally {
				
				LockchildThreads.writeLock().unlock();
				
			}
			
		}
		
		/*
		 * Terminate the crawler once all the childThreads handling the clients 
		 * are terminated.
		 */
		
		while (childThreads != 0) {
			
			try {
				
				System.out.println("Waiting for child Threads to terminate...");
				
				Thread.sleep(500);
				
			} catch (InterruptedException ie) {
				
				System.out.println("Interrupted when waiting for childthreads to close..");
				//ie.printStackTrace();
				
			}  
			
		}
		
		System.out.println("All the child Threads are terminated...");
		
		System.out.println("Terminating Crawler client threads that poll Broker Database...");
		
		LockcrawlerClientEnd.writeLock().lock();
		
		try {
			/*
			 * Asking crawlers connected to broker to terminate
			 */
			CrawlerClientEnd = true;
			
		} finally {
			
			LockcrawlerClientEnd.writeLock().unlock();
			
		}
		
		
		while (crawlerClientThreads != 0) {
			
			try {
				
				System.out.println("Waiting for Crawler client Threads to terminate...");
				
				Thread.sleep(500);
				
			} catch (InterruptedException ie) {
				
				System.out.println("Interrupted while waiting for crawler client threads to close..");
				//ie.printStackTrace();
				
			}  
			
		}
		
		System.out.println("Terminating the Crawler Main Thread...");

	}
	/*
	 * Retrieval of Crawler port number & periodicity from config.ini file.
	 */
	private static void ConfigRetrieve() throws IOException { 
		
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
			
				if (token1.equalsIgnoreCase("CrawlerPortNumber")) {
					
					crawlerPortNo = Integer.parseInt(token.nextToken());
					
				}
				
				if (token1.equalsIgnoreCase("CrawlerPeriodicity")) {
					
					CrawlerPeriodicity = Integer.parseInt(token.nextToken());
					
				}
				
				line = in.readLine();
			}
			
			inputstream.close();
			
		} catch (IOException e) {
			
			System.out.println("Config input/output stream not closed/created");
			//e.printStackTrace();
			
		}
		
	}
	
}
