package src.Support;

import java.util.HashMap;

import java.util.LinkedList;


public class Project {

	/*In WORTH, un progetto, identificato da un nome univoco, 
	è costituito da una serie di “card” (“carte”), che
	rappresentano i compiti da svolgere per portarlo a termine, 
	e fornisce una serie di servizi*/
	
	private final String name; 			//nome univoco del progetto
	private LinkedList<String> members; //utenti che hanno i permessi per modificare le card e accedere aiservizi associati al progetto
	private String chatAddress; 		//indirizzo IP multicast per chat
	
	
	private HashMap<String, Card> TODO;
	private HashMap<String, Card> INPROGRESS;
	private HashMap<String, Card> TOBEREVISED;
	private HashMap<String, Card> DONE;
	
	//Costruttore utilizzato quando viene creato un nuovo progetto
	public Project(String projectname,Utente creator) {
		
		this.name=projectname;
		
		TODO=new HashMap<String, Card>();	
		INPROGRESS=new HashMap<String, Card>();
		TOBEREVISED=new HashMap<String, Card>();
		DONE=new HashMap<String, Card>();
		this.members= new LinkedList<String>(); 
		
		members.add(creator.getUsername());
	
	}
	
	//Costruttore utilizzato quando viene ricostruito lo stato del sistema
	public Project(String projectName) {
		
		this.name=projectName;
		
		TODO=new HashMap<String, Card>();	
		INPROGRESS=new HashMap<String, Card>();
		TOBEREVISED=new HashMap<String, Card>();
		DONE=new HashMap<String, Card>();
		this.members= new LinkedList<String>(); 
		
	}
	
	//Restituisce nome progetto
	public String getName() {
		return this.name;
	}

	//Aggiunge membro al progetto
	public void newMember(Utente u) {
		
		if(members.contains(u.getUsername())==false) {
			
			members.add(u.getUsername());
			
		}

	}
	
	//Restituisce una stringa contenente la lista dei membri al progetto
	public String listMembers() {
		
		 String listUsers = new String();
		 
	      for (String s: members) {                       
	            listUsers=listUsers + " "+ s ;
	      }
	        
	      return listUsers; 
	}
	
	//Restituisce una stringa contenente la lista delle cards contenute nel progetto e le liste che le contengono
	public String listCards() {
		
		String listCards = new String();
		for(String s: TODO.keySet()) {
			 listCards=listCards + "CARD: "+ s + " STATUS: TODO" + " - ";
		}
		for(String s: INPROGRESS.keySet()) {
			 listCards=listCards + "CARD: "+ s + " STATUS: INPROGRESS" + " - " ;
		}
		for(String s: TOBEREVISED.keySet()) {
			 listCards=listCards + "CARD: "+ s + " STATUS: TOBEREVISED" + " - " ;
		}
		for(String s: DONE.keySet()) {
			 listCards=listCards + "CARD: "+ s + " STATUS: DONE" + " - "  ;
		}
		return listCards;
	}
	
	//Restituisce membri progetto
	public LinkedList<String> showMembers(){
		return this.members;	
	}
	
	//Restituisce true se utente è membro del progetto false altrimenti
	public Boolean isMember(String username) {
		
		for(String s : members) {
			if(s.equals(username))
				return true;
		}
		
		return false;
	}

	//Copia i membri passati come parametro di input nei membri di questo progetto
	public void CopyMembers(LinkedList<String> members) {
		
		this.members=members;
		
	}
	
	//Crea una nuova card e la inserisce nella lista TODO
	public Card newCard(String cardname, String description) {
		
		Card card=null;
		
		if(cardExist(cardname)==false) {
			
			card = new Card(cardname,description);
			
			TODO.put(cardname, card);
		}
		
		return card;
	}

	//Muove la card dalla lista di partenza alla list di destinazione
	public void moveCard(String cardname, String partenza, String destinazione ) {
		
			
			Card temp=StringToList(partenza).remove(cardname);
			
			StringToList(destinazione).putIfAbsent(cardname, temp);
			
			temp.setCurrentList(destinazione);
		
	}
	
	//Associa il nome della lista passata come string alla lista 
	public HashMap<String, Card> StringToList(String list) {

		if (list.equals("TODO")) return TODO;
		if (list.equals("INPROGRESS")) return INPROGRESS;
		if (list.equals("TOBEREVISED")) return TOBEREVISED;
		
		return DONE;
		
		
	
	}

	//Restituisce true se lo spostamento da una lista ad un'altra rientra tra quelli consentiti false altrimenti
	public boolean validMove(String cardname,String partenza, String destinazione) {
		
		//Se lista di partenza non contiene la card la mossa non è valida
		if(StringToList(partenza).containsKey(cardname)==false)  return false;
		
		//TODO->INPROGRESS
		if(partenza.equals("TODO") && destinazione.equals("INPROGRESS")) return true;
		
		//INPROGRESS -> TOBEREVISED || DONE
		if(partenza.equals("INPROGRESS") && destinazione.equals("TOBEREVISED")) return true;
		if(partenza.equals("INPROGRESS") && destinazione.equals("DONE")) return true;
		
		//TOBEREVISED -> INPROGRESS || DONE
		if(partenza.equals("TOBEREVISED") && destinazione.equals("INPROGRESS")) return true;
		if(partenza.equals("TOBEREVISED") && destinazione.equals("DONE")) return true;
		
		//altri casi
		return false;
		
		
		
	}

	//Restituisce true se la card è contenuta in una delle liste false altrimenti
	public Boolean cardExist(String cardname) {
		return (TODO.containsKey(cardname) || INPROGRESS.containsKey(cardname) || TOBEREVISED.containsKey(cardname) || DONE.containsKey(cardname));
	}
	
	//Restituisce la card associata ad un nome
	public Card getCard(String cardname) {
		if(TODO.containsKey(cardname))	return TODO.get(cardname);
		if(INPROGRESS.containsKey(cardname))	return INPROGRESS.get(cardname);
		if(TOBEREVISED.containsKey(cardname))	return TOBEREVISED.get(cardname);
		return DONE.get(cardname);
	}
	
	//Ripristina stato delle card inserendole nella lista corretta
	public void insertInList(Card card) {
		String destinazione=card.getCurrentList();
		String cardname=card.getCardName();
		StringToList(destinazione).putIfAbsent(cardname, card);
		
	}
	
	//Restituisce true se tutte le cards del progetto sono nella lista done false altrimenti
	public Boolean isDone() {
		// controllo che le altre tre liste siano vuote
		return(TODO.isEmpty() && INPROGRESS.isEmpty() && TOBEREVISED.isEmpty());
	}

	//Set chat del progetto
	public void setChatAddress(String chatAddress) {
		this.chatAddress=chatAddress;
	}

	//Restituisce chat del progetto
	public String getChatAddress() {
		return this.chatAddress;
	}
}