import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Golden_Ratio extends Remote{
	
	String serviceAgreement() throws RemoteException;
	
	String ratioTerms() throws RemoteException;

}
