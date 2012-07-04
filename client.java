//package example.hello;

import java.rmi.registry.*;
import java.io.*;
import java.util.*;
//import java.net.*;

public class client {

	/* The Main function
		1. Verifies no of arguments, if wrong prints error
		2. Creates instance of client class 
		3. calls procCommands method (which actually reads and processes the input)
	*/
	public static void main(String[] args) {
	
		// Validates arguments
		if(!(args.length>=2&&args.length<=3)) {
			System.out.println("Usage java client crawlername crawlerportno");
			System.out.println("Usage java client crawlername crawlerportno scriptfilename");
			System.exit(-10);
		}
		BufferedReader in = null;
		
		// Opens appropriate file/stdin
		if(args.length==3) {
			System.out.println("Opening file "+args[2]);
			scriptfile=true;
			try {
				in=new BufferedReader(new FileReader(args[2]));
			} catch (Exception e) {
				System.out.println("Error opening file "+args[2]+" Exiting...");
				System.exit(-100);
			}
		} else {
			scriptfile=false;
			try {
				in=new BufferedReader(new InputStreamReader(System.in));
			} catch (Exception e) {
				System.out.println("Error opening Stdin Exiting...");
				System.exit(-101);
			}
		}
		int portno=-1;
		try {
			portno=Integer.parseInt(args[1]);
		} catch (Exception e) {
			System.out.println("Error parsing port no "+args[1]+" Exiting...");
			System.exit(-102);
		}
		client cli = new client(args[0],portno,in);
		cli.procCommands();
	}
	static boolean scriptfile=false;
	String commread="";
	Random r = new Random();
	String host=null;
	int portno=-1;
	BufferedReader in=null;
	
	
	client(String host, int portno, BufferedReader in) {
		this.host=host;
		this.portno=portno;
		this.in=in;
	}
	
	// Trims the input to remove comments and blank spaces around the command
	private String trim(String str) {

		// First Strip off comments
		if (str.indexOf('%')>=0)
			str=str.substring(0,(str.indexOf('%')));
		//Trim Blank spaces
		str=str.trim();
		return str;
	}
	
	// Reads input and identifies the command invoked and calls the appropriate 
	public void procCommands() {
		System.out.println("Waiting for commands:");
		while(true) {
			String read=null;
			try {
				read=in.readLine();
				commread=read;
				if(scriptfile)
					System.out.println("Command read: '"+commread+"'");
			} catch (Exception e) {
				System.out.println("I/O exception occured while reading from console/scriptfile: Possibly file/input terminated without end command Exiting...");
				System.exit(-201);
			}
			if(read==null) {
				System.out.println("End of file/input received without End command... Exiting anyway");
				System.exit(-200);
			}
			read=trim(read);
			if(read.equals(""))
				continue;
			if(read.toLowerCase().startsWith("end ")||read.equalsIgnoreCase("end"))
				handleEnd(read);
			else if(read.toLowerCase().startsWith("cube-root ")||read.equalsIgnoreCase("cube-root"))
				handleCubeRoot(read,0);
			else if(read.toLowerCase().startsWith("square-root ")||read.equalsIgnoreCase("square-root"))
				handleSquareRoot(read,0);
			else if(read.toLowerCase().startsWith("server-time ")||read.equalsIgnoreCase("server-time"))
				handleServerTime(read,0);
			else if(read.toLowerCase().startsWith("golden-ratio ")||read.equalsIgnoreCase("golden-ratio"))
				handleGoldenRatio(read,0);
			else if(read.toLowerCase().startsWith("sha-digest ")||read.equalsIgnoreCase("sha-digest"))
				handleSHADigest(read,0);
			else if(read.toLowerCase().startsWith("list ")||read.equalsIgnoreCase("list")) {
				String words[]=read.split(" ");
				if(words.length>=2) {
					String command=words[1];
					int toStart=2; String toAppend="";
					if(words.length>=4 && words[2].equalsIgnoreCase("where")) {
						if(words.length<4) {
							System.out.println("Error 0x003: list command received with invalid parameters, Ignoring\n Offending line is "+commread);
							continue;
						}
						toStart=4;
						toAppend=" "+words[2]+" "+words[3];
						
					}
					if(read.toLowerCase().indexOf("where")<0)
						toAppend=" where *";
					for(int i=toStart+1;i<words.length;i++)
						command=command+" "+words[i];
					command=command+toAppend;
					
					if(words.length==2||read.toLowerCase().indexOf("using")<0) {
						if(command.split(" ").length==3&&command.endsWith(" where *")) {
							System.out.println("\"Using\" clause not present in list command, will attempt to simply print the table for the appropriate service");
							listServers(words[1]);
						} else
							System.out.println("Error 0x003: list command received with invalid parameters, Ignoring\n Offending line is "+commread);
						continue;
					}
					else if(command.toLowerCase().startsWith("cube-root "))
						handleCubeRoot(command,1);
					else if(command.toLowerCase().startsWith("square-root "))
						handleSquareRoot(command,1);
					else if(command.toLowerCase().startsWith("server-time"))
						handleServerTime(command,1);
					else if(command.toLowerCase().startsWith("golden-ratio ")||command.equalsIgnoreCase("golden-ratio"))
						handleGoldenRatio(command,1);
					else if(command.toLowerCase().startsWith("sha-digest "))
						handleSHADigest(command,1);
					else
						System.out.println("Error 0x003: list command received with invalid parameters, Ignoring\n Offending line is "+commread);
				} else
					System.out.println("Error 0x003: list command received with invalid parameters, Ignoring\n Offending line is "+commread);
			} else
				System.out.println("Unknown: Error 0x005: Unknown command read from console/scriptfile - "+read);
		}
	}
	
	// Handles the end command
	public void handleEnd(String read) {
		if(read.equalsIgnoreCase("end")) {
			System.out.println("End command read.. terminating..");
			System.exit(0);
		}
		if(read.toLowerCase().startsWith("end "))
			System.out.println("Error 0x003: End command received with parameters, Ignoring\n Offending line is "+commread);
	}
	
	// Searches for server matching the service "service" which can have the variables n,m,p in them and also satisfy the "condition"
	public String findServer(String service, int n, int m,int p, String condition, int listMode) {
		int matchCount=0;
		try {
			SocketComm soc = null;
			int ran=r.nextInt()%10000;
			if(ran<0) ran*=-1;
			
			// Connect and say hello, hi, find the services and close connection
			soc = new SocketComm(host,portno);
			if(soc==null||soc.socket==null) {
				System.out.println("Unable to contact crawler service, the current search for "+service+" cannot be processed");
				return null;
			}
			soc.write("HELLO");
			String reply=soc.read();
			if(reply==null|| (!reply.equalsIgnoreCase("HI"))) {
				System.out.println("Unable to contact crawler service, the current search for "+service+" cannot be processed");
				return null;
			}
			soc.write("QUERY "+ran+" "+service+" *");
			reply=soc.read();
			if(reply==null) {
				System.out.println("Unable to contact crawler service, the current search for "+service+" cannot be processed");
				return null;
			}
			if(reply.toLowerCase().startsWith("error")) {
				handleSocketErrorMess(reply);
				//System.out.println("Server says error : "+reply);
				return null;
			}
			String words[]=reply.split(" ");
			if(words.length!=3||(!words[0].equalsIgnoreCase("QUERY-REPLY"))||(!words[1].equalsIgnoreCase(""+ran))) {
				if(listMode==0)
					System.out.println("Invalid response from crawler service, the current command "+service+" cannot be processed"+"\nThe response is "+reply);
				else
					System.out.println("Invalid response from crawler service, the current command "+"list"+" cannot be processed"+"\nThe response is "+reply);
				return null;
			}
			int count=-1;
			try {
				count=Integer.parseInt(words[2]);
			} catch(Exception e) {
				if(listMode==1)
					System.out.println("Invalid response from crawler service, the current command "+"list"+" cannot be processed"+"\nThe response is "+reply);
				else
					System.out.println("Invalid response from crawler service, the current command "+service+" cannot be processed"+"\nThe response is "+reply);
				return null;
			}
			System.out.println(count+" servers provide the service "+service);
			String responses[]=new String[count];
			for(int i=0;i<count;i++) {
				responses[i]=soc.read();
			}
			try {
				soc.write("BYE");
				soc.read();
				soc.close();
			} catch (Exception ee){}	
			// Evaluate condition and values required for it and return the first match
			for(int i=0;i<count;i++) {
				String response=responses[i];
				String wordsres[]=response.split(" ");
				if(wordsres.length<5||wordsres.length>6) {
					System.out.println("Invalid response from crawler service, the current command "+service+" cannot be processed"+"\nThe response is "+response);
					return null;
				}
				
				String server=wordsres[4];
				if(service.equalsIgnoreCase("server-time")) {
					if(listMode==0) {
						System.out.println("Not evaluating conditions for server-time call, selecting first available server : "+server);
						return server;
					}
					else {
						if(matchCount==0)
							System.out.println("The providers that satisfy the conditions are");
						matchCount++;
						System.out.println("\t"+matchCount+". "+server);
						continue;
					}
				}
				if(wordsres.length!=6) {
					System.out.println("Invalid response from crawler service, the current command "+service+" cannot be processed"+"\nThe response is "+response);
					return null;
				}
				String agreement=wordsres[5];
				String cost=agreement.split(":")[0];
				String guarantee=agreement.split(":")[1];
				if(condition.equalsIgnoreCase("^1=1234")) {
					if(listMode==0) {
						System.out.println("No constrains on cost and guarantee specified, selecting first response");
						//return server;
					}
				}/* else {
						if(matchCount==0)
							System.out.println("The providers that satisfy the conditions are");
						matchCount++;
						System.out.println("\t"+matchCount+". "+server);
						continue;
					}
				}*/
				try {
					if(service.equalsIgnoreCase("sha-digest")||service.equalsIgnoreCase("square-root")||service.equalsIgnoreCase("cube-root"))
						cost=cost.replaceAll("n",n+"");
					if(service.equalsIgnoreCase("square-root")||service.equalsIgnoreCase("cube-root")) {
						cost=cost.replaceAll("m",m+"");
						cost=cost.replaceAll("p",p+"");
					}
					int costInt=Evaluator.evaluateIntExpr(cost);
					int sg=Integer.parseInt(guarantee);
					String conditioncopy=condition.replaceAll("C",costInt+"");
					conditioncopy=conditioncopy.replaceAll("c",costInt+"");
					conditioncopy=conditioncopy.replaceAll("S",sg+"");
					conditioncopy=conditioncopy.replaceAll("s",sg+"");
					if(Evaluator.evaluateBoolExpr(conditioncopy)==1) {
						//System.out.println(server + " satisfies cost and service agreement constrains and is selected");
						if(listMode==0) {
							System.out.println("Selecting service-provider "+server+" with cost "+costInt+" and service guarantee "+sg);
							return server;
						}
						else {
							if(matchCount==0)
								System.out.println("The providers that satisfy the conditions are");
							matchCount++;
							System.out.println("\t"+matchCount+". "+server+" with cost "+costInt+" service guarantee "+sg);
							continue;
						}
					} //else
						//System.out.println(server + " does not satisfy cost and agreement constraints, checking next server");
				} catch (Exception e) {
					System.out.println("An exception occurred evaluating the constrains, please check the conditions on cost and service guarantee");
					return null;
				}
			}
			if(listMode==0||(listMode==1&&matchCount==0))
				if(condition.equalsIgnoreCase("^1=1234"))
					System.out.println("No matches found for "+service);
				else
					System.out.println("No matches found for "+service+" with constraints "+condition);
			return null;
		}
		catch (Exception e) {
			System.out.println("Error occured while finding matches for "+service+", the crawler may be dead");
			//e.printStackTrace();
		}
		return null;
	}
	
	//Handle Cube root
	public void handleCubeRoot(String read, int listMode) {
		
		Registry registry = null;
		String words[]=read.split(" ");
		if((!(words.length==3||words.length==5))||(words.length>3&&(!(words[3].equalsIgnoreCase("where"))))) {
		
			if(listMode==0)
				System.out.println("Error 0x003: cube-root command with invalid parameters, will not be processed, Ignoring\nThe offending line is "+commread);
			else
				System.out.println("Error 0x003: list command with invalid parameters, will not be processed, Ignoring\nThe offending line is "+commread);
			return;
		}
		int n,m,p; String cond="*";
		if(words.length>3&&(!words[4].equalsIgnoreCase("*")))
			cond=words[4];
		else
			cond="^1=1234";
		try {
			if(words[1].indexOf('.')>=0) {
				String parts[]=words[1].split("[.]");
				if(parts.length>2)
					throw (new Exception());
				n=words[1].length()-1;
				m=parts[1].length();
			} else {
				n=words[1].length();
				m=0;
			}
			p=Integer.parseInt(words[2]);
		} catch (Exception e) {
			System.out.println("Error parsing arguments of cube-root, will not be processed further\n Offending line is "+commread);
			return;
		}
		String server=findServer("cube-root",n,m,p,cond,listMode);
		if(listMode==1)
			return;
		if(server==null||server.trim().length()==0) {
			System.out.println("No matchin servers found for cube-root: The line is "+commread);
			return;
		}
		try {
			registry = LocateRegistry.getRegistry(server);
			System.out.println("RMI: Connected to RMI registry, trying obtain remote server object: cube-root");
		} catch (Exception e) {
			System.err.println("RMI: Unable to connect to RMI Reg at " + server);
		    	//e.printStackTrace();
		}
		try {
			Cube_Root cr= (Cube_Root) registry.lookup("Cube_Root");
			
			//System.out.println("RMI: Object cube-root obtained successfully");
			String res=cr.cubeRoot(words[1],p);
			if(res.toUpperCase().startsWith("FALSE"))
				System.out.println("Execution unsuccessfull, server says error ");
			else
				System.out.println("Result is "+res);

		} catch (Exception e) {
			System.err.println("RMI: Unable to obtain object: cube-root, server may be dead or object does not exist");
		    	//e.printStackTrace();
		}
	}
	
	//Handle Square Root message
	public void handleSquareRoot(String read, int listMode) {
		Registry registry = null;
		String words[]=read.split(" ");
		if((!(words.length==3||words.length==5))||(words.length>3&&(!(words[3].equalsIgnoreCase("where"))))) {
			if(listMode==0)
				System.out.println("Error 0x003: square-root command with invalid parameters, will not be processed, Ignoring\nThe offending line is "+commread);
			else
				System.out.println("Error 0x003: list command with invalid parameters, will not be processed, Ignoring\nThe offending line is "+commread);
			return;
		}
		int n,m,p; String cond="*";
		if(words.length>3&&(!words[4].equalsIgnoreCase("*")))
			cond=words[4];
		else
			cond="^1=1234";
		try {
			if(words[1].indexOf('.')>=0) {
				String parts[]=words[1].split("[.]");
				if(parts.length>2)
					throw (new Exception());
				n=words[1].length()-1;
				m=parts[1].length();
			} else {
				n=words[1].length();
				m=0;
			}
			p=Integer.parseInt(words[2]);
		} catch (Exception e) {
			System.out.println("Error parsing arguments of square-root, will not be processed further\n Offending line is "+commread);
			return;
		}
		String server=findServer("square-root",n,m,p,cond,listMode);
		if(listMode==1)
			return;
		if(server==null||server.trim().length()==0) {
			System.out.println("No matching servers found for square-root: The line is "+read);
			return;
		}
		try {
			registry = LocateRegistry.getRegistry(server);
			System.out.println("RMI: Connected to RMI registry, trying obtain remote server object: square-root");
		} catch (Exception e) {
			System.err.println("RMI: Unable to connect to RMI Reg at " + server);
		    	//e.printStackTrace();
		}
		try {
			Square_Root cr= (Square_Root) registry.lookup("Square_Root");
			
			//System.out.println("RMI: Object square-root obtained successfully");
			String res=cr.squareRoot(words[1],p);
			if(res.toUpperCase().startsWith("FALSE"))
				System.out.println("Execution unsuccessfull, server says error with params");
			else
				System.out.println("Result is "+res);

		} catch (Exception e) {
			System.err.println("RMI: Unable to obtain object: square-root, server may be dead or object does not exist");
		    	e.printStackTrace();
		}
	}
	
	// Handle Server time message
	public void handleServerTime(String read, int listMode) {
		Registry registry = null;
		if(read.equalsIgnoreCase("server-time where *"))
			read="server-time";
		String words[]=read.split(" ");
		if(!(read.equalsIgnoreCase("server-time")||read.equalsIgnoreCase("server-time where *"))) {
			if(listMode==0)
				System.out.println("Error 0x003: server-time command with invalid parameters, will not be processed, Ignoring\nThe offending line is "+commread);
			else
				System.out.println("Error 0x003: list command with invalid parameters, will not be processed, Ignoring\nThe offending line is "+commread);
			return;
		}
		int n=1,m=1,p=1; String cond="*";
		
		String server=findServer("server-time",n,m,p,cond,listMode);
		if(listMode==1)
			return;
		if(server==null||server.trim().length()==0) {
			System.out.println("No matching servers found for server-time: The line is "+read);
			return;
		}
		try {
			registry = LocateRegistry.getRegistry(server);
			System.out.println("RMI: Connected to RMI registry, trying obtain remote server object: server-time");
		} catch (Exception e) {
			System.err.println("RMI: Unable to connect to RMI Reg at " + server);
		    	//e.printStackTrace();
		}
		try {
			Server_Time cr= (Server_Time) registry.lookup("Server_Time");
			
			//System.out.println("RMI: Object server-time obtained successfully");
			String res=cr.serverTime();
			if(res.toUpperCase().startsWith("FALSE"))
				System.out.println("Execution unsuccessfull, server says error with params");
			else
				System.out.println("Result is "+res);

		} catch (Exception e) {
			System.err.println("RMI: Unable to obtain object: server-time, server may be dead or object does not exist");
		    	//e.printStackTrace();
		}
	}
	// Handle Golden Ratio
	public void handleGoldenRatio(String read, int listMode) {
		Registry registry = null;
		String words[]=read.split(" ");
		if((!(words.length==1||words.length==3))||(words.length>1&&(!(words[1].equalsIgnoreCase("where"))))) {
			if(listMode==0)
				System.out.println("Error 0x003: golden-ratio command with invalid parameters, will not be processed, Ignoring\nThe offending line is "+commread);
			else
				System.out.println("Error 0x003: list command with invalid parameters, will not be processed, Ignoring\nThe offending line is "+commread);
			return;
		}
		int n=1,m=1,p=1; String cond="*";
		if(words.length>2&&(!words[2].equalsIgnoreCase("*")))
			cond=words[2];
		else
			cond="^1=1234";
		
		String server=findServer("golden-ratio",n,m,p,cond,listMode);
		if(listMode==1)
			return;
		if(server==null||server.trim().length()==0) {
			System.out.println("No matching servers found for golden-ratio: The line is "+read);
			return;
		}
		try {
			registry = LocateRegistry.getRegistry(server);
			System.out.println("RMI: Connected to RMI registry, trying obtain remote server object: golden-ratio");
		} catch (Exception e) {
			System.err.println("RMI: Unable to connect to RMI Reg at " + server);
		    	//e.printStackTrace();
		}
		try {
			Golden_Ratio cr= (Golden_Ratio) registry.lookup("Golden_Ratio");
			
			//System.out.println("RMI: Object golden-ratio obtained successfully");
			String res=cr.ratioTerms();
			if(res.toUpperCase().startsWith("FALSE"))
				System.out.println("Execution unsuccessfull, server says error with params");
			else
				System.out.println("Result is "+res);

		} catch (Exception e) {
			System.err.println("RMI: Unable to obtain object: golden-ratio, server may be dead or object does not exist");
		    	//e.printStackTrace();
		}
	}
	// Handle SHA Digest
	public void handleSHADigest(String read, int listMode) {
		Registry registry = null;
		int startQuote=read.indexOf("\"");
		int endQuote=read.lastIndexOf("\"");
		if(startQuote<0||endQuote<0||startQuote>=endQuote) {
			if(listMode==0)
				System.out.println("Error 0x003: sha-digest command with invalid parameters, will not be processed, Ignoring\nThe offending line is "+commread);
			else
				System.out.println("Error 0x003: list command with invalid parameters, will not be processed, Ignoring\nThe offending line is "+commread);
			return;
		}
		String data=read.substring(startQuote+1,endQuote);
		if(!read.substring(0,startQuote).trim().equalsIgnoreCase("sha-digest")) {
			if(listMode==0)
				System.out.println("Error 0x003: sha-digest command with invalid parameters, will not be processed, Ignoring\nThe offending line is "+commread);
			else
				System.out.println("Error 0x003: list command with invalid parameters, will not be processed, Ignoring\nThe offending line is "+commread);
			return;
		}
		
		int n=data.length(); int m=1,p=1; String cond="*";
		
		if((endQuote+2)<read.length()) {
			String afterData[]=read.substring(endQuote+2,read.length()).split(" ");
			if(afterData.length==2&&afterData[0].equalsIgnoreCase("where"))
				if(afterData[1].equalsIgnoreCase("*"))
					cond="^1=1234";
				else
					cond=afterData[1];
			else {
			if(listMode==0)
				System.out.println("Error 0x003: sha-digest command with invalid parameters, will not be processed, Ignoring\nThe offending line is "+commread);
			else
				System.out.println("Error 0x003: list command with invalid parameters, will not be processed, Ignoring\nThe offending line is "+commread);
				return;
			}
		}
		else
			cond="^1=1234";
			
		
		String server=findServer("sha-digest",n,m,p,cond,listMode);
		if(listMode==1)
			return;
		if(server==null||server.trim().length()==0) {
			System.out.println("No matching servers found for sha-digest: The line is "+read);
			return;
		}
		try {
			registry = LocateRegistry.getRegistry(server);
			System.out.println("RMI: Connected to RMI registry, trying obtain remote server object: sha-digest");
		} catch (Exception e) {
			System.err.println("RMI: Unable to connect to RMI Reg at " + server);
		    	//e.printStackTrace();
		}
		try {
			SHA_Digest cr= (SHA_Digest) registry.lookup("SHA_Digest");
			
			//System.out.println("RMI: Object sha-digest obtained successfully");
			String res=cr.SHADigest(data);
			if(res.toUpperCase().startsWith("FALSE"))
				System.out.println("Execution unsuccessfull, server says error with params");
			else
				System.out.println("Result of SHADigest is "+res);

		} catch (Exception e) {
			System.err.println("RMI: Unable to obtain object: sha-digest, server may be dead or object does not exist");
		    	//e.printStackTrace();
		}
	}
	private void handleSocketErrorMess(String error)
	{
		if(error.toUpperCase().startsWith("ERROR 0x003"))
			System.out.println("Error 0x003: Malformed protocol message");
		else if(error.toUpperCase().startsWith("ERROR 0x005"))
			System.out.println("Error 0x005: unknown protocol message");
	}
	private void listServers(String service) {
		try {
			SocketComm soc = null;
			int ran=r.nextInt()%10000;
			if(ran<0) ran*=-1;
			
			// Connect and say hello, hi, find the services and close connection
			soc = new SocketComm(host,portno);
			if(soc==null||soc.socket==null) {
				System.out.println("Unable to contact crawler service, the current search for "+service+" cannot be processed");
				return;
			}
			soc.write("HELLO");
			String reply=soc.read();
			if(reply==null|| (!reply.equalsIgnoreCase("HI"))) {
				System.out.println("Unable to contact crawler service, the current search for "+service+" cannot be processed");
				return;
			}
			soc.write("QUERY "+ran+" "+service+" *");
			reply=soc.read();
			if(reply==null) {
				System.out.println("Unable to contact crawler service, the current search for "+service+" cannot be processed");
				return;
			}
			if(reply.toLowerCase().startsWith("error")) {
				handleSocketErrorMess(reply);
				//System.out.println("Server says error : "+reply);
				return;
			}
			String words[]=reply.split(" ");
			if(words.length!=3||(!words[0].equalsIgnoreCase("QUERY-REPLY"))||(!words[1].equalsIgnoreCase(""+ran))) {
					System.out.println("Invalid response from crawler service, the current command "+service+" cannot be processed"+"\nThe response is "+reply);
				return;
			}
			int count=-1;
			try {
				count=Integer.parseInt(words[2]);
			} catch(Exception e) {
				System.out.println("Invalid response from crawler service, the current command "+service+" cannot be processed"+"\nThe response is "+reply);
				return;
			}
			System.out.println(count+" servers provide the service "+service);
			for(int i=0;i<count;i++) {
				String result=soc.read();
				String words2[]=result.split(" ");
				if(words2.length==6)
					System.out.println("\t"+(i+1)+" "+words2[4]+" Service Agreement "+words2[5]);
				else if(words2.length==5&&service.equalsIgnoreCase("server-time"))
					System.out.println("\t"+(i+1)+" "+words2[4]);
				else
					System.out.println("Invalid response from crawler "+result);
			}
			try {
				soc.write("BYE");
				soc.read();
				soc.close();
			} catch (Exception ee){}	
		} catch (Exception e) {
			System.out.println("Error occured while finding matches for "+service+", the crawler may be dead");
		}
	}
}

