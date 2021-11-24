package src.Rmi;

import src.Interfaces.*;
import src.Support.UtentiDB;

import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

public class NotifyEventServer extends RemoteObject implements NotifyServer_interface{
	
	private static final long serialVersionUID = 1L;

	private HashMap<String,NotifyClient_interface> clientsRegistrati; // hashmap nome utente e oggetto remoto
	
	public NotifyEventServer() throws RemoteException{
		super();
		clientsRegistrati = new HashMap<String,NotifyClient_interface>( ); 
	}
	
	public synchronized void registerForCallback(String username ,NotifyClient_interface ClientInterface) throws RemoteException{
		
		//se il client non è registrato nella lista callback lo aggiungo
		
		if(!clientsRegistrati.containsKey(username)) {
			clientsRegistrati.put(username, ClientInterface);
			System.out.println("New client registered for callback");
		}

	}
	
	public synchronized void unregisterForCallback(String username ,NotifyClient_interface ClientInterface) throws RemoteException{
	
		//disiscrivo l'utente dal servizio di notifica attraverso il suo username
		if(clientsRegistrati.remove(username)!=null) {
			System.out.println("client unregistered for callback");
		}
		else {
			System.out.println("error, unable to unregister client");
		}
	}
	
	public void update(UtentiDB udb) throws RemoteException{
		NotifyCallback(udb);
	}
	
	//callback per tutti gli utenti registrati a servizio di callback
	public synchronized void NotifyCallback(UtentiDB udb) throws RemoteException {
		
		for(NotifyClient_interface cli : clientsRegistrati.values()) {
			  System.out.println("Sending Notification");
			  cli.notifyClient(udb);
		  }		
			
	}

	//aggiorna utente di essere stato aggiunto come membro di un progetto
	public synchronized void updateMember(String username, String projectName,String Address) throws RemoteException{
	
		if(clientsRegistrati.containsKey(username)) {
		clientsRegistrati.get(username).notifyMember(projectName, Address);
		}
	}
	
	//aggiorna membri di cancellazione di un progetto
	public synchronized void updateMembers(LinkedList<String> members,String projectName) throws RemoteException{
		//per tutti i membri del progetto se sono registrati al servizio di notifica notificali
		for(String username: members) {
			if(clientsRegistrati.containsKey(username)) {
				clientsRegistrati.get(username).notifyCancel(projectName);
			}
		}
		
	}
	
}
	
