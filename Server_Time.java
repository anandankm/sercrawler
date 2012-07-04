import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server_Time extends Remote{
	
	String serverTime() throws RemoteException;

}
