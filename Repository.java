/*
 *
 * -------------------
 * Random alphanumeric generator help:
 * 
 * http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string-in-java
 * 
 * -------------------
*/
import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.StringTokenizer;

public class Repository {
	/*
	 * *************Crawler Client Members*************
	 */
	public boolean CrawlerClientReceivedError = false;		// Set true if client receives ERROR protocol from server
	
	public boolean CrawlerClientReceivedStatus = false;		// Set true if client receives STATUS protocol from server
	
	public int CrawlerClientReceivedQueryReply = -1;			// Set to number of RESPONSES [0 or more], client will get from server
	
	public boolean CrawlerClientProtSeqError = false;			// Set true if client does not receive "HI\n" from server
	
	/*
	 * ************************************************
	 */
	
	public ArrayList<String> CubeRootProviders = new ArrayList<String>();
	
	public ArrayList<String> SquareRootProviders = new ArrayList<String>();
	
	public ArrayList<String> SHADigestProviders = new ArrayList<String>();
	
	public ArrayList<String> GoldenRatioProviders = new ArrayList<String>();
	
	public ArrayList<String> ServerTimeProviders = new ArrayList<String>();
	
	public boolean serverAccess = false;
	
	private BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
	
	public boolean Status = true;							// server can send STATUS as true
	
	public boolean ReturnedStatus = true;					// client received some status message
	
	public ArrayList<String[]> ScriptBuffer = new ArrayList<String[]>();
	
	public ArrayList<String> Services = new ArrayList<String>();
	
	public LinkedHashMap<String,String> ServiceBuffer = new LinkedHashMap<String,String>();
	
	public LinkedHashMap<String,String> TransFirstTriple = new LinkedHashMap<String,String>();
	
	private StringTokenizer token = null;
	
	public String[] lineArray = null;
	
	public String ErrorCode = null;
	
	public String ReceivedErrorCode = null;
	
	public boolean sendStatus = false;			// Set true if STATUS message need to be sent to client
	
	public boolean sendError = false;			// Set true if ERROR message need to be sent to client
	
	public boolean sendQueryReply = false;		// Set true if QUERY-REPLY message need to be sent to client
	
	public boolean sendOK = false;				// Set true if OK message need to be sent to client
	
	public boolean ReceivedBye = false;			// Set true if BYE message read from client

	//private BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
	/*
	 * Random Alpha numeric QUERY ID generated to send to the server.
	 * Please note the website reference at the top of this source code.
	 */
	public String RandomAlphaNum() {
		
		char[] ANChar = new char[8];
		
		Random rand = new Random();
		
		char[] alphaNumChar = new char[36];
		
		for(int i = 0; i < 10; i++) {
			
			alphaNumChar[i] = (char)('0' + i);
			
		}
		
		for(int i = 10; i < 36; i++) {
			
			alphaNumChar[i] = (char)('a' + i - 10);
			
		}
		
		for(int i = 0; i < 8; i++) {
			
			ANChar[i] = alphaNumChar[rand.nextInt(36)];
			 
		}
		
		return new String(ANChar);
		
	}
	/*
	 * Process the input line either for server or for client.
	 */
	public void ProcessReadLine(String inputLine) {
		
		token = new StringTokenizer(inputLine);
		
		int tcount = token.countTokens();
		
		if ( tcount <= 0) {
			 
			System.out.println("Empty line received");
			
			sendStatus = true;
			
			Status =  false;			
			
			return;
		 }
		
		String token1 = token.nextToken();
		/*
		 * Crawler Client reads STATUS message 
		 * and decides what to do next...
		 */
		if ( token1.equalsIgnoreCase("STATUS") ) {
			
			CrawlerClientReceivedStatus = true;

			if (serverAccess) {
				
				server.ReceivedStatus = true;
				
			}
			
			if (token.hasMoreTokens()) {
				
				String stat = token.nextToken();
				
				if (stat.equalsIgnoreCase("TRUE")) {
					
					ReturnedStatus = true;
					
				} else if (stat.equalsIgnoreCase("FALSE")) {
					
					ReturnedStatus = false;
					
				}
			} 
			
			return;
		}
		/*
		 * Crawler Client reads ERROR message 
		 * and decides what to do next...
		 */
		
		if ( token1.equalsIgnoreCase("ERROR")) {
			
			CrawlerClientReceivedError = true;
			
			if (token.hasMoreTokens()) {
				
				ReceivedErrorCode = token.nextToken();
				
			} 
			
			if (serverAccess) {
				
				server.ReceivedError = true;
				
			}
			
			return;
			
		}
		
		/*
		 * Broker reads REGISTER or REMOVE message 
		 * and decides what to do next...
		 */
		
		if ((token1.equalsIgnoreCase("REGISTER")) || (token1.equalsIgnoreCase("REMOVE"))) {
			
			sendStatus = true;
			
			lineArray = new String[3];
			
			for (int i = 0; i < 3; i++) {
				
				if (token.hasMoreTokens()) {
					lineArray[i] = token.nextToken();
				} else {
					/*
					 * The message is formed with inadequate protocol values
					 */
					sendError = true;
					
					sendStatus = false;
					
					ErrorCode = "0x003";
					
					return;
				}
				
			}
				
			if (token1.equalsIgnoreCase("REGISTER")) {
			/*
			 * Need to lock the shared resource RegisterBuffer for writing
			 */
				broker.LockRegister.writeLock().lock();
				
				try {
					
					if (!broker.RegisterBuffer.isEmpty()) {
						
						for (int i = 0; i < broker.RegisterBuffer.size(); i++) {
							/*
							 *  Code to have unique Register triples
							*/
							if ((broker.RegisterBuffer.get(i)[0].equals(lineArray[0])) && 
								(broker.RegisterBuffer.get(i)[1].equals(lineArray[1])) &&
								(broker.RegisterBuffer.get(i)[2].equals(lineArray[2]))) {		
								/*
								 * Both the incoming and one of the already existing triples are same.
								 * Just leaving the triple as it is... this is same as overwriting 
								 * the existing triple with the same triple...
								 * 
								 */
								
								return;
								
							}
							if ((broker.RegisterBuffer.get(i)[0].equals(lineArray[0])) && 
								(broker.RegisterBuffer.get(i)[2].equals(lineArray[2]))) {		
								/*
								 * Same service from same service provider but with different version number.
								 * So, update the version number alone.
								 */
								
								broker.RegisterBuffer.get(i)[1] = lineArray[1];
								
								return;
								
							}
						}
					}

					broker.RegisterBuffer.add(lineArray);
					
				} finally {
					
					broker.LockRegister.writeLock().unlock();
					
				}
				
				
			}
		
			if (token1.equalsIgnoreCase("REMOVE")) {
				
				broker.LockRegister.writeLock().lock();
				
				try {
					
					if (broker.RegisterBuffer.isEmpty()) {
						/*
						 * Protocol Sequence Error - 0x002
						*/
						sendError = true;
						
						sendStatus = false;
						
						ErrorCode = "0x002";
						
						return;
						
					} else {
						
						
						boolean temp = false;
						
						for (int i = 0; i < broker.RegisterBuffer.size(); i++) {
							
							if ((broker.RegisterBuffer.get(i)[0].equals(lineArray[0])) && 
								(broker.RegisterBuffer.get(i)[1].equals(lineArray[1])) &&
								(broker.RegisterBuffer.get(i)[2].equals(lineArray[2]))) {
								/*
								 * If the given triple is present in the database,
								 * then remove it.
								 */
								broker.RegisterBuffer.remove(i);
								
								temp = true;
							
							}
						}
						
						if (!temp) {
							/*
							 * If the given triple is not present in the database,
							 * then send STATUS FALSE\n message to server.
							 */
							Status = false;
							
							return;
							
						}
						
					}
			
				} finally {
					
					broker.LockRegister.writeLock().unlock();
					
				}
					
							
			}
				
		}
		
		
		/*
		 * crawler child thread reads QUERY message from a client
		 * and decides what to do next...
		 */
		
		if (token1.equalsIgnoreCase("QUERY")) {
			
			lineArray = new String[3];
			
			for (int i = 0; i < 3; i++) {
				
				if (token.hasMoreTokens()) {
					
					lineArray[i] = token.nextToken();
					
				} else {
					/*
					 * The message from client is formed with inadequate protocol values
					 * Correct Example :QUERY id value1 value2\n
					 * where,
					 *  id is the Transaction id
					 *  value1 is the first triple value
					 *  value2 can be second triple value or "*" 
					 */
					sendError = true;
					
					ErrorCode = "0x003";
					
					return;
				}
				
			}
			
			sendQueryReply = true;	
			
		}
		/*
		 * Crawler Client reads QUERY-REPLY message 
		 * and decides what to do next...
		 */
		if (token1.equalsIgnoreCase("QUERY-REPLY")) {
			
			if (token.countTokens() == 2) {
				
				String TransId = token.nextToken();
				
				if (serverAccess) {
					
					server.ReceivedQueryReply = Integer.parseInt(token.nextToken());
					
				} else {
					
					CrawlerClientReceivedQueryReply = Integer.parseInt(token.nextToken());
					
					
				}
			}
			
		}
		/*
		 * Crawler Client reads RESPONSE message 
		 * and displays the RESPONSES
		 */
		if (token1.equalsIgnoreCase("RESPONSE")) {
			
			if (token.countTokens() == 4) {
				
				String TransId = token.nextToken();
				
				String num = token.nextToken();
				
				String serviceName = token.nextToken();
				
				String serviceProvider = token.nextToken();
				//System.out.println(serviceProvider + " " + serviceName + " " + num);
				if (serverAccess) {
					
					System.out.println("\t" + num + ". " + TransFirstTriple.get(TransId) + " " + serviceName + " " + serviceProvider);
					
					return;
					
				}
				
				/*
				 * Add service provider Host IP to crawler's Cube Root database.
				 */
				if (serviceName.equalsIgnoreCase("cube-root")) {
					
					CubeRootProviders.add(serviceProvider);
					
					crawler.LockCubeRootHosts.writeLock().lock();
					
					try {
						
						if (!crawler.CubeRootHosts.contains(serviceProvider)) {
							
							crawler.CubeRootHosts.add(serviceProvider);
							/*
							 * Add service agreement of Cube_Root interface 
							 * to crawler's Cube Root database.
							 */
							CubeRootAddSA(serviceProvider);
							
						}
						
					} catch (Exception e) {
						
						System.out.println("Service Provider not added at cube root response");
							
							//e.printStackTrace();
							 	
					} finally {
						
						crawler.LockCubeRootHosts.writeLock().unlock();
						
					} 
					
				}
				/*
				 * Add service provider Host IP to crawler's Square Root database.
				 */
				if (serviceName.equalsIgnoreCase("square-root")) {
					
					SquareRootProviders.add(serviceProvider);
					
					crawler.LockSquareRootHosts.writeLock().lock();
					
					try {
						
						if (!crawler.SquareRootHosts.contains(serviceProvider)) {
							
							crawler.SquareRootHosts.add(serviceProvider);
							/*
							 * Add service agreement of Square_Root interface 
							 * to crawler's Square Root database.
							 */
							SquareRootAddSA(serviceProvider);
							
						}
						
					} catch (Exception e) {
						System.out.println("Service Provider not added at square root response");
							//e.printStackTrace();
							 	
					} finally {
						
						crawler.LockSquareRootHosts.writeLock().unlock();
						
					} 
					
				}
				/*
				 * Add service provider Host IP to crawler's SHA Digest database.
				 */
				if (serviceName.equalsIgnoreCase("sha-digest")) {
					
					SHADigestProviders.add(serviceProvider);
					
					crawler.LockSHADigestHosts.writeLock().lock();
					
					try {
						
						if (!crawler.SHADigestHosts.contains(serviceProvider)) {
							
							crawler.SHADigestHosts.add(serviceProvider);
							/*
							 * Add service agreement of SHA_Digest interface 
							 * to crawler's SHA Digest database.
							 */
							SHADigestAddSA(serviceProvider);
							
						}
						
					} catch (Exception e) {
						System.out.println("Service Provider not added at sha digest response");
							//e.printStackTrace();
							 	
					} finally {
						
						crawler.LockSHADigestHosts.writeLock().unlock();
						
					} 
					
				}
				/*
				 * Add service provider Host IP to crawler's Golden ratio database.
				 */
				if (serviceName.equalsIgnoreCase("golden-ratio")) {
					
					GoldenRatioProviders.add(serviceProvider);
					
					crawler.LockGoldenRatioHosts.writeLock().lock();
					
					try {
						
						if (!crawler.GoldenRatioHosts.contains(serviceProvider)) {
							
							crawler.GoldenRatioHosts.add(serviceProvider);
							/*
							 * Add service agreement of Golden_Ratio interface 
							 * to crawler's Golden ratio database.
							 */
							GoldenRatioAddSA(serviceProvider);
							
						}
						
					} catch (Exception e) {
						System.out.println("Service Provider not added at golden-ratio response");
							//e.printStackTrace();
							 	
					} finally {
						
						crawler.LockGoldenRatioHosts.writeLock().unlock();
						
					} 
					
				}
				/*
				 * Just add servide provider Host IP to crawler's server time database.
				 * No service agreement for Server_Time interface.
				 */
				if (serviceName.equalsIgnoreCase("server-time")) {
					
					ServerTimeProviders.add(serviceProvider);
					
					crawler.LockServerTimeHosts.writeLock().lock();
					
					try {
						
						if (!crawler.ServerTimeHosts.contains(serviceProvider)) {
							
							crawler.ServerTimeHosts.add(serviceProvider);
							
						}
						
					} catch (Exception e) {
						
						System.out.println("Service Provider not added at server time response");
							
							//e.printStackTrace();
							 	
					} finally {
						
						crawler.LockServerTimeHosts.writeLock().unlock();
						
					} 
					
				}
				
			
			}
			
		}
		/*
		 * crawler receives BYE message
		 */
		if (token1.equalsIgnoreCase("BYE")) {
			
			ReceivedBye = true;
			
		}
		/*
		 * Client receives LATER message
		 */
		
		if (token1.equalsIgnoreCase("LATER")) {
			
			System.out.println("Terminating Connection with broker...");
			
		}

		/*
		 * crawler receives SHUTDOWN message
		 */
		
		if (token1.equalsIgnoreCase("SHUTDOWN")) {
			
			crawler.LockShutdown.writeLock().lock();
			
			try {
				
				if (!crawler.Shutdown) {
					
					crawler.Shutdown = true;
					
					System.out.println("SHUTDOWN listen...");
					
					/*
					 * crawler needs to stop listening
					 */
	
					crawler.welcomeSocket.close();
							
				}
				
			} catch (IOException ie) {
					
				System.out.println("crawler welcomesocket not closed");
					//ie.printStackTrace();
					 	
			} finally {
				
				crawler.LockShutdown.writeLock().unlock();
				
			} 
			
			sendOK = true;
			
		}
		/*
		 * Client receives OK message acknowledging that the crawler has stopped listening.
		 */
		if (token1.equalsIgnoreCase("OK")) {
			
			System.out.println("crawler has SHUTDOWN Listening...");
			
		}
		
		
	}
	/*
	 * 1. Add the service agreement string to the crawler database for Cube_Root Interface 
	 */
	private void CubeRootAddSA(String serviceProvider) {
		try {
			
			Registry registry = LocateRegistry.getRegistry(serviceProvider);
			
			Cube_Root servicestub = (Cube_Root) registry.lookup("Cube_Root");
			
			crawler.LockCubeRootSA.writeLock().lock();
			
			try {
				
				crawler.CubeRootSA.add(servicestub.serviceAgreement());
				
			} catch (Exception e) {
					
				System.out.println("Service Provider SA not added at cube root response");
					//e.printStackTrace();
					 	
			} finally {
				
				crawler.LockCubeRootSA.writeLock().unlock();
				
			}
			
			
		} catch (Exception e) {
            
        	System.err.println("CrawlerClient exception: --> Couldn't find service name: " 
        			 + "Cube_Root in the Service Provider: " + serviceProvider);
            
        	//e.printStackTrace();
        }
		
	}
	
	/*
	 * 2. Add the service agreement string to the crawler database for Square_Root Interface 
	 */
	private void SquareRootAddSA(String serviceProvider) {
		try {
			
			Registry registry = LocateRegistry.getRegistry(serviceProvider);
			
			Square_Root servicestub = (Square_Root) registry.lookup("Square_Root");
			
			crawler.LockSquareRootSA.writeLock().lock();
			
			try {
				
				crawler.SquareRootSA.add(servicestub.serviceAgreement());
				
			} catch (Exception e) {
					
				System.out.println("Service Provider SA not added at square root response");
					//e.printStackTrace();
					 	
			} finally {
				
				crawler.LockSquareRootSA.writeLock().unlock();
				
			}
			
			
		} catch (Exception e) {
            
        	System.err.println("CrawlerClient exception: --> Couldn't find service name: " 
        			 + "Cube_Root in the Service Provider: " + serviceProvider);
            
        	//e.printStackTrace();
        }
		
	}
	
	/*
	 * 3. Add the service agreement string to the crawler database for SHA_Digest Interface 
	 */
	private void SHADigestAddSA(String serviceProvider) {
		try {
			
			Registry registry = LocateRegistry.getRegistry(serviceProvider);
			
			SHA_Digest servicestub = (SHA_Digest) registry.lookup("SHA_Digest");
			
			crawler.LockSHADigestSA.writeLock().lock();
			
			try {
				
				crawler.SHADigestSA.add(servicestub.serviceAgreement());
				
			} catch (Exception e) {
				System.out.println("Service Provider SA not added at sha digest response");
					//e.printStackTrace();
					 	
			} finally {
				
				crawler.LockSHADigestSA.writeLock().unlock();
				
			}
			
			
		} catch (Exception e) {
            
        	System.err.println("CrawlerClient exception: --> Couldn't find service name: " 
        			 + "Cube_Root in the Service Provider: " + serviceProvider);
            
        	//e.printStackTrace();
        }
		
	}
	
	/*
	 * 4. Add the service agreement string to the crawler database for Golden_Ratio Interface 
	 */
	private void GoldenRatioAddSA(String serviceProvider) {
		
		try {
			
			Registry registry = LocateRegistry.getRegistry(serviceProvider);
			
			Golden_Ratio servicestub = (Golden_Ratio) registry.lookup("Golden_Ratio");
			
			crawler.LockGoldenRatioSA.writeLock().lock();
			
			try {
				
				crawler.GoldenRatioSA.add(servicestub.serviceAgreement());
				
			} catch (Exception e) {
					
				System.out.println("Service Provider SA not added at golden ratio response");
					//e.printStackTrace();
					 	
			} finally {
				
				crawler.LockGoldenRatioSA.writeLock().unlock();
				
			}
			
			
		} catch (Exception e) {
            
        	System.err.println("CrawlerClient exception: --> Couldn't find service name: " 
        			 + "Cube_Root in the Service Provider: " + serviceProvider);
            
        	//e.printStackTrace();
        }
		
	}
	
	/*
	 * To check whether the requested registration of service
	 * falls into one of these services.
	 */
	private String checkService(String service, int lineNum){
		
		if (service.equalsIgnoreCase("cube-root")) {
			
			ScriptBuffer.get(lineNum)[2] = "Cube_Root";
			
			return "cube-root";
			
		} else if (service.equalsIgnoreCase("square-root")) {
			
			ScriptBuffer.get(lineNum)[2] = "Square_Root";
			
			return "square-root";
			
		} else if (service.equalsIgnoreCase("golden-ratio")) {
			
			ScriptBuffer.get(lineNum)[2] = "Golden_Ratio";
			
			return "golden-ratio";
			
		} else if (service.equalsIgnoreCase("sha-digest")) {
			
			ScriptBuffer.get(lineNum)[2] = "SHA_Digest";
			
			return "sha-digest";
			
		} else if (service.equalsIgnoreCase("server-time")) {
			
			ScriptBuffer.get(lineNum)[2] = "Server_Time";
			
			return "server-time";
			
		} else {
			
			return null;
			
		}
		
	}
	
	/*
	 * 1. The service provider binds the registered server time service with its rmi registry.
	 */
	
	private void bindServerTime() {
		try {
            
			Server_Time BindObject = new ServerTime();
            
			Server_Time stub =
                (Server_Time) UnicastRemoteObject.exportObject(BindObject, 0);
            
			Registry registry = LocateRegistry.getRegistry();

			registry.rebind("Server_Time", stub);
            
			System.out.println("Server_Time Object bound");
			
        } catch (Exception e) {
            
        	System.err.println("Server RMI exception:->  Server_Time Object not bound");
            
        	//e.printStackTrace();
        	
        }
	}
	
	/*
	 * 2. The service provider binds the registered cube root service with its rmi registry.
	 */
	
	private void bindCubeRoot() {
		try {
            
			Cube_Root BindObject = new CubeRoot();
            
			Cube_Root stub =
                (Cube_Root) UnicastRemoteObject.exportObject(BindObject, 0);
            
			Registry registry = LocateRegistry.getRegistry();

			registry.rebind("Cube_Root", stub);
            
			System.out.println("Cube_Root Object bound");
			
        } catch (Exception e) {
            
        	System.err.println("Server RMI exception:->  Cube_Root Object not bound");
            
        	//e.printStackTrace();
        	
        }
	}
	
	/*
	 * 3. The service provider binds the registered square root service with its rmi registry.
	 */
	
	private void bindSquareRoot() {
		try {
            
			Square_Root BindObject = new SquareRoot();
            
			Square_Root stub =
                (Square_Root) UnicastRemoteObject.exportObject(BindObject, 0);
            
			Registry registry = LocateRegistry.getRegistry();

			registry.rebind("Square_Root", stub);
            
			System.out.println("Square_Root Object bound");
			
        } catch (Exception e) {
            
        	System.err.println("Server RMI exception:->  Square_Root Object not bound");
            
        	//e.printStackTrace();
        	
        }
	}
	/*
	 * 4. The service provider binds the registered square root service with its rmi registry.
	 */
	
	private void bindSHADigest() {
		try {
            
			SHA_Digest BindObject = new shaDigest();
            
			SHA_Digest stub =
                (SHA_Digest) UnicastRemoteObject.exportObject(BindObject, 0);
            
			Registry registry = LocateRegistry.getRegistry();

			registry.rebind("SHA_Digest", stub);
            
			System.out.println("SHA_Digest Object bound");
			
        } catch (Exception e) {
            
        	System.err.println("Server RMI exception:->  SHA_Digest Object not bound");
            
        	//e.printStackTrace();
        	
        }
	}
	
	/*
	 * 5. The service provider binds the registered Golden Ratio service with its rmi registry.
	 */
	
	private void bindGoldenRatio() {
		try {
            
			Golden_Ratio BindObject = new GoldenRatio();
            
			Golden_Ratio stub =
                (Golden_Ratio) UnicastRemoteObject.exportObject(BindObject, 0);
            
			Registry registry = LocateRegistry.getRegistry();

			registry.rebind("Golden_Ratio", stub);
            
			System.out.println("Golden_Ratio Object bound");
			
        } catch (Exception e) {
            
        	System.err.println("Server RMI exception:->  Golden_Ratio Object not bound");
            
        	//e.printStackTrace();
        	
        }
	}
	
	
	/*
	 * Register service to the service provider's rmi registry.
	 */
	private void addToRMI(String service) {
		
		if (service.equalsIgnoreCase("cube-root")) {
			
			bindCubeRoot();
			
		} else if (service.equalsIgnoreCase("square-root")) {
			
			bindSquareRoot();
			
		} else if (service.equalsIgnoreCase("sha-digest")) {
			
			bindSHADigest();
			
		} else if (service.equalsIgnoreCase("golden-ratio")) {
			
			bindGoldenRatio();
			
		} else if (service.equalsIgnoreCase("server-time")) {
			
			bindServerTime();
			
		} else {
			
			return ;
			
		}
	}
	
	/*
	 * Server Side Output Message Formation
	 * The message is formed by sending the scriptbuffer list index [--> here variable 'lineNum']
	*/
	
	public String ProcScriptBuffer(int lineNum) {
		
		StringBuffer Line = new StringBuffer();
		
		int size = ScriptBuffer.get(lineNum).length; 
		
		if (size == 5) {
			
			Line.append(ScriptBuffer.get(lineNum)[0]);
			
			Line.append("\n");
			
		}
		
		
		if ((size == 3) || (size == 4)) {
			
			if (size == 3) {
				
				Line.append("REGISTER");
				
				if ( (ScriptBuffer.get(lineNum)[0] != null)) {
					
					String service = checkService(ScriptBuffer.get(lineNum)[0], lineNum);
					
					if (service != null) {
						
						ScriptBuffer.get(lineNum)[1] = server.serverName;
						
						if (!Services.contains(service)) {
							
							Services.add(service);
							
							ServiceBuffer.put(service,ScriptBuffer.get(lineNum)[2]);
							
							addToRMI(service);
							
						}
						
					}
				}
				
				System.out.println("Registering a Triple...");
				
			} else {
				
				Line.append("REMOVE");
				
				System.out.println("Removing a Triple...");
				
			}
			
			
			for (int j = 0; j < size; j++) {
				
				if (ScriptBuffer.get(lineNum)[j] != null) {
					
					Line.append(" ");
					
					Line.append(ScriptBuffer.get(lineNum)[j]);
					
				}
				
			}
			
			Line.append("\n");
			
		}
		
		if (size == 1) {
			
			if (ScriptBuffer.get(lineNum)[0].equalsIgnoreCase("SHUTDOWN")) {
				
				Line.append("SHUTDOWN");
				
				Line.append("\n");
			} else {
				
				int seconds = Integer.parseInt(ScriptBuffer.get(lineNum)[0]);
				
				System.out.println("Client sleeping for " + seconds + " seconds");
				
				try {
					
					Thread.sleep(seconds*1000);
					
				} catch (InterruptedException IE) {
					System.out.println("Interrupted at Procscriptbuffer");
					//IE.printStackTrace();
					
				}
				
				Line.append("SLEEP"); 
				
			}
			
			
		}
		
		if (size == 2) {
			
			String FirstTriple = "no query data found";
			
			Line.append("QUERY");
			
			if (ScriptBuffer.get(lineNum)[0] != null) {
					
					Line.append(" ");
					
					String TransId = RandomAlphaNum();
					
					System.out.println("Generating random QueryID string " + TransId);
				
					Line.append(TransId);
				
					Line.append(" ");
					
					Line.append(ScriptBuffer.get(lineNum)[0]);
					
					TransFirstTriple.put(TransId, ScriptBuffer.get(lineNum)[0]);
					
					FirstTriple = ScriptBuffer.get(lineNum)[0];
					
			}
			
			if (ScriptBuffer.get(lineNum)[1] != null) {
				
				Line.append(" ");
			
				Line.append(ScriptBuffer.get(lineNum)[1]);
				
			} else if (ScriptBuffer.get(lineNum)[0] != null) {
				
				Line.append(" ");
				
				Line.append("*");
				
			}
			
			Line.append("\n");
			
			System.out.println("Querying <" + FirstTriple + ">");
			
		}

		return Line.toString();
		
	}
	
	/*
	 * Process the script file and store it in the ScriptBuffer on the server side
	 */
	public void ProcessScriptFile(String ScriptFile) throws IOException { 
		
		FileInputStream inputstream;
		
		BufferedReader in = null;
		
		try {
			inputstream = new FileInputStream(ScriptFile);
			
			in = new BufferedReader(new InputStreamReader(new DataInputStream(inputstream)));
			
			String line;
			
			line = in.readLine();
			
			while (line != null) {
			
				token = new StringTokenizer(line);
				
				int tcount = token.countTokens();
				
				if ( tcount <= 0) {
					 
					 line = in.readLine();
				 
					 continue;
					 
				}
				
				String token1 = token.nextToken();
				
				if (token1.charAt(0) == '%') {
					
					line = in.readLine();
					
					continue;
					
				}
				
				ProcessScriptLine(token1);
								
				line = in.readLine();
			}
			
			inputstream.close();
			
			token = null;
			
		} catch (IOException e) {
			
			System.out.println("Input output error at ProcessScriptfile");
			//e.printStackTrace();
			
		}
		
	}
	/*
	 * Process the script input through the command line
	 */
	public void ProcessScript(){
		
		try {
		
			String line;
			
			line = systemIn.readLine();
			
			token = new StringTokenizer(line);
			
			int tcount = token.countTokens();
			
			if ( tcount <= 0) {
				 
				 ProcessScript();
				 
				 return;
				 
			}
			
			String token1 = token.nextToken();
			
			if (token1.charAt(0) == '%') {

				ProcessScript();
				
				return;
				
				
			}
			ProcessScriptLine(token1);
			
		} catch (IOException e) {
			
			System.out.println("System standard input not readable");
			
			//e.printStackTrace();
			
		}
		
	}
	/*
	 * service provider side script line processing
	 */
	private void ProcessScriptLine(String token1) {
		
		int len = token1.length();
		
		for (int i = 0; i < len; i++) {

			if (token1.charAt(i) == '%') {
				
				len = i;
				
				break;
				
			}
			
		}
				
		token1 = token1.substring(0,len);
		
		if (token1.equalsIgnoreCase("REGISTER")) {
			
			AddToBuffer(3);
			
		} else if (token1.equalsIgnoreCase("QUERY")) {
			
			AddToBuffer(2);
			
		} else if (token1.equalsIgnoreCase("REMOVE")) {
			
			AddToBuffer(4);
			
		} else if (token1.equalsIgnoreCase("SHUTDOWN")) {
			
			lineArray = new String[1];
			
			lineArray[0] = "SHUTDOWN";
			
			ScriptBuffer.add(lineArray);
			
		} else if (token1.equalsIgnoreCase("SLEEP")) {
			
			AddToBuffer(1);
			
		} else if (token1.equalsIgnoreCase("END")) {
			
			server.GivenEnd = true;
			
		} else if (token1.equalsIgnoreCase("EXIT")) {
			
			server.GivenExit = true;
			
		} else {
			
			lineArray = new String[5];
			
			lineArray[0] = token1;
			
			ScriptBuffer.add(lineArray);
			
		}
		
	}
	
	/*
	 * Just add the String array to the server's "ScriptBuffer" ArrayList.
	 */
	private void AddToBuffer(int len) {
		
		lineArray = new String[len];
		
		for (int i = 0; i < len; i++) {
			
			if (token.hasMoreTokens()) {
				
				String s = token.nextToken();
				
				if (s.charAt(0) != '%') {
					
					int leng = s.length();
					
					for (int j = 0; j < leng; j++) {

						if (s.charAt(j) == '%') {
							
							leng = j;
							
							break;
							
						}

					}
					
					lineArray[i] = s.substring(0,leng);
					
				} else {
					
					break;
				}
				
			} else {
				
				break;
			}
			
		}
		
		ScriptBuffer.add(lineArray);
	}
	
}


