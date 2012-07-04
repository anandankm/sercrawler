import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class server {
	
	private static int brokerPortNo = 0;				// Initialize serverPortNo before getting from config.ini
	
	public  static String lowerfib = null;
	
	public  static String upperfib = null;
	
	private static String brokerName;					
	
	public static String serverName;
	
	public static long fiboThreadSleep = 0;
	
	public static final BufferedReader systemIn  = new BufferedReader(new InputStreamReader(System.in));
	
	public static final ReentrantReadWriteLock Locklowerfib = new ReentrantReadWriteLock(true);
	
	public static final ReentrantReadWriteLock Lockupperfib = new ReentrantReadWriteLock(true);
	
	public static boolean ReceivedError = false;		// Set true if server receives ERROR protocol from server
	
	public static boolean ReceivedStatus = false;		// Set true if server receives STATUS protocol from server
	
	public static int ReceivedQueryReply = -1;			// Set to number of RESPONSES [0 or more], server will get from server
	
	public static boolean ProtSeqError = false;			// Set true if server does not receive "HI\n" from server
	
	public static boolean GivenEnd = false;
	
	public static boolean GivenExit = false;
	
	public static void main(String args[]) throws IOException {
		
		brokerName = args[0];
	
		brokerPortNo = Integer.parseInt(args[1]);
		
		Repository Rp = new Repository();
		
		Buffer Buf=new Buffer();
	    // create new threads
		
		ServerConfig();
		
		try {
			
			Thread upperfibo = new fiboThread1(Buf);
			
		    Thread lowerfibo = new fiboThread2(Buf);
		    
		    upperfibo.start();
		    
			lowerfibo.start();
	    
		} catch (Exception e ) {
			
			System.out.println("Could not start fibonacci threads.. Exiting Gracefully");
			
			System.exit(1);
		}
	  
		
		Rp.serverAccess = true;
		
		System.out.println("Trying to connect to server [" + brokerName +"]...");
		
		serverName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
		
		Socket clientSocket = null;
		
		/* 
		 * Declaring socket with broker host name, broker port number.
		 */
		
		try {
			
			clientSocket = new Socket(brokerName,brokerPortNo);
			
		} catch (IOException ioe) {
			
			System.out.println("Error Code: 0x004;\n" + 
					"System Exit: Could not connect to server --> clientSocket error --> IOException --> " + 
					ioe.getLocalizedMessage());
			
			//ioe.printStackTrace();
			
			clientSocket.close();
			
			System.exit(1);
		}
		
		
		System.out.println("Service Provider [" + serverName + "] Established connection with "+ brokerName +"..." );
		/*
		 * Input, output streams for the socket
		 */
		PrintWriter out = new PrintWriter(
                clientSocket.getOutputStream(), true);
		
		BufferedReader in = new BufferedReader(
                  new InputStreamReader(
                      clientSocket.getInputStream()));

		String inputLine, outputLine;
		
		/*
		 *  "Server says 'Hello' to the broker"
		 */
		
		outputLine = "HELLO\n";
		
		out.print(outputLine);
		
		out.flush();
		
		inputLine = in.readLine();
		
		if (inputLine.equalsIgnoreCase("HI")) {
			
			System.out.println("Broker says 'Hi'...");
			
		} else {
			
			System.out.println("Input from Broker does not equal 'Hi'...");
			
			System.out.println("Broker says Protocol Sequence Error...");
			
			ProtSeqError = true;  
			
		}
		/*
		 * If the ProtSeqError is set true, the broker did not receive "HELLO" and it responded with "ERROR 0x002"
		 * and is going to close connection.
		 */
		if (!ProtSeqError) {
			
			if (args.length == 3) {
				
				System.out.println("Now processing the script file: script.txt...");
				
				Rp.ProcessScriptFile(args[2]);
				
			} else {
				
				System.out.println("Now processing the given scripts...");
				
				Rp.ProcessScript();
				
			}
			
			while (!Rp.ScriptBuffer.isEmpty()) {
				
				/*
				 * Get the first entry from the script buffer, send it to server.
				 */
				
				outputLine = Rp.ProcScriptBuffer(0);
				
				if (!outputLine.equalsIgnoreCase("SLEEP")) {
					
					out.print(outputLine);
					
					out.flush();
					
					/*
					 * Read the response from the broker, for the above sent message. 
					 */
					
					Rp.ProcessReadLine(in.readLine()); 
					
					/*
					 * The server received STATUS from the broker
					 */
					
					if (ReceivedStatus) {
						
						if (Rp.ReturnedStatus) {
							
							System.out.println("Broker Response: Success");
							
						} else {
							
							System.out.println("Broker Response: Malformed Request");
							
							Rp.ReturnedStatus = true;
							
						}
						
						ReceivedStatus = false;
					}
					
					/*
					 * The server received ERROR from the broker
					 */
					
					if (ReceivedError) {
						
						System.out.println("Broker Response: Error Code - " + Rp.ReceivedErrorCode);
						
						ReceivedError = false;
					}
					
					/*
					 * The server received QUERY-REPLY from the broker.
					 */
					
					if (ReceivedQueryReply >= 0) {
						
						System.out.println("Found " + ReceivedQueryReply + " Triples");
						
						for (int j = 0; j < ReceivedQueryReply; j++) 
							/*
							 * Expect ReceivedQueryReply number of RESPONSES from the broker.
							 */
							Rp.ProcessReadLine(in.readLine());
						
						ReceivedQueryReply = -1;
						
					}
					
				} else {
					
					System.out.println("\t done sleeping !!!");
					
				}
												
				Rp.ScriptBuffer.remove(0);
				
				if (args.length < 3) {
					
					Rp.ProcessScript();
					
				}
				
			}
			

			if (!GivenEnd) {
				
				System.out.println("'Script file/Script Commands' does not have END command at the end");
				
				System.out.println("\t Anyways, ending the service provider session...!");

			}
			
			
			if (!GivenExit) {
				
				while(!systemIn.readLine().equalsIgnoreCase("EXIT")) {
			    	
			    	System.out.println("\t...[Please type 'EXIT' to terminate this service provider...]");
			    	
			    }

			}
			
			while(!Rp.ServiceBuffer.isEmpty()) {
				
				outputLine = "REMOVE " + Rp.Services.get(0)+ " " + serverName + " " + Rp.ServiceBuffer.get(Rp.Services.get(0)) +"\n";
			
				System.out.println(outputLine);
				
				out.print(outputLine);
				
				out.flush();
				
				/*
				 * Read the response from the broker, for the above sent message. 
				 */
				
				Rp.ProcessReadLine(in.readLine()); 
				
				/*
				 * The server received STATUS from the broker.
				 */
				
				if (ReceivedStatus) {
					
					if (Rp.ReturnedStatus) {
						
						System.out.println("Broker Response: Success");
						
					} else {
						
						System.out.println("Broker Response: Malformed Request");
						
						Rp.ReturnedStatus = true;
						
					}
					
					ReceivedStatus = false;
				}
				/*
				 * The server received ERROR from the broker.
				 */
				if (ReceivedError) {
					
					System.out.println("Broker Response: Error Code - " + Rp.ReceivedErrorCode);
					
					ReceivedError = false;
				}
				
				Rp.ServiceBuffer.remove(Rp.Services.get(0));
				
				Rp.Services.remove(0);
			}
			
			outputLine = "BYE\n";
			
			out.print(outputLine);
			
			out.flush();
			
		}
		/*
		 * Just read the "LATER\n" message from the broker and close the connection.
		 */
		Rp.ProcessReadLine(in.readLine());
		
		try {
			
			in.close();
		
			out.close();
			
			clientSocket.close();
			
		} catch (IOException ie) {
			
			System.out.println("Server unable to close...");
			//ie.printStackTrace();
			
		}
			
		System.out.println("Service Provider Closed... Done!");
		
		System.exit(1);
	
	}
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
			
				if (token1.equalsIgnoreCase("fiboThreadSleep")) {
					
					fiboThreadSleep = Long.parseLong(token.nextToken());
					
					line = in.readLine();
					 
					continue;
					
				}
								
				line = in.readLine();
			}
			
			inputstream.close();
			
		} catch (IOException e) {
			
			System.out.println("Server unable to read from config file...");
			//e.printStackTrace();
			
		}
		
	}
}
