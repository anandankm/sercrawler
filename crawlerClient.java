import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class crawlerClient extends Thread {

	private Socket connectionSocket = null;
	
	private PrintWriter out = null;
	
	private BufferedReader in = null;
	
	private String serviceName = null; 
	
	public crawlerClient (Socket cxnSocket, String service) {
		
		this.connectionSocket = cxnSocket;
		
		this.serviceName = service;
		
	}
	
	public void run() {
		
		Repository Rp = new Repository();
		
		boolean ProtSeqError = false;
		
		long start = 0;
		
		long stop = 0;
		
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
			
		} catch (IOException ie) {
			
			System.out.println("out, in not created");
		
			//ie.printStackTrace();
		
		} 

		
		/*
		 *  crawlerClient says 'Hello'...
		 */
		
		outputLine = "HELLO\n";
		
		out.print(outputLine);
		
		out.flush();
		
		try {
			
			inputLine = in.readLine();
			
		} catch (IOException ie) {
			
			System.out.println("HI from broker not read");
			
			//ie.printStackTrace();
			
		}
		
		if (inputLine.equalsIgnoreCase("HI")) {
			
			System.out.println("Broker says 'Hi'...");
			
		} else {
			
			System.out.println("Input from Broker does not equal 'Hi'");
			
			System.out.println("Broker says Protocol Sequence Error");
		
			ProtSeqError = true;  
			
		}
		/*
		 * If the ProtSeqError is set true, the broker did not receive "HELLO" and it responded with "ERROR 0x002"
		 * and is going to close connection.
		 */
		if (!ProtSeqError) {
			
			while (!crawler.CrawlerClientEnd) {
				
				outputLine = "QUERY " + Rp.RandomAlphaNum() + " " + this.serviceName + " *\n";
				
				try {
					stop = System.currentTimeMillis();
					
					if ((stop - start) < crawler.CrawlerPeriodicity) {
						
						Thread.sleep(crawler.CrawlerPeriodicity - stop + start);
						
					} 
					
					if (crawler.CrawlerClientEnd)	
						
						break;
					
				} catch (InterruptedException ie) {
					
					System.out.println("Interrupted at " + this.serviceName + " crawler client Thread");
					
					//ie.printStackTrace();
					
				}
					
				try {
					
					start = System.currentTimeMillis(); 
					
					out.print(outputLine);
					
					out.flush();
					
				} catch (Exception e) {
					
					System.out.println("could not write output string to broker; exiting the while loop gracefully...");
					
					break;
					
				} 
				
				/*
				 * Read the response from the server, for the above sent message. 
				 */
				
				try {
				
					String str = in.readLine();
					
					Rp.ProcessReadLine(str);
					
				} catch (IOException ie) {
					
					//ie.printStackTrace();
					
					System.out.println("could not read string from broker; exiting the while loop gracefully...");
					
					break;
					
				} 
				
				/*
				 * The crawler client received STATUS from the broker
				 */
				
				if (Rp.CrawlerClientReceivedStatus) {
					
					if (Rp.ReturnedStatus) {
						
						System.out.println("Broker Response: Success");
						
					} else {
						
						System.out.println("Broker Response: Malformed Request");
						
						Rp.ReturnedStatus = true;
						
					}
					
					Rp.CrawlerClientReceivedStatus = false;
				}
				
				/*
				 * The crawler client received ERROR from the server
				 */
				
				if (Rp.CrawlerClientReceivedError) {
					
					if (Rp.ReceivedErrorCode.equalsIgnoreCase("0x002")) {
						
						Rp.CrawlerClientReceivedQueryReply = 0;
					}
					
					Rp.CrawlerClientReceivedError = false;
				}
				
				/*
				 * The client received QUERY-REPLY from the server.
				 */

				
				if (Rp.CrawlerClientReceivedQueryReply >= 0) {
					
					
					if (Rp.CrawlerClientReceivedQueryReply == 0) {
						
						if (this.serviceName == "cube-root")  {
							
							crawler.LockCubeRootHosts.writeLock().lock();
								
								try {
									
									if (!crawler.CubeRootHosts.isEmpty()) {
										/*
										 * Clear Cube Root Database.
										 */
										crawler.CubeRootHosts.clear();
										
										crawler.LockCubeRootSA.writeLock().lock();
										
										try {
											
											crawler.CubeRootSA.clear();
											
										} finally {
											
											crawler.LockCubeRootSA.writeLock().unlock();
											
										}
										
									}
											 	
								} finally {
									
									crawler.LockCubeRootHosts.writeLock().unlock();
									
								} 
								
						}
						
						if (this.serviceName == "square-root")  {
							
							crawler.LockSquareRootHosts.writeLock().lock();
								
								try {
									
									if (!crawler.SquareRootHosts.isEmpty()) {
										/*
										 * Clear Square Root Database.
										 */
										crawler.SquareRootHosts.clear();
										
										crawler.LockSquareRootSA.writeLock().lock();
										
										try {
											
											crawler.SquareRootSA.clear();
											
										} finally {
											
											crawler.LockSquareRootSA.writeLock().unlock();
											
										}
										
									}
											 	
								} finally {
									
									crawler.LockSquareRootHosts.writeLock().unlock();
									
								} 
								
						}
						
						if (this.serviceName == "sha-digest")  {
							
							crawler.LockSHADigestHosts.writeLock().lock();
								
								try {
									
									if (!crawler.SHADigestHosts.isEmpty()) {
										/*
										 * Clear SHA Digest Database.
										 */
										crawler.SHADigestHosts.clear();
										
										crawler.LockSHADigestSA.writeLock().lock();
										
										try {
											
											crawler.SHADigestSA.clear();
											
										} finally {
											
											crawler.LockSHADigestSA.writeLock().unlock();
											
										}
										
									}
											 	
								} finally {
									
									crawler.LockSHADigestHosts.writeLock().unlock();
									
								} 
								
						}
						
						if (this.serviceName == "golden-ratio")  {
							
							crawler.LockGoldenRatioHosts.writeLock().lock();
								
								try {
									
									if (!crawler.GoldenRatioHosts.isEmpty()) {
										/*
										 * Clear Golden Ratio Database
										 */
										crawler.GoldenRatioHosts.clear();
										
										crawler.LockGoldenRatioSA.writeLock().lock();
										
										try {
											
											crawler.GoldenRatioSA.clear();
											
										} finally {
											
											crawler.LockGoldenRatioSA.writeLock().unlock();
											
										}
										
									}
											 	
								} finally {
									
									crawler.LockGoldenRatioHosts.writeLock().unlock();
									
								} 
								
						}
						
						if (this.serviceName == "server-time")  {
							
							crawler.LockServerTimeHosts.writeLock().lock();
								
								try {
									
									if (!crawler.ServerTimeHosts.isEmpty()) {
										/*
										 * Clear Server Time Database
										 */
										crawler.ServerTimeHosts.clear();
										
									}
											 	
								} finally {
									
									crawler.LockServerTimeHosts.writeLock().unlock();
									
								} 
								
						}
						
					}
					
					for (int j = 0; j < Rp.CrawlerClientReceivedQueryReply; j++) {
						/*
						 * Expect ReceivedQueryReply number of RESPONSES from the server.
						 */
						try {
							String str1 = in.readLine();
							
							
							Rp.ProcessReadLine(str1);
							
						} catch (IOException ie) {
							
							//ie.printStackTrace();
							
							System.out.println("could not read string from broker; exiting the for loop gracefully...");
							
							break;
							
						}
					}
					
					if (Rp.CrawlerClientReceivedQueryReply > 0) {
						
						if (this.serviceName == "cube-root")  {
							
							crawler.LockCubeRootHosts.writeLock().lock();
								
								try {
									
									if (!crawler.CubeRootHosts.isEmpty()) {
										/*
										 * Check for Synchronized Database.
										 */
										ArrayList<Integer> BufferIndex = new ArrayList<Integer>();
										
										for (int i = 0; i < crawler.CubeRootHosts.size(); i++) {
											
											if (!Rp.CubeRootProviders.contains(crawler.CubeRootHosts.get(i))) {
												
												BufferIndex.add(i);
												
											}
											
										}
										
										for (int j = 0; j < BufferIndex.size(); j++) {
											
											if (!Rp.CubeRootProviders.contains(crawler.CubeRootHosts.get(j))) {
												
												crawler.CubeRootHosts.remove(BufferIndex.get(j));
												
												crawler.LockCubeRootSA.writeLock().lock();
												
												try {
													
													crawler.CubeRootSA.remove(BufferIndex.get(j));
													
												} finally {
													
													crawler.LockCubeRootSA.writeLock().unlock();
													
												}
												
											}
											
										}
										
										Rp.CubeRootProviders.clear();
									}
											 	
								} finally {
									
									crawler.LockCubeRootHosts.writeLock().unlock();
									
								} 
								
						}
						
						if (this.serviceName == "square-root")  {
							
							crawler.LockSquareRootHosts.writeLock().lock();
								
								try {
									
									if (!crawler.SquareRootHosts.isEmpty()) {
										/*
										 * Check for Synchronized Database.
										 */
										ArrayList<Integer> BufferIndex = new ArrayList<Integer>();
										
										for (int i = 0; i < crawler.SquareRootHosts.size(); i++) {
											
											if (!Rp.SquareRootProviders.contains(crawler.SquareRootHosts.get(i))) {
												
												BufferIndex.add(i);
												
											}
											
										}
										
										for (int j = 0; j < BufferIndex.size(); j++) {
											
											if (!Rp.SquareRootProviders.contains(crawler.SquareRootHosts.get(j))) {
												
												crawler.SquareRootHosts.remove(BufferIndex.get(j));
												
												crawler.LockSquareRootSA.writeLock().lock();
												
												try {
													
													crawler.SquareRootSA.remove(BufferIndex.get(j));
													
												} finally {
													
													crawler.LockSquareRootSA.writeLock().unlock();
													
												}
												
											}
											
										}
										
										Rp.SquareRootProviders.clear();
										
									}
											 	
								} finally {
									
									crawler.LockSquareRootHosts.writeLock().unlock();
									
								} 
								
						}
						
						if (this.serviceName == "sha-digest")  {
							
							crawler.LockSHADigestHosts.writeLock().lock();
								
								try {
									
									if (!crawler.SHADigestHosts.isEmpty()) {
										
										/*
										 * Check for Synchronized Database.
										 */
										
										ArrayList<Integer> BufferIndex = new ArrayList<Integer>();
										
										for (int i = 0; i < crawler.SHADigestHosts.size(); i++) {
											
											if (!Rp.SHADigestProviders.contains(crawler.SHADigestHosts.get(i))) {
												
												BufferIndex.add(i);
												
											}
											
										}
										
										for (int j = 0; j < BufferIndex.size(); j++) {
											
											if (!Rp.SHADigestProviders.contains(crawler.SHADigestHosts.get(j))) {
												
												crawler.SHADigestHosts.remove(BufferIndex.get(j));
												
												crawler.LockSHADigestSA.writeLock().lock();
												
												try {
													
													crawler.SHADigestSA.remove(BufferIndex.get(j));
													
												} finally {
													
													crawler.LockSHADigestSA.writeLock().unlock();
													
												}
												
											}
											
										}
										
										Rp.SHADigestProviders.clear();
										
									}
											 	
								} finally {
									
									crawler.LockSHADigestHosts.writeLock().unlock();
									
								} 
								
						}
						
						if (this.serviceName == "golden-ratio")  {
							
							crawler.LockGoldenRatioHosts.writeLock().lock();
								
								try {
									
									if (!crawler.GoldenRatioHosts.isEmpty()) {

										/*
										 * Check for Synchronized Database.
										 */
										
										ArrayList<Integer> BufferIndex = new ArrayList<Integer>();
										
										for (int i = 0; i < crawler.GoldenRatioHosts.size(); i++) {
											
											if (!Rp.GoldenRatioProviders.contains(crawler.GoldenRatioHosts.get(i))) {
												
												BufferIndex.add(i);
												
											}
											
										}
										
										for (int j = 0; j < BufferIndex.size(); j++) {
											
											if (!Rp.GoldenRatioProviders.contains(crawler.GoldenRatioHosts.get(j))) {
												
												crawler.GoldenRatioHosts.remove(BufferIndex.get(j));
												
												crawler.LockGoldenRatioSA.writeLock().lock();
												
												try {
													
													crawler.GoldenRatioSA.remove(BufferIndex.get(j));
													
												} finally {
													
													crawler.LockGoldenRatioSA.writeLock().unlock();
													
												}
												
											}
											
										}
										
										Rp.GoldenRatioProviders.clear();
										
										
									}
											 	
								} finally {
									
									crawler.LockGoldenRatioHosts.writeLock().unlock();
									
								} 
								
						}
						
						if (this.serviceName == "server-time")  {
							
							crawler.LockServerTimeHosts.writeLock().lock();
								
								try {
									
									if (!crawler.ServerTimeHosts.isEmpty()) {
										/*
										 * Check for Synchronized Database.
										 */
										
										ArrayList<Integer> BufferIndex = new ArrayList<Integer>();
										
										for (int i = 0; i < crawler.ServerTimeHosts.size(); i++) {
											
											if (!Rp.ServerTimeProviders.contains(crawler.ServerTimeHosts.get(i))) {
												
												BufferIndex.add(i);
												
											}
											
										}
										
										for (int j = 0; j < BufferIndex.size(); j++) {
											
											if (!Rp.ServerTimeProviders.contains(crawler.ServerTimeHosts.get(j))) {
												
												crawler.ServerTimeHosts.remove(BufferIndex.get(j));
												
											}
											
										}
										
										Rp.ServerTimeProviders.clear();
										
									}
											 	
								} finally {
									
									crawler.LockServerTimeHosts.writeLock().unlock();
									
								} 
								
						}
						
					}
					
					Rp.CrawlerClientReceivedQueryReply = -1;
					
				}

			}
			
			outputLine = "BYE\n";
			
			try {
				
				out.print(outputLine);
			
				out.flush();
			
			} catch (Exception e) {
			
				System.out.println("could not write 'BYE\n' string to broker; exiting gracefully...");
			
			}
			
		}
		/*
		 * Just read the "LATER\n" message from the server and close the connection.
		 */
		try {
			
			Rp.ProcessReadLine(in.readLine());
			
		} catch (IOException ie) {
			
			//ie.printStackTrace();
			
			System.out.println("could not read 'LATER\n' string from broker; exiting gracefully...");
			
		}
		
		try {
			
			in.close();
		
			out.close();
			
			connectionSocket.close();
			
		} catch (IOException ie) {
			
			//ie.printStackTrace();
			
			System.out.println("could not close crawlerclient Socket connected to broker; exiting gracefully...");
			
			System.exit(1);
			
		}
		
		crawler.LockcrawlerClientThreads.writeLock().lock();
		
		try {
			
			crawler.crawlerClientThreads--;
			
		} finally {
			
			crawler.LockcrawlerClientThreads.writeLock().unlock();
			
		}
		
		System.out.println("CrawlerClient Closed... Done!");
		
	
	}
	
}
