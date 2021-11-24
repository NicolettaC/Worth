package src;

import java.io.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.HashMap;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import src.Support.ProjectChat;
import src.Support.UtentiDB;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import src.Interfaces.*;
import src.Rmi.NotifyEventClient;


public class MainClient{

    
    private static final int RMIport = 5000; 		  //porta RMI 
    private static final int TCPport = 5001; 		  //porta TCP per connessione
    private static final int RMICALLBACKport = 5002;  //porta per callback rmi

    private static HashMap<String,String> projectAddress ;  //nome progetto, indirizzo multicast del progetto
    private static HashMap<String,ProjectChat> pjChat;		//nome progetto, chat del progetto
    
    private static UtentiDB udb;		//Database utenti registrati
    private static String crUsername;	//Username dell'utente loggato
    
    
    
    public static void main(String[] args) throws Exception {
       
        System.out.println("----------Welcome in WORTH----------");
        
        
        //inizializzo variabili
        udb = new UtentiDB();
        projectAddress=new HashMap<String,String>();
        pjChat = new HashMap<String,ProjectChat>();
        String command = null;  			//stringa contenente comando in input
        boolean close = false; 				//se è false l'applicativo rimane in esecuzione
        Socket socketTCP = new Socket();	//socket usata per connettersi al server
        
        //preparo buffer per lettura da linea di comando
        try (BufferedReader in  = new BufferedReader(new InputStreamReader(System.in))){

        	
        	
        	//connessione ad RMI,RMIcallback e TCP
        	
        	//RMI 
        
            Registry r = LocateRegistry.getRegistry(RMIport);
            Registration_interface remoteRegistration = (Registration_interface) r.lookup("REGISTRATION");

            //RMIcallback
            Registry registry = LocateRegistry.getRegistry(RMICALLBACKport);
            NotifyServer_interface server = (NotifyServer_interface) registry.lookup("NOTIFICATION");
            
            NotifyClient_interface callbackObj = new NotifyEventClient(udb,projectAddress,pjChat);
            NotifyClient_interface stub = (NotifyClient_interface) UnicastRemoteObject.exportObject(callbackObj, 0);
            
      
            //Connessione TCP
            socketTCP.connect(new InetSocketAddress(InetAddress.getLocalHost(),TCPport));
            
            //buffer per lettura e scrittura 
            BufferedReader reader = new BufferedReader(new InputStreamReader(socketTCP.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socketTCP.getOutputStream()));
            

            while(close==false){
                
                System.out.println("> Insert command: ");

                command = in.readLine();
                
                if(command.equals("login")){
                	Cllogin(server,stub,in,writer,reader);
                }
                else if(command.equals("register")){
                    Clregistration(in,writer,remoteRegistration);
                }
                else if (command.equals("listUsers")) { 
                    //Se è false si richiedono gli utenti registrati 
                	ClListUsers(false);
                }
                else if (command.equals("listOnlineUsers")) { 
                	//Se è true si richiedono gli utenti registrati e online
                	ClListUsers(true);
                }
                else if(command.equals("createProject")) {
                	ClCreateProject(in,writer,reader);
                }
                else if(command.equals("addMember")) {
                	ClAddMember(in,writer,reader);
                }
                else if(command.equals("showMembers")) {
                	ClshowMembers(in,writer,reader);
                }
                else if(command.equals("showCards")) {
                	ClshowCards(in,writer,reader);
                }
                else if(command.equals("showCard")) {
                	ClshowCard(in,writer,reader);
                }
                else if(command.equals("addCard")) {
                	CladdCard(in,writer,reader);
                }
                else if(command.equals("moveCard")) {
                	ClmoveCard(in,writer,reader);
                }
                else if(command.equals("getCardHistory")) {
                	ClgetCardHistory(in,writer,reader);
                }
                else if(command.equals("cancelProject")) {
                	ClcancelProject(in,writer,reader);  	
                }
                else if(command.equals("help")) {
                	ClHelp();
                }
                else if(command.equals("listProjects")) {	
                	CllistProjects(in,writer,reader);
                }
                else if(command.equals("readChat")) {
                	if(crUsername!=null) {
                	ClreadChat(in,writer,reader);
                	}
                	else {
                		System.out.println("failed, you need to be logged in");
                	}
                }
                else if(command.equals("sendChatmsg")) {
                	if(crUsername!=null) {
                	ClsendChatMsg(in,writer,reader);
                	}
                	else {
                		System.out.println("failed, you need to be logged in");
                	}
                }
                else if(command.equals("logout")){
                	
                	writer.write("logout" + "\r\n");
                    writer.flush();
                    
                    String message=reader.readLine();
                    System.out.println(message);
                    
                    if(message.startsWith("Operation logout done succesfully")) {

                         server.unregisterForCallback(crUsername,stub); 
                         crUsername= null;
                         closeChats();
                         
                    }
                }
                else if(command.equals("close")) {
                	
                	//se l'utente chiude con close prima di aver fatto logout chiedo logout
                	if(crUsername!=null) {
                		writer.write("logout" + "\r\n");
                        writer.flush();
                        
                        String message=reader.readLine();
                        System.out.println(message);
                        
                        if(message.startsWith("Operation logout done succesfully")) {

                             server.unregisterForCallback(crUsername,stub); 
                             crUsername= null;
                             closeChats();
                             
                        }
                	}
                	
                	writer.write("close" + "\r\n");
                	writer.flush();
                	
                	closeChats();
                	close=true;
                	
                } 
                else{
                	//l'utente ha inserito un comando non riconosciuto
                    System.out.println(command +" : command not found ");
                    System.out.println("Write help to list commands! ");
                }
            }
            
            UnicastRemoteObject.unexportObject(callbackObj, false);
          
            socketTCP.close();
        }
        catch(Exception e){
            System.out.println(e);
        }

        System.out.println("Bye Bye!");

    }
    

    public static void Cllogin(NotifyServer_interface server, NotifyClient_interface stub, BufferedReader in, BufferedWriter writer,BufferedReader reader) throws RemoteException, IOException{
    	
    	 System.out.print("> Insert username: ");
         String username = in.readLine();
         System.out.print("> Insert password: ");
         String password = in.readLine();
         
      
         writer.write("login" + " " + username + " " + password + "\r\n");
         writer.flush();
         
         String message;
         message=reader.readLine();
        
		 System.out.println(message);
		 
		//se l'operazione di login è andata a buon fine posso procedere con la registrazione al servizio di callback e della chat
		 if(message.equals("Operation login done succesfully")) {
			 
			 //registra utente a callback
			 server.registerForCallback(username,stub);
			 
			 //chiede al main server di aggiornare gli utenti sull'avvenuto login
			 writer.write("update" + "\r\n");
			 
	         writer.flush();

	         //chiede al main server gli indirizzi IP dei progetti di cui è membro
			 ClgetChat(in,writer,reader);
			 
			 //si unisce alle chat dei progetti
			 Cljoinchats();
			 
			 //setto come utente corrente l'username dell'utente che ha eseguito login con successo
			 crUsername=username;
		 }
	
    }
   
    public static void Clregistration(BufferedReader in,BufferedWriter writer,Registration_interface remoteRegistration) throws RemoteException, IOException{

        System.out.print("> Insert new username: ");
        String username = in.readLine();
        System.out.print("> Insert password: ");
        String password = in.readLine();
        
        

        //registra utente tramite RMI
        if(remoteRegistration.registerUser(username,password)){
        	
            System.out.println("User registered succesfully");
            
       	 	//chiede al main server di aggiornare gli utenti sull'avvenuto login
			 writer.write("update" + "\r\n");
			 
	         writer.flush();
        }
        else{
            System.out.println("registration failed");
        }

    }

    public static void ClListUsers(Boolean online) {
    	
    	//se crUsername è null nessun utente è online
    	if(crUsername==null) {
    		System.out.println("Operation failed,you need to be logged in");
    		return;
    	}
    
    	
    	//se online=true si sta richiedendo la lista degli utenti online 
    	//altrimenti la lista degli utenti registrati
    	if(online) {
    		//chiamo il metodo di common.UtentiDB per stampare gli utenti online
    		System.out.println(udb.ListOnlineUsers());
    	}
    	else {
    		//chiamo il metodo di common.UtentiDB per stampare gli utenti registrati
    		System.out.println(udb.ListUsers());
    	}	
    
    }
   
    public static void ClAddMember(BufferedReader in, BufferedWriter writer,BufferedReader reader) throws RemoteException, IOException{
    	
    	System.out.print("> Insert project name: ");
         String pjn = in.readLine();
         System.out.print("> Insert username of new member: ");
         String username = in.readLine();
     	
       //faccio richiesta al main server di aggiungere membro al progetto
         writer.write("addMember" + " " + pjn + " " + username + "\r\n");
         writer.flush();
         
         String message;
         message=reader.readLine();
        
		 System.out.println(message);
			
    }
    
    public static void ClshowMembers(BufferedReader in, BufferedWriter writer,BufferedReader reader) throws RemoteException, IOException{
    	 System.out.print("> Insert project name: ");
         String pjn = in.readLine();
         
       
       //faccio richiesta al main server di mostrare i membri del progetto
         writer.write("showMembers" + " " + pjn + "\r\n");
         writer.flush();
         
         String message;
         message=reader.readLine();
        
		 System.out.println(message);
         
    }
    
    public static void ClshowCards(BufferedReader in, BufferedWriter writer,BufferedReader reader) throws RemoteException, IOException{
   	 System.out.print("> Insert project name: ");
        String pjn = in.readLine();
      
       //faccio richiesta al main server di mostrare le cards di un progetto
        writer.write("showCards" + " " + pjn + "\r\n");
        writer.flush();
        
        String message;
        message=reader.readLine();
       
		System.out.println(message);
        
   }

    public static void ClshowCard(BufferedReader in, BufferedWriter writer,BufferedReader reader) throws RemoteException, IOException{
    	System.out.print("> Insert project name: ");
   	 	String pjn = in.readLine();
        System.out.print("> Insert card name: ");
        String cardName = in.readLine();
      
        
      //faccio richiesta al main server di mostrare le informazioni di una card
        writer.write("showCard" + " " + pjn + " " + cardName + "\r\n");
        writer.flush();
        
        String message;
        message=reader.readLine();
       
		System.out.println(message);
        
   }
    
    public static void ClCreateProject(BufferedReader in, BufferedWriter writer,BufferedReader reader) throws RemoteException, IOException {
    	
    	System.out.print("> Insert project name: ");
        String projectName = in.readLine();
  
        
       //faccio richiesta al main server di creare un nuovo progetto
        writer.write("createProject" + " " + projectName + "\r\n");
        writer.flush();
        
        String message;
        message=reader.readLine();
       
		System.out.println(message);
		
		//se l'operazione è andata a buon fine richiedo indirizzi IP associati ai progetti e associo una chat
		if(message.equals("Operation createProject done succesfully")) {

         	ClgetChat(in,writer,reader);
         	
         	String address =  projectAddress.get(projectName);
         	
         	//creo nuova chat per progetto appena creato
         	ProjectChat pjc = new ProjectChat(address);
         	
         	//eseguo thread chat
			new Thread(pjc).start();
			
			//aggiorno associazione nomeprogetto,chat
			pjChat.put(projectName, pjc);
         	
		 }
	
			
    }
    
    public static void CladdCard(BufferedReader in, BufferedWriter writer,BufferedReader reader) throws RemoteException, IOException {
    	
    	System.out.print("> Insert project name: ");
        String projectName = in.readLine();
        System.out.print("> Insert card name: ");
        String cardName = in.readLine();
        System.out.print("> Insert card description: ");
        String cardDescription = in.readLine();
        
     
        
       //faccio richiesta al main server di aggiungere la card al progetto
        writer.write("addCard" + "," + projectName + "," + cardName + "," + cardDescription  + "\r\n");
        writer.flush();
        
        String message;
        message=reader.readLine();
       
		System.out.println(message);
			
    }
    
    public static void ClmoveCard(BufferedReader in, BufferedWriter writer,BufferedReader reader) throws RemoteException, IOException {
    	
    	System.out.print("> Insert project name: ");
        String projectName = in.readLine();
        System.out.print("> Insert card name: ");
        String cardName = in.readLine();
        System.out.print("> Insert list: ");
        String list = in.readLine();
        System.out.print("> Insert destination list: ");
        String destination = in.readLine();
        
     
        //faccio richiesta al main server di muovere una card
        writer.write("moveCard" + " " + projectName + " " + cardName + " " + list  + " " + destination  +"\r\n");
        writer.flush();
        
        String message;
        message=reader.readLine();
        
        //se operazione è andata a buon fine posso mandare in chat l'aggiornamento
       if(message.startsWith("Operation moveCard done succesfully")){
        
 
        ProjectChat pjc = pjChat.get(projectName);
  
        
    	pjc.sendMessage("WORTH: " + crUsername + " moved "+cardName+" from "+ list + " to " + destination );
       }
        
		System.out.println(message);
			
    }

    public static void ClgetCardHistory(BufferedReader in, BufferedWriter writer,BufferedReader reader) throws RemoteException, IOException {
    	
    	System.out.print("> Insert project name: ");
        String projectName = in.readLine();
        System.out.print("> Insert card name: ");
        String cardName = in.readLine();
  
      
      //faccio richiesta al main server di ricevere la storia delle card
        writer.write("getCardHistory" + " " + projectName + " " + cardName + "\r\n");
        writer.flush();
        
        String message;
        message=reader.readLine();
       
		System.out.println(message);
			
    }
   
    public static void ClcancelProject(BufferedReader in, BufferedWriter writer,BufferedReader reader) throws RemoteException, IOException {
    	System.out.print("> Insert project name: ");
        String projectName = in.readLine();
    
      //faccio richiesta al main server di cancellare progetto
        writer.write("cancelProject" + " " + projectName  + "\r\n");
        writer.flush();
        
        String message;
        message=reader.readLine();
       
		System.out.println(message);
		
		
    
    }

    public static void CllistProjects(BufferedReader in, BufferedWriter writer,BufferedReader reader) throws RemoteException, IOException {
    	
    	//faccio richiesta al main server di mostrare progetti dell'utente
    	writer.write("listProjects" + "\r\n");
    	writer.flush();
    	
    	 String message;
         message=reader.readLine();
        
 		System.out.println(message);
 		
 		
 	
    }

    //METODI RIGUARDANTI LE CHAT
    
    public static void ClreadChat(BufferedReader in, BufferedWriter writer,BufferedReader reader) throws RemoteException, IOException{
    	
    	System.out.print("> Insert project name: ");
    	String projectName = in.readLine();
    	
    	if(projectName.isEmpty()==true ) {
        	System.out.println("Failed, field empty");
        	return;
        }
    	
    	//Se il progetto è contenuto nell'associazione nome progetto-chat posso procedere con la chiamata del metodo readMessages
    	if(pjChat.containsKey(projectName)) {
    	
    		//Recupero riferimento alla chat del progetto con nome richiesto 
    		ProjectChat pjc = pjChat.get(projectName);
    		
    		//Stampo messaggi non letti
			System.out.println(pjc.readMessages());
			
		}
    	else {
    		System.out.println("failed, you are not member of this project");
    	}
    }
   
    public static void ClsendChatMsg(BufferedReader in, BufferedWriter writer,BufferedReader reader) throws RemoteException, IOException{
    	
    	System.out.print("> Insert project name: ");
        String projectName = in.readLine();
        System.out.print("> Insert message: ");
        String message= in.readLine();
      
        if(projectName.isEmpty()==true || message.isEmpty()==true) {
        	System.out.println("Failed, field empty");
        	return;
        }
        message = crUsername + " sent: " + message;
        
      //Se il progetto è contenuto nell'associazione nome progetto-chat posso procedere con la chiamata del metodo sendMessage 
        if(pjChat.containsKey(projectName)) {
        	
        	//Recupero riferimento alla chat del progetto con nome richiesto 
 			ProjectChat pjc = pjChat.get(projectName);
 		        	
 			//Invio messaggio
 		    pjc.sendMessage(message);
 		        
 		    System.out.println("message delivered");
 		     
 		}
        else {
        	
           System.out.println("failed, you are not member of this project");
        
        }
     }

    public static void Cljoinchats() {
    	
    	// metodo utilizzato per creare i thread di tutte le chat contenute nell'associazione nome progetto-indirizzo
    	for(String projectName : projectAddress.keySet()) {
    		
    		String address =  projectAddress.get(projectName);
    		
    		ProjectChat pjc = new ProjectChat(address);
    		
    		new Thread(pjc).start();
    		
    		pjChat.put(projectName, pjc);

    	}
    	
    }
    
    public static void ClgetChat(BufferedReader in, BufferedWriter writer,BufferedReader reader) throws RemoteException, IOException{
    	
    	// metodo utilizzato per richiedere al main server gli indirizzi associati ai progetti di cui utente è membro
    	
    	writer.write("getChat"  + "\r\n");
    	writer.flush();
        
        String message;
        message=reader.readLine();
        
       
        //formato messaggio = nomeProgetto Indirizzo,NomeProgetto Indirizzo...
        String[] messages = message.split(",");
        
        for(String msg : messages) {
        	
        	if(msg.isEmpty()==false) {
        		
        	String[] addrPrj = msg.split(" ");
        	
        	projectAddress.put(addrPrj[0],addrPrj[1]);
        	
        	}
        	
        }
	
    }
    
    public static void closeChats() {

    	//metodo utilizzato per chiudere tutti i thread chat
    	for(ProjectChat chat: pjChat.values()) {
    		chat.stop(); 

    	}
    	
    	pjChat.clear();
    	
		projectAddress.clear();	
    }
    
    public static void ClHelp() {
    	 System.out.println("DESCRIPTION: ");
    	
    	 System.out.println("\nWorth is a tool for managing collaborative projects\n");
    	 
      	
      	 
      	 System.out.println("COMMANDS: ");
      	 System.out.println("\nWorth command syntax, all the commands are case sensitive, the command's parameters will be asked after \n");
      	 System.out.println("register - Used to register a new user");
      	 System.out.println("login - Used to login");
      	 System.out.println("logout - Used to logout");
      	 System.out.println("listUsers - Used to list all users registered in WORTH");
      	 System.out.println("listOnlineUsers -  Used to list all users currently online");
      	 System.out.println("listProjects -  Used to list current user's projects");
      	 System.out.println("createProject - Used to create a new project");
      	 System.out.println("addMember - Used to add a new member to a project");
      	 System.out.println("showMembers - Used to show project's members ");
      	 System.out.println("showCards Used to show project's cards");
      	 System.out.println("showCard - Used to list a card Info");
      	 System.out.println("addCard - Used to add a new card to a project");
      	 System.out.println("moveCard - Used to move a card from one list to another LISTS:[TODO,INPROGRESS,TOBEREVISED,DONE]");
      	 System.out.println("getCardHistory - Used to show a card movements history");
      	 System.out.println("cancelProject - Used to delete a project");
      	 System.out.println("sendChatmsg - Used to send a new message in the chat");
      	 System.out.println("readChat - Used to read messages in the chat");
      	 System.out.println("close - Used to close the application");
      	 System.out.println("---------------------------------------------------------");
      }

    	
}