import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class shaDigest implements SHA_Digest{
	/*
	 * (non-Javadoc)
	 * @see serverAddInterface#add_number(java.lang.String, java.lang.String)
	 */
	public String SHADigest(String data) throws RemoteException {
		try {
		final char[] hexChars = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
		String returndigest=null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(data.getBytes(), 0, data.getBytes().length);
			byte[] hash = md.digest();

			// to convert to hexadecimal string
					            StringBuffer sb = new StringBuffer();
					            int msb;
					            int lsb = 0;
					            int i;
					            for (i = 0; i < hash.length; i++) {
					                msb = ((int)hash[i] & 0x000000FF) / 16;
					                lsb = ((int)hash[i] & 0x000000FF) % 16;
					                sb.append(hexChars[msb]);
					                sb.append(hexChars[lsb]);
				            }
			  returndigest=sb.toString();
			  //System.out.println(st);

			//returndigest=new String(hash);
			//System.out.println(returndigest);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("No such algorithm exist");;
		}
		return returndigest;
		
	} catch (Exception e) {
		
		return "SHA_Digest: Input Error";
		
	}
	}


	public String serviceAgreement() throws RemoteException {
		String cost=null;
		String S=null;
		try {
			Properties tempobj3 = new Properties();
			InputStream tempfileobj = new FileInputStream("shadigestconfig.ini");
			tempobj3.load(tempfileobj);
			cost= tempobj3.getProperty("cost");
			S= tempobj3.getProperty("servicegurantee");
			tempfileobj.close();
		}
		catch(IOException e){
			System.out.println("connot open config cuberootconfig.ini file");
		}
		return (cost+":"+S);
	}
	
}
