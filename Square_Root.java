import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Square_Root extends Remote{
	
	String serviceAgreement() throws RemoteException;
	
	String squareRoot(String input, int precision) throws RemoteException;

}
