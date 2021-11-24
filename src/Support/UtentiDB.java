package src.Support;


import java.io.Serializable;
import java.util.LinkedList;


public class UtentiDB implements Serializable{
	
	private static final long serialVersionUID = 1L;
    private LinkedList<Utente> utenti; //Contiene tutti gli utenti, associa l'username dell'utente all'istanza utente. 


    public UtentiDB(){
        this.utenti = new LinkedList<Utente>();
    }

    public void addUtente(Utente ut){
    	
        utenti.add(ut);
    }

    //Controlla se un utente è registrato
    public Boolean isRegistered(Utente ut){
    	
    	for(Utente u:utenti) {
    		if(u.getUsername().equals(ut.getUsername())) {
    			return true;
    		}
    	}
        return false;
    }
    
    //Restituisce l'utente con username in input se esiste, altrimenti restuisce null
    public Utente containUsername(String username) {
    	
    	for(Utente ut: utenti) {
    		String us=ut.getUsername();
    		if(us.equals(username)) {
    			return ut;
    		}
    	}
    	
    	return null;
    	
    }

   public void copy(UtentiDB udb) {
    	this.utenti=udb.getUtenti();
    }
    
    public LinkedList<Utente> getUtenti(){

        return this.utenti;
    }
    
    //Restituisce la lista degli utenti contenuti nel database
    public String ListUsers(){
        StringBuilder listUsers = new StringBuilder();
        for (Utente u: utenti) {                       // crea la lista degli utenti registrati al servizio
            listUsers.append(u.getUsername()).append(" ");
        	listUsers.append(u.getStatus()).append("\n");
        }
        return listUsers.toString(); 
    }
    
    //Restituisce la lista degli utenti contenuti nel database con stato online
    public String ListOnlineUsers(){
        StringBuilder listUsers = new StringBuilder();
        for (Utente u: utenti) { // crea la lista degli utenti registrati al servizio e online
            if(u.getStatus().equals("ONLINE")) {
        	listUsers.append(u.getUsername()).append(" ");
        	listUsers.append(u.getStatus()).append("\n");
            }
        }
        return listUsers.toString(); 
    }
    
    //Cambia lo stato degli utenti ad offline
    public void setOffline() {
    	 for (Utente u: utenti) { 
    		 u.setStatus("OFFLINE");
    	 }
    }
    
 


}