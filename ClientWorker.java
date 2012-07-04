//This is Broker side of program implementing runnable
//Author:AJAY JAIN
//29 september 2009
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ClientWorker implements Runnable{


	private Socket clisoc;
	ClientWorker(Socket clisoc) {
		this.clisoc = clisoc;
	}
	//String fileName = "config.ini";
	broker servobj=new broker();
	Long tdel= null;
	PrintWriter out = null;
	BufferedReader in = null;   
	FileReader readfile =null;
	BufferedReader brd =null;
	FileWriter filewrite=null;
	BufferedWriter bwd=null;
	String inputfrmclient=null;
	File fname= new File ("storedata.txt");
	File fname1= new File ("rmstoredata.txt");
	public void run(){
		try{
			//if file to store data does not exist ,one will be created
			if(!fname.exists()){
				fname.createNewFile();
			}
			//reading the lines sent by client 
			out = new PrintWriter(clisoc.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clisoc.getInputStream()));
			//creating a file name based on the thread name and sendig it to service provider to create file based on it
			//Thread t1 = Thread.currentThread();
			//String tempfile=t1.getName();
			//out.println(tempfile);
			//out.println("ok\n");
			


			while((inputfrmclient = in.readLine()) != "exit"){

				//	System.out.println(inputfrmclient);


				//checking various conditions and displaying the results.        	
				/*if (inputfrmclient.contains("register")){
					out.println("Registering Triples.....\n");
				}
				else {
					out.println("Wrong Format:please use register as keyword\n");
				}*/

				int count=0;
				//Tokenizers to split the lines in words using space as reference
				StringTokenizer wordsfromline = new StringTokenizer(inputfrmclient," ");
				ArrayList<String> temp = new ArrayList<String>();
				while (wordsfromline.hasMoreTokens()){
					String arrayvalue= new String(wordsfromline.nextToken());
					count++;
					temp.add(arrayvalue);
				}
				String inputstr ;


				String comparewordto =temp.get(0);



				//The block for processing the register command
				if (comparewordto.equalsIgnoreCase("REGISTER"))	{
					if (count==4){
						inputstr =temp.get(1)+" " +temp.get(2)+" " +temp.get(3)+" " ;
						//check for duplicates and take action accordingly
						String s ; 
						int flag=0;
						//locking file so that only operation from one client is permitted at a time
						brd=Readwritelock.readfile(fname);
						while ((s=brd.readLine())!= null){
							StringTokenizer swords = new StringTokenizer(s," ");
							ArrayList<String> scmp = new ArrayList<String>();
							while (swords.hasMoreTokens()){
								String svalue= new String(swords.nextToken());
								count++;
								scmp.add(svalue);
							}
							//compare if different then register
							if( (scmp.get(0).equals(temp.get(1)))&&(scmp.get(1).equals(temp.get(2))) && ((scmp.get(2).equals(temp.get(3))))){
								flag=1;	}
							

							else{
								continue;}
						}

						//take action on the set flag value ..flag 0 means register else  not			
						if (flag==0){
							//register the  value in the file by calling the function
							try {
								Readwritelock.writefile(fname,inputstr,true);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							//out.println(inputstr);
							out.println("STATUS TRUE");

						}
						else{
							out.println("STATUS TRUE");
							//out.println("Service Already exists in Database");
						}
					}//count
					else{
						out.println("ERROR 0x003");
					}
				} 



				//the block for processing remove command
				if (comparewordto.equalsIgnoreCase("REMOVE"))	{
					if(count==4){
						String s ;
						String toremove = null;
						BufferedReader brdc5=Readwritelock.readfile(fname);
						while ((s=brdc5.readLine())!= null){
							if ((s.contains(temp.get(1)))&&(s.contains(temp.get(2)))&&(s.contains(temp.get(3)))){
								toremove=s;
							}
						}
						if(!fname1.exists()){
							fname1.createNewFile();
						}

						brdc5.close();
						BufferedReader brdc6=Readwritelock.readfile(fname);
						while ((s=brdc6.readLine())!= null){
							if(s.equalsIgnoreCase(toremove))
								continue;
							try {
								Readwritelock.writefile(fname1,s,true);
							} catch (InterruptedException e) {
								System.out.println("Cannot write in file check the method");
								e.printStackTrace();
							}
						}
						brdc6.close();

						out.println("Status True");

						//renaming the temp file to original.as concurrent processing creates problem
						//contents were rewritten in the new file and then renamed for maintaing the integrity of program
						try {
							// Create channel on the source
							FileChannel source = new FileInputStream("rmstoredata.txt").getChannel();
							// Create channel on the destination
							FileChannel destination = new FileOutputStream("storedata.txt").getChannel();
							// Copy file contents from source to destination
							destination.transferFrom(source, 0, source.size());
							// Close the channels
							source.close();
							destination.close();
						} catch (IOException e) {
						}
						if(fname1.isFile()){
							fname1.delete();
						}
					}
					
						else{
							out.println("ERROR 0x003");
						}
					
				}//closing braces for remove
				//query
				if ((comparewordto.equalsIgnoreCase("list"))||(comparewordto.equalsIgnoreCase("query")))	{

					File fname2= new File ("storedata.txt");
					String s;
					int cnt=0;
					BufferedReader brdcnt4=Readwritelock.readfile(fname2);
					while ((s=brdcnt4.readLine())!= null){
						if (((s.contains(temp.get(2)))||(temp.get(2).equals("*")))&&((s.contains(temp.get(3)))||(temp.get(3).equals("*")))){
							cnt++;
						}
					}
					String sending="QUERY-REPLY "+ temp.get(1)+" "+cnt;
					out.println(sending);
					brdcnt4.close();
					BufferedReader brdc4=Readwritelock.readfile(fname);
					///out.println("RESPONSE \n");	
					int j = 0;
					while ((s=brdc4.readLine())!= null){
						if (((s.contains(temp.get(2)))||(temp.get(2).equals("*")))&&((s.contains(temp.get(3)))||(temp.get(3).equals("*")))){
							out.println("RESPONSE " + temp.get(1) + " "+ (j+1) + " " + s);
						}
					}
					brdc4.close();

				}
				
				if (comparewordto.equalsIgnoreCase("BYE"))	{
					out.println("LATER");
				}
				
				if (comparewordto.equalsIgnoreCase("HELLO"))	{
					out.println("HI");
				}
				else{	out.println("ERROR:0x003");
				}

			}

			clisoc.close();
			out.close();
			in.close();
		}
		catch(IOException e){
			System.out.println("Service Provider Terminated ");
		}
	}

}



