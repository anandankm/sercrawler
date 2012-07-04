import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;

public class ServerTime implements Server_Time{
	/*
	 * First or Second arguments must not be null or any character or string that is not
	 * an integer.
	 * (non-Javadoc)
	 * @see serverAddInterface#add_number(java.lang.String, java.lang.String)
	 */
	
	public String serverTime() throws RemoteException {
		String strAscii=null;
		long timeInMillis = System.currentTimeMillis();
		String str=Long.toString(timeInMillis);
		try {
			strAscii = new String (str.getBytes ("ASCII"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} 
		return strAscii ;
	}
	
}
