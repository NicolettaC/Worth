package src.Rmi;
import java.rmi.*;
import java.rmi.server.*;
import java.util.HashMap;

import src.Interfaces.*;
import src.Support.ProjectChat;
import src.Support.UtentiDB;

public class NotifyEventClient extends RemoteObject implements NotifyClient_interface{
	 
	private static final long serialVersionUID = 1L;
	private UtentiDB udb ;
	private HashMap<String,String> projectAddress;
	private HashMap<String,ProjectChat> pjChat; 
	
	public NotifyEventClient(UtentiDB usdb,HashMap<String,String> projectAddress,HashMap<String,ProjectChat> pjChat) throws RemoteException {
		super();
		udb=usdb;
		this.projectAddress=projectAddress;
		this.pjChat=pjChat;
	}

	//la struttura locale del client viene aggiornata con quella passata come parametro
	public void notifyClient(UtentiDB udbUpdated) throws RemoteException{
		udb.copy(udbUpdated);
	}
	
	//la struttura locale viene aggiornata con il nuovo progetto
	public void notifyMember(String projectName, String Address) throws RemoteException{
		
		projectAddress.put(projectName, Address);
		
		ProjectChat pjc= new ProjectChat(Address);
		
		new Thread(pjc).start();
		
		pjChat.put(projectName, pjc);
		
		System.out.println("Notify >> You are a new member of the project " + projectName );
	}

	//Rimuovo dalla struttura locale il progetto eliminato e chiudo thread chat
	public void notifyCancel(String projectName) throws RemoteException{
		
		projectAddress.remove(projectName);
		
		pjChat.get(projectName).stop();
		
		pjChat.remove(projectName);
		
		System.out.println("Notify >> The project " + projectName + ", has been cancelled");
		
	}
	
	
	
	
}
