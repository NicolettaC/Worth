package src.Interfaces;
import java.rmi.*;
import java.util.LinkedList;

import src.Support.UtentiDB;

public interface NotifyServer_interface extends Remote {

	public void registerForCallback(String username ,NotifyClient_interface ClientInterface) throws RemoteException;
	
	public void unregisterForCallback(String username ,NotifyClient_interface ClientInterface) throws RemoteException;
	
	public void update(UtentiDB udb) throws RemoteException;

	public void NotifyCallback(UtentiDB udb) throws RemoteException;

	public void updateMember(String username, String projectName,String Address) throws RemoteException;
	
	public void updateMembers(LinkedList<String> members,String projectName) throws RemoteException;
	
}
