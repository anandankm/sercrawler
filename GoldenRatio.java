import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Properties;

public class GoldenRatio implements Golden_Ratio{
	/*
	 * (non-Javadoc)
	 * @see serverAddInterface#add_number(java.lang.String, java.lang.String)
	 */
public String ratioTerms() throws RemoteException {
	
	try {
	
		String str = null;
	
		String str1 = null;
		server.Lockupperfib.readLock().lock();
	
		try {
		
			str = server.upperfib;
			
			str1 = str;
		
		} finally {
		
			server.Lockupperfib.readLock().unlock();
			
		}
		String str2 = null;
		server.Locklowerfib.readLock().lock();
		
		try {
			str2 = server.lowerfib;
		
		} finally {
		
			server.Locklowerfib.readLock().unlock();
			
		}
		if (str1.length() > str2.length()) {
			
			str = str1 + ":" + str2;
			
		} else if (str2.length() > str1.length()) {
			
			str = str2 + ":" + str1;
		} else {
			
			if (str2.compareTo(str1) < 0) {
				
				str = str1 + ":" + str2;
				
			} else {
				
				str = str2 + ":" + str1;
				
			}
			
		}
		return str;
	} catch (Exception e) {
		
		return "GoldenRatio: Input Error";
		
	}
		
	}

	
		public String serviceAgreement() throws RemoteException {
			String cost=null;
			String S=null;
			try {
				Properties tempobj3 = new Properties();
				InputStream tempfileobj = new FileInputStream("goldenratioconfig.ini");
				tempobj3.load(tempfileobj);
				cost= tempobj3.getProperty("cost");
				S= tempobj3.getProperty("servicegurantee");
				tempfileobj.close();
			}
			catch(IOException e){
				System.out.println("connot open config grconfig.ini file");
			}
			return (cost+":"+S);
		}
	
}
