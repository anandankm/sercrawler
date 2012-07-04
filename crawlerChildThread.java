import java.io.*;
import java.net.Socket;

public class crawlerChildThread extends Thread{
		
	private Socket connectionSocket = null;
	
	private PrintWriter out = null;
	
	private BufferedReader in = null;
	
	private Repository Rp = new Repository();
	
	public crawlerChildThread (Socket cxnSocket) {
		
		this.connectionSocket = cxnSocket;	
		
	}
	
	public void run() {
		
		/*
		 * Input, output streams for the socket
		 */
		String inputLine  = null;
		
		String outputLine = null;
		
		try {
			
			this.out = new PrintWriter(
	                connectionSocket.getOutputStream(), true);
			
			this.in = new BufferedReader(
	                  new InputStreamReader(
	                      connectionSocket.getInputStream()));
	
			inputLine = in.readLine();
			
		} catch (IOException ie) {
		
			System.out.println("Crawler child thread out, in created or Hello from client not read");
			//ie.printStackTrace();
		
		} 
	
		if (inputLine.equalsIgnoreCase("Hello")) {
			
			System.out.println("Client says 'Hello'...");
			
			/*
			 * Crawler sends back 'Hi'
			 */
			
			outputLine = "HI\n";
			
			out.print(outputLine);
			
			out.flush();
			
		} else {
			
			System.out.println("Input from Client does not equal 'Hello'");
			
			outputLine = "ERROR 0x002\n";
			
			out.print(outputLine);
			
			out.flush();
			
			/*
			 * Once ReceivedBye is set to true, server ends the connection.
			 * Note that here client did not send BYE, but the connection is ended because,
			 * the client did not send appropriate HELLO protocol message to start.
			 */
			Rp.ReceivedBye = true;
			
		}
		
		while (!Rp.ReceivedBye) {
			
			
			/*
			 * Read the message from the client and determine the appropriate message to send back 
			 */
			try {
				
				Rp.ProcessReadLine(in.readLine());
				
			} catch (IOException ie) {
				
				System.out.println("Input from client not readable");
				//ie.printStackTrace();
				
			}
			/*
			 * Need to send ERROR message
			 */
			if (Rp.sendError) {
				
				outputLine = "ERROR " + Rp.ErrorCode +"\n";
				
				out.print(outputLine);
				
				out.flush();
				
				Rp.sendError = false;
				
			} else if (Rp.sendStatus) {
				/*
				 * Need to send STATUS message
				 */
				
				if (Rp.Status) {
					
					outputLine = "STATUS TRUE\n";
					
				} else {
					
					outputLine = "STATUS FALSE\n";
					
					Rp.Status = true;
				}
				
				out.print(outputLine);
				
				out.flush();
				
				Rp.sendStatus = false;
				
			} else if (Rp.sendQueryReply) {
				/*
				 * Need to send QUERY-REPLY message
				 */
				
				int size = 0;
				
				if (Rp.lineArray[1].equalsIgnoreCase("cube-root")) {
					
					crawler.LockCubeRootHosts.readLock().lock();
					
					try {
						
						size = crawler.CubeRootHosts.size();
						
						outputLine = "QUERY-REPLY " + Rp.lineArray[0] + " " + size + "\n";
						
						out.print(outputLine);
						
						out.flush();
						
						for (int i = 0; i < size; i++) {
							
							outputLine = "RESPONSE " + Rp.lineArray[0] + " " + (i+1) + " cube-root " + 
											crawler.CubeRootHosts.get(i) + " " + crawler.CubeRootSA.get(i) +"\n";
							
							out.print(outputLine);
							
							out.flush();
							
						}
						
						
					} catch (Exception e) {
						System.out.println("Crawler child Thread unable to send msg out at cube-root...");	
							
						//e.printStackTrace();
							 	
					} finally {
						
						crawler.LockCubeRootHosts.readLock().unlock();
						
					}  
					
				} else if (Rp.lineArray[1].equalsIgnoreCase("square-root")) {
					
					crawler.LockSquareRootHosts.readLock().lock();
					
					try {
						
						size = crawler.SquareRootHosts.size();
						
						outputLine = "QUERY-REPLY " + Rp.lineArray[0] + " " + size + "\n";
						
						out.print(outputLine);
						
						out.flush();
						
						for (int i = 0; i < size; i++) {
							
							outputLine = "RESPONSE " + Rp.lineArray[0] + " " + (i+1) + " square-root " + 
											crawler.SquareRootHosts.get(i) + " " + crawler.SquareRootSA.get(i) +"\n";
							
							out.print(outputLine);
							
							out.flush();
							
						}
						
						
					} catch (Exception e) {
							
						System.out.println("Crawler child Thread unable to send msg out at square root...");
							//e.printStackTrace();
							 	
					} finally {
						
						crawler.LockSquareRootHosts.readLock().unlock();
						
					}  
					
				} else if (Rp.lineArray[1].equalsIgnoreCase("golden-ratio")) {
					
					crawler.LockGoldenRatioHosts.readLock().lock();
					
					try {
						
						size = crawler.GoldenRatioHosts.size();
						
						outputLine = "QUERY-REPLY " + Rp.lineArray[0] + " " + size + "\n";
						
						out.print(outputLine);
						
						out.flush();
						
						for (int i = 0; i < size; i++) {
							
							outputLine = "RESPONSE " + Rp.lineArray[0] + " " + (i+1) + " golden-ratio " + 
											crawler.GoldenRatioHosts.get(i) + " " + crawler.GoldenRatioSA.get(i) +"\n";
							
							out.print(outputLine);
							
							out.flush();
							
						}
						
						
					} catch (Exception e) {
						
						System.out.println("Crawler child Thread unable to send msg out at golden ratio...");
							
							//e.printStackTrace();
							 	
					} finally {
						
						crawler.LockGoldenRatioHosts.readLock().unlock();
						
					}  
					
				} else if (Rp.lineArray[1].equalsIgnoreCase("sha-digest")) {
					
					crawler.LockSHADigestHosts.readLock().lock();
					
					try {
						
						size = crawler.SHADigestHosts.size();
						
						outputLine = "QUERY-REPLY " + Rp.lineArray[0] + " " + size + "\n";
						
						out.print(outputLine);
						
						out.flush();
						
						for (int i = 0; i < size; i++) {
							
							outputLine = "RESPONSE " + Rp.lineArray[0] + " " + (i+1) + " sha-digest " + 
											crawler.SHADigestHosts.get(i) + " " + crawler.SHADigestSA.get(i) +"\n";
							
							out.print(outputLine);
							
							out.flush();
							
						}
						
						
					} catch (Exception e) {
						System.out.println("Crawler child Thread unable to send msg out at sha digest...");
							//e.printStackTrace();
							 	
					} finally {
						
						crawler.LockSHADigestHosts.readLock().unlock();
						
					}  
					
				} else if (Rp.lineArray[1].equalsIgnoreCase("server-time")) {
					
					crawler.LockServerTimeHosts.readLock().lock();
					
					try {
						
						size = crawler.ServerTimeHosts.size();
						
						outputLine = "QUERY-REPLY " + Rp.lineArray[0] + " " + size + "\n";
						
						out.print(outputLine);
						
						out.flush();
						
						for (int i = 0; i < size; i++) {
							
							outputLine = "RESPONSE " + Rp.lineArray[0] + " " + (i+1) + " server-time " + 
											crawler.ServerTimeHosts.get(i) + "\n";
							
							out.print(outputLine);
							
							out.flush();
							
						}
						
						
					} catch (Exception e) {
						System.out.println("Crawler child Thread unable to send msg out at server time...");
							//e.printStackTrace();
							 	
					} finally {
						
						crawler.LockServerTimeHosts.readLock().unlock();
						
					}  
					
				} else {
					
					outputLine = "ERROR 0x003\n";
					
					out.print(outputLine);
					
					out.flush();
					
				}
								
				Rp.sendQueryReply = false;
		
			} else if(Rp.sendOK){
				
				outputLine = "OK\n";
				
				out.print(outputLine);
				
				out.flush();	
				
				Rp.sendOK = false;
				
				
			} else if(!Rp.ReceivedBye){  		// crawler could have received BYE after entering the while loop..
				/*
				 * Need to send error stating that protocol message sent by client does not match
				 * any protocol sequence specified [Neither QUERY nor REGISTER nor REMOVE nor BYE] .
				 */
				outputLine = "ERROR 0x005\n";
				
				out.print(outputLine);
				
				out.flush();
				
			}

		}
	
		outputLine = "LATER\n";
	
		out.print(outputLine);
	
		out.flush();
	
		try {
			in.close();
		
			out.close();
			
			connectionSocket.close();
			
			System.out.println("Remote Connection Closed!");
			
			/*
			 * The count for number of crawler child threads must be locked,
			 * as it can be accessed by multiple crawler child threads at the same time.
			 */
			
			crawler.LockchildThreads.writeLock().lock();
			
			try {
				
				crawler.childThreads--;
				
			} finally {
				
				crawler.LockchildThreads.writeLock().unlock();
				
			}
			
			
		} catch (IOException ie) {
			
			System.out.println("Crawler child Thread unable to close connections...");
			//ie.printStackTrace();
			
		} 
		
	}

}
