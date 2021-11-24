package src.Interfaces;
import java.rmi.*;

import src.Support.UtentiDB;

public interface NotifyClient_interface extends Remote {

	public void notifyClient(UtentiDB udbUpdated) throws RemoteException;
	
	public void notifyMember(String projectName,String Address) throws RemoteException;

	public void notifyCancel(String projectName) throws RemoteException;
	
}
