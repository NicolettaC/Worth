package src;


import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;

import com.fasterxml.jackson.databind.ObjectMapper;

import src.Rmi.NotifyEventServer;
import src.Support.*;

import java.nio.file.*;

public class Operations implements Runnable {

	private Socket socket; 		   //Socket connessione client-server
	private NotifyEventServer nes; //CallbackRMI

	private UtentiDB udb;		   			   //Database utenti registrati
	private HashMap<String, Project> projects; //Associazione NomeProgetto-progetto di tutti i progetti

	private String status;		   //Status=online un utente ha richiesto login ed è andato a buon fine
	private Utente currentUt;      //Utente collegato a questa sessione di login
	private ChatAddress chatAddr;  //Istanza di tipo ChatAddress utilizzata per generare indirizzi IP multicast


	public Operations(Socket socket,NotifyEventServer nes,UtentiDB udb,HashMap<String, Project> projects,ChatAddress chatAddr ) {
		this.socket=socket;
		this.udb=udb;
		this.nes=nes;
		this.status="OFFLINE";
		this.currentUt= new Utente();
		this.chatAddr=chatAddr;
		this.projects=projects;
	}

	public void run() {


		String message=""; 

		try(
				//Preparo buffer per ricevere e inviare messaggi sulla socket
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				){
			while((message=reader.readLine())!=null) {

				//I messaggi hanno formato del tipo comando parametri quindi separo la stringa tramite split
				String[] command=message.split(" "); 

				System.out.println("Received " + message + " request " );

				//Controllo che tipo di richiesta è stata fatta
				if(message.startsWith("login")){
					//due parametri, quindi se command ha lunghezza diversa da 3 o uno dei parametri è vuoto genero errore
					if(command.length!=3) {
						Reply(writer,-8,"login");
					}
					else if(command[1].isEmpty()==true || command[2].isEmpty()==true) {
						Reply(writer,-8,"login");
					}
					else {//Chiamo metodo per eseguire operazione
					Reply(writer,login(command[1],command[2]),command[0]);
					}
				}
				if(message.startsWith("logout")){
					//Chiamo metodo per eseguire operazione
					Reply(writer,logout(),command[0]);
					
				}
				if(message.startsWith("createProject")){
					//un solo parametro, quindi se command ha lunghezza diversa da 2 o uno dei parametri è vuoto genero errore 
					if(command.length!=2) {
						Reply(writer,-8,"createProject");
					}
					else if(command[1].isEmpty()==true) {
						Reply(writer,-8,"createProject");
					}
					else {//Chiamo metodo per eseguire operazione
					Reply(writer,createProject(command[1]),command[0]);
					}
				}
				if(message.startsWith("addMember")){
					//due parametri, quindi se command ha lunghezza diversa da 3 o uno dei parametri è vuoto genero errore
					if(command.length!=3) {
						Reply(writer,-8,"addMember");
					}
					else if(command[1].isEmpty()==true || command[2].isEmpty()==true) {
						Reply(writer,-8,"addMember");
					}
					else {//Chiamo metodo per eseguire operazione
					Reply(writer,addMember(command[1],command[2]),command[0]);
					}
				}
				if(message.startsWith("addCard")){
					//in questo caso il formato messaggio è diverso
					String[] comand=message.split(",");
					//tre parametri, quindi se command ha lunghezza diversa da 4 o uno dei parametri è vuoto genero errore
					if(comand.length!=4) {
						Reply(writer,-8,"addCard");
					}
					else if(comand[1].isEmpty()==true || comand[2].isEmpty()==true || comand[3].isEmpty()==true) {
						Reply(writer,-8,"addCard");
					}
					else {//Chiamo metodo per eseguire operazione
					Reply(writer,addCard(comand[1],comand[2],comand[3]),comand[0]);
					}
				}
				if(message.startsWith("moveCard")){
					//quattro parametri, quindi se command ha lunghezza diversa da 5 o uno dei parametri è vuoto genero errore
					if(command.length!=5) {
						Reply(writer,-8,"moveCard");
					}
					else if(command[1].isEmpty()==true || command[2].isEmpty()==true || command[3].isEmpty()==true || command[4].isEmpty()==true) {
						Reply(writer,-8,"moveCard");
					}
					else {//Chiamo metodo per eseguire operazione
					Reply(writer,moveCard(command[1],command[2],command[3],command[4]),command[0]);
					}
				}
				if(message.startsWith("cancelProject")){
					//un solo parametro, quindi se command ha lunghezza diversa da 2 o uno dei parametri è vuoto genero errore
					if(command.length!=2) {
						Reply(writer,-8,"cancelProject");
					}
					else if(command[1].isEmpty()==true) {
						Reply(writer,-8,"cancelProject");
					}
					else {//Chiamo metodo per eseguire operazione
					Reply(writer,cancelProject(command[1]),command[0]);
					}
				}
				if(message.startsWith("showMembers")){
					//un solo parametro, quindi se command ha lunghezza diversa da 2 o uno dei parametri è vuoto genero errore
					if(command.length!=2) {
						Reply(writer,-8,"showMembers");
					}
					else if(command[1].isEmpty()==true) {
						Reply(writer,-8,"showMembers");
					}
					else {//Chiamo metodo per eseguire operazione
					writer.write(showMembers(command[1]) + "\r\n");
					writer.flush();
					System.out.println("Reply sent");
					}	
				}
				if(message.startsWith("getCardHistory")){
					//due parametri, quindi se command ha lunghezza diversa da 3 o uno dei parametri è vuoto genero errore
					if(command.length!=3) {
						Reply(writer,-8,"getCardHistory");
					}
					else if(command[1].isEmpty()==true || command[2].isEmpty()==true) {
						Reply(writer,-8,"getCardHistory");
					}
					else {//Chiamo metodo per eseguire operazione
					writer.write(getCardHistory(command[1],command[2]) + "\r\n");
					writer.flush();
					System.out.println("Reply sent");}
				}
				if(message.startsWith("listProjects")){
					//Chiamo metodo per eseguire operazione
					writer.write(listProjects() + "\r\n");
					writer.flush();
					System.out.println("Reply sent");
					
				}
				if(command[0].equals("showCards")){
					//un solo parametro, quindi se command ha lunghezza diversa da 2 o uno dei parametri è vuoto genero errore
					if(command.length!=2) {
						Reply(writer,-8,"showCards");
					}
					else if(command[1].isEmpty()==true) {
						Reply(writer,-8,"showCards");
					}
					else {//Chiamo metodo per eseguire operazione
					writer.write(showCards(command[1]));
					writer.flush();
					System.out.println("Reply sent");}
				}
				if(command[0].equals("showCard")){
					//due parametri, quindi se command ha lunghezza diversa da 3 o uno dei parametri è vuoto genero errore
					if(command.length!=3) {
						Reply(writer,-8,"showCard");
					}
					else if(command[1].isEmpty()==true || command[2].isEmpty()==true) {
						Reply(writer,-8,"showCard");
					}
					else {//Chiamo metodo per eseguire operazione
					writer.write(showCard(command[1],command[2]));
					writer.flush();
					System.out.println("Reply sent");}
				}
				if(message.startsWith("getChat")){
					//Chiamo metodo per eseguire operazione
					writer.write(getChat());
					writer.flush();
					System.out.println("Reply sent");
				}
				if(command[0].equals("update")){

					//aggiorna utenti tramite RMI callback
					try {
						nes.update(this.udb);
					}
					catch(RemoteException e){
						System.out.println(e);
					}

				}
				if(command[0].equals("close")) {
					break;
				}


				//non considero il caso in cui il messaggio è diverso da quelli standard perchè considero il caso nel MainClient
				//quindi non riceverò messaggi diversi

			}//fine while
		}//fine try
		catch (SocketException e) {

			//Se utente chiude applicativo forzatamente genera eccezione di tipo SocketException
			//in questo caso gestisco l'uscita chiedendo di cancellare la registrazione al RMICALLBACK e chiedendo logout utente.

			if (e.toString().contains("Socket closed") || e.toString().contains("Connection reset")){

				System.out.println("An existing connection was forcibly closed by one client");

				//Se un utente era online
				if(this.status.equals("ONLINE")){

					try {
						//Disiscrivo da servizio di notifica attraverso l'username dell'utente online ora
						nes.unregisterForCallback(currentUt.getUsername(), null);
					}
					catch(RemoteException ex) {
						ex.printStackTrace();
					}

					//operazione logout
					logout();

				}//fine if su stato utente

			}//fine if su socket

		}catch (IOException e) {
			e.printStackTrace();
		}

		//se arrivo in questo punto il client ha richiesto chiusura connessione quindi chiudo socket
		try {
			socket.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}

	int login(String nickUtente,String password ) {

		//se una sessione di login è già in esecuzione non si può richiedere l'operazione
		if(this.status.equals("ONLINE")) {
			return -1;
		}

		//Gestisco concorrenza su database utenti in modo tale da non avere inconsistenze nel caso di richeste concorrenti
		synchronized(udb){

			//ContainUsername restituisce un riferimento all'utente con nome utente se esso esiste all'interno nel database utenti null altrimenti
			currentUt=udb.containUsername(nickUtente);

			//Se l'username non fa parte del database o la password non è quella associata all'username ritorna errore e non effettua login
			if(currentUt==null || currentUt.getPassword().equals(password)==false) {
				return -2;//l'username non esiste
			}

			//Se l'utente associato all'username è gia online ritorna errore e non effettua login
			if(currentUt.getStatus().equals("ONLINE")) {

				return -1;
			}

			//Cambio stato utente e stato locale 
			this.status="ONLINE";

			currentUt.setStatus("ONLINE");


		}

		//operazine eseguita con successo
		return 1;

	}

	int logout() {

		//Gestisco concorrenza su database utenti in modo tale da non avere inconsistenze nel caso di richeste concorrenti
		synchronized(udb){

			//Cambio stato utente e stato locale 

			this.status="OFFLINE";

			currentUt.setStatus("OFFLINE");

			//Aggiorno gli altri utenti
			try {
				nes.update(udb);
			}
			catch(RemoteException e){
				e.printStackTrace();
			}

		}
		return 1;
	}

	int createProject(String projectName) {

		//prima di poter eseguire l'operazione si deve essere online
		if(this.status.equals("OFFLINE")) {
			return -3;
		}
		
		//Gestisco concorrenza su projects in modo tale da non avere inconsistenze nel caso di richeste concorrenti
		synchronized(projects) {
			
			//Se il progetto non esiste ritorna errore
			if(projects.containsKey(projectName)) {
				return -4;
			}

			//Creo nuova istanza progetto
			Project project = new Project(projectName,currentUt);

			//Genero e assegno indirizzo IP multicast per la chat al progetto
			project.setChatAddress(this.chatAddr.newAddress());

			//Inserisco progetto nell'insieme dei progetti
			projects.put(projectName,project);

			try {
				//Rendo persistente il progetto inserendolo nella cartella Backup come nuova cartella
				Files.createDirectory (Paths.get("./src/Backup/" + projectName ));

				ObjectMapper mapper = new ObjectMapper();

				//Creo all'interno della cartella del nuovo progetto un file con i membri del progetto
				File mbr=new File("./src/Backup/" + projectName + "/members.json"); 

				//Scrivo i membri del progetto nel file members
				mapper.writeValue(mbr,project.showMembers());

			}
			catch(IOException e) {
				e.printStackTrace(); 
			}


		}


		return 1;

	}

	int addMember(String projectName,String nickUtente) {

		if(this.status.equals("OFFLINE")) {
			return -3;
		}
		//Gestisco concorrenza su projects in modo tale da non avere inconsistenze nel caso di richeste concorrenti
		synchronized(projects) {

			//il metodo containUsername restituisce un'istanza utente se il nickUtente è registrato e null se non è registrato
			Utente tempu=udb.containUsername(nickUtente);

			// se l'utente non è registrato o il progetto non esiste l'operazione fallisce
			if(tempu==null || projects.containsKey(projectName)==false) {
				return -5;
			}

			//per aggiungere membri chi fa la richiesta deve essere membro
			if((projects.get(projectName).isMember(currentUt.getUsername()))==false) {
				return -6;
			}

			//aggiungo utente come membro al progetto
			projects.get(projectName).newMember(tempu);

			//serializzo per rendere consistente i membri del progetto
			try {
				ObjectMapper mapper = new ObjectMapper();

				File UR=new File("./src/Backup/" + projectName + "/members.json"); 

				mapper.writeValue(UR,projects.get(projectName).showMembers());
			}
			catch(IOException e) {
				e.printStackTrace(); 
			}

			//aggiorno utente che è stato aggiunto come membro del progetto
			try {
				nes.updateMember(nickUtente, projectName,projects.get(projectName).getChatAddress());
			}
			catch(RemoteException ex) {
				ex.printStackTrace();
			}

		}

		return 1;
	}

	int addCard(String projectName,String cardName,String descrizione) {
		if(this.status.equals("OFFLINE")) {
			return -3;
		}
		
		//Gestisco concorrenza su projects in modo tale da non avere inconsistenze nel caso di richeste concorrenti
		synchronized(projects) {
			// se il progetto non esiste l'operazione fallisce
			if(projects.containsKey(projectName)==false) {
				return -5;
			}
			//per aggiungere cards al progetto si deve essere membri
			if((projects.get(projectName).isMember(currentUt.getUsername()))==false) {
				return -6;
			}
			//la card deve avere un nome univoco, quindi se già esiste l'operazione fallisce
			if(projects.get(projectName).cardExist(cardName)) {
				return -4;
			}

			//Aggiungo nuova card al progetto, questo metodo restituisce istanza card se è andato a buon fine, null altrimenti
			Card card=projects.get(projectName).newCard(cardName, descrizione);

			if(card==null) {
				return -5;
			}

			//Modifico la story della card creando nuovo log e aggiungendolo alla card
			CardLog cl=new CardLog("-","TODO",currentUt.getUsername());

			card.addCardLog(cl);

			//serializzo per rendere persistente card
			try {

				ObjectMapper mapper = new ObjectMapper();

				File UR=new File("./src/Backup/" + projectName +"/"+ "Card_"+cardName +".json"); 

				mapper.writeValue(UR,card);

			}
			catch(IOException e) {
				e.printStackTrace(); 
			}

		}
		return 1;

	}

	int moveCard(String projectName,String cardName,String listIn, String listOut) {

		if(this.status.equals("OFFLINE")) {
			return -3;
		}
		//Gestisco concorrenza su projects in modo tale da non avere inconsistenze nel caso di richeste concorrenti
		synchronized(projects) {
			//se il progetto non esiste l'operazione fallisce
			if(projects.containsKey(projectName)==false) {
				return -5;
			}
			//per muovere cards del progetto si deve essere membri
			if((projects.get(projectName).isMember(currentUt.getUsername()))==false) {
				return -6;
			}

			//se la card non esiste l'operazione fallisce
			if(projects.get(projectName).cardExist(cardName)==false) {
				return -5;
			}
			//se non si può fare questo movimento l'operazione fallisce
			if(projects.get(projectName).validMove(cardName,listIn, listOut)==false) {
				return -7;
			}

			//muovo card
			projects.get(projectName).moveCard(cardName, listIn, listOut);
			
			//aggiorno log movimenti card
			
			Card card= projects.get(projectName).getCard(cardName);

			CardLog cl=new CardLog(listIn,listOut,currentUt.getUsername());

			card.addCardLog(cl);

			//Serializzo per rendere persistenti modifiche
			try {

				ObjectMapper mapper = new ObjectMapper();

				File UR=new File("./src/Backup/" + projectName +"/"+ "Card_"+cardName +".json"); 



				mapper.writeValue(UR,card);

			}
			catch(IOException e) {
				e.printStackTrace(); 
			}
		}



		return 1;
	}

	String listProjects() {

		//prima di poter eseguire l'operazione si deve essere online
		if(this.status.equals("OFFLINE")) {
			return "Operation listProjects failed, need to be logged in" + "\r\n";
		}

		String reply = new String();
		
		//Gestisco concorrenza su projects in modo tale da non avere inconsistenze nel caso di richeste concorrenti
		synchronized(projects){

			//Per ogni progetto se utente è membro aggiungo progetto a risposta
			for (Project project : projects.values()) {
	
					if(project.isMember(currentUt.getUsername())) {
						
						reply=reply +  project.getName() + " ";
						
					}

				}  
			}

		
		return reply;

	}

	String showMembers(String projectName) {

		//prima di poter eseguire l'operazione si deve essere online
		if(this.status.equals("OFFLINE")) {
			return "Operation showMembers failed, need to be logged in" + "\r\n";
		}

		String reply = new String();
		
		//Gestisco concorrenza su projects in modo tale da non avere inconsistenze nel caso di richeste concorrenti
		synchronized(projects){ 

			if(projects.containsKey(projectName)==false) {
				return "Operation showMembers failed, this project doesn't exist" + "\r\n";
			}
			//per mostrare membri del progetto si deve essere membri
			if((projects.get(projectName).isMember(currentUt.getUsername()))==false) {
				return "Operation showMembers failed, you don't have the permission" + "\r\n";
			}
		
			reply=projects.get(projectName).listMembers();
			
		}

		return reply;

	}

	String showCards(String projectName) {

		//prima di poter eseguire l'operazione si deve essere online
		if(this.status.equals("OFFLINE")) {
			return "Operation showCards failed, need to be logged in" + "\r\n";
		}
		String reply = new String();
		
		//Gestisco concorrenza su projects in modo tale da non avere inconsistenze nel caso di richeste concorrenti
		synchronized(projects){ 

			if(projects.containsKey(projectName)==false) {
				return "Operation showCards failed, this project doesn't exist" + "\r\n";
			}
			if((projects.get(projectName).isMember(currentUt.getUsername()))==false) {
				return "Operation showCards failed, you don't have the permission" + "\r\n";
			}

			reply = projects.get(projectName).listCards() +"\r\n";
		}
		return reply;

	}

	String showCard(String projectName,String cardName) {

		//prima di poter eseguire l'operazione si deve essere online
		if(this.status.equals("OFFLINE")) {
			return "Operation showCard failed, need to be logged in" + "\r\n";
		}
		String reply = new String();

		//Gestisco concorrenza su projects in modo tale da non avere inconsistenze nel caso di richeste concorrenti
		synchronized(projects){ 

			// se il progetto non esiste l'operazione fallisce
			if(projects.containsKey(projectName)==false) {
				return "Operation showCard failed" + "\r\n";
			}
			//per recuperare le informazioni della card si deve essere membri
			if((projects.get(projectName).isMember(currentUt.getUsername()))==false) {
				return "Operation showCard failed, you don't have the permission" + "\r\n";
			}
			//se la card non esiste l'operazione fallisce
			if(projects.get(projectName).cardExist(cardName)==false) {
				return "Operation showCard failed" + "\r\n";
			}

			//Chiedo istanza card
			Card card= projects.get(projectName).getCard(cardName);
			
			reply = card.toString() +"\r\n";
			
		}
		return reply;


	}

	String getCardHistory(String projectName,String cardName) {
		
		//prima di poter eseguire l'operazione si deve essere online
		if(this.status.equals("OFFLINE")) {
			return "Operation getCardHistory failed, need to be logged in" + "\r\n";
		}
		Card card=null;
		synchronized(projects){ 
		// se il progetto non esiste l'operazione fallisce
		if(projects.containsKey(projectName)==false) {
			return "Operation getCardHistory failed";
		}
		// si deve essere membri
		if((projects.get(projectName).isMember(currentUt.getUsername()))==false) {
			return "Operation getCardHistory failed, you don't have the permission" + "\r\n";
		}
		//se la card non esiste l'operazione fallisce
		if(projects.get(projectName).cardExist(cardName)==false) {
			return "Operation getCardHistory failed";
		}

		//Prendo istanza card
		card= projects.get(projectName).getCard(cardName);
		}
		return card.ListCardLog();
	}

	int cancelProject(String projectName) {
		
		//prima di poter eseguire l'operazione si deve essere online
		if(this.status.equals("OFFLINE")) {
			return -3;
		}

		//Gestisco concorrenza su projects in modo tale da non avere inconsistenze nel caso di richeste concorrenti
		synchronized(projects) {
			// se il progetto non esiste l'operazione fallisce
			if(projects.containsKey(projectName)==false) {
				return -5;
			}
			//per eliminare il progetto si deve essere membri 
			if((projects.get(projectName).isMember(currentUt.getUsername()))==false) {
				return -6;
			}
			//per eliminare il progetto le cards devono essere nella lista DONE
			if(projects.get(projectName).isDone()==false) {
				return -6;
			}

			//Prendo lista membri per poi aggiornarli dell'avvenuta cancellazione
			LinkedList<String> members = projects.get(projectName).showMembers();

			//rimuovo progetto da lista progetti
			projects.remove(projectName);

			File projectFile = new File("./src/Backup/" + projectName);


			
			for(File currFile:projectFile.listFiles()) {
				//controllo che l'eliminazione sia andata a buon fine
				if(currFile.delete()==false) {
					return -5;
				}
			}
			
			//controllo che l'eliminazione sia andata a buon fine
			if(projectFile.delete()==false) {
				return -5;
			}

			try {
				//se tutto è andato a buon fine posso aggiornare i membri dell'avvenuta cancellazione
				nes.updateMembers(members, projectName);
			}
			catch(RemoteException e){
				e.printStackTrace();
			}


		}

		return 1;
		
	}

	String getChat() {
		//Restituisco gli indirizzi multicast dei progetti
		String reply = new String();
		synchronized(projects){ 
		for(Project pj : projects.values()) {
			if(pj.isMember(currentUt.getUsername())) {
				reply=reply + pj.getName() + " " + pj.getChatAddress() + ",";
			}
		}
		}
		return reply + "\r\n";


	}

	//Risposte standard individuate dal parametro result
	void Reply(BufferedWriter writer, int result, String operation) throws IOException{

		switch(result) {

		case 1:
			writer.write("Operation "+ operation + " done succesfully" +"\r\n");
			writer.flush();
			System.out.println("Reply sent");
			break;
		case -1:
			writer.write("Operation " + operation + " failed, a login session is currently active" + "\r\n");
			writer.flush();
			System.out.println("Reply sent");
			break;
		case -2:
			writer.write("Operation " + operation + " failed, username or password not correct " + "\r\n");
			writer.flush();
			System.out.println("Reply sent");
			break;
		case -3:
			writer.write("Operation " + operation + " failed,you need to be logged in" + "\r\n");
			writer.flush();
			System.out.println("Reply sent");
			break;
		case -4:
			writer.write("Operation " + operation + " failed, already exists " + "\r\n");
			writer.flush();
			System.out.println("Reply sent");
			break;
		case -5:
			writer.write("Operation " + operation + " failed " + "\r\n");
			writer.flush();
			System.out.println("Reply sent");
			break;
		case -6:
			writer.write("Operation " + operation + " failed, you don't have the permission" + "\r\n");
			writer.flush();
			System.out.println("Reply sent");
			break;
		case -7:
			writer.write("Operation " + operation + " failed, this move is not permitted" + "\r\n");
			writer.flush();
			System.out.println("Reply sent");
			break;
		case -8:
			writer.write("Operation " + operation + " failed, field empty" + "\r\n");
			writer.flush();
			System.out.println("Reply sent");
			break;
		default:
			break;

		}

	}



}
