import java.net.*;
import java.io.*;
import java.util.Properties;

// SocketComm class used for all socket related activities including send message and receive message and handle their exceptions
public class SocketComm {
	public Socket socket = null;
	BufferedReader inStream = null;
	PrintStream outStream = null;
	
	// open the connection and obtain streams
	SocketComm(String hostname, int serverPort) {
		try {
			socket = new Socket(hostname, serverPort);
		} catch (Exception e) {
			System.out.println("Error Connecting to server at "+hostname+":"+serverPort);
			return;
			//System.exit(-2);
		}
		try {
			inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outStream = new PrintStream(socket.getOutputStream());
		} catch (Exception e) {
			System.out.println("Error Opening socket, I/O streams\n" + e);
			//System.exit(-4);
		}
	}

	// Send the message
	public void write(String mess) {
		 try {
			 outStream.println(mess);
		 } catch (Exception e) {
			 System.out.println("Error Sending message \n" + e);
		 }
	}
	// Wait and recv a line
	public String read() {
		try {
			return inStream.readLine();
		} catch (Exception e) {
			System.out.println("Error Recv message \n"+e);
		}
		return null;
	}
	// Close socket()
	public void close() {
		try {
			socket.close();
		} catch (Exception e) {
			System.out.println("Error Closing socket \n"+e);
			try {  socket.close(); } catch (Exception ee) {}
		}
	}
		
}
