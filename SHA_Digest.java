import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SHA_Digest extends Remote{
	
	String serviceAgreement() throws RemoteException;
	
	String SHADigest(String data) throws RemoteException;

}
