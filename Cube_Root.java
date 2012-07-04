import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Cube_Root extends Remote{
	
	String serviceAgreement() throws RemoteException;
	
	String cubeRoot(String input, int precision) throws RemoteException;

}
