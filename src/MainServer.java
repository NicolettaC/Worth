package src;

import java.io.File;
import java.net.Socket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import src.Interfaces.*;
import src.Rmi.NotifyEventServer;
import src.Rmi.Registration;
import src.Support.*;

public class MainServer {

	private static final int RMIport = 5000; 		  //porta RMI 
	private static final int TCPport = 5001; 	      //porta TCP
	private static final int RMICALLBACKport = 5002;  //porta RMI callback
	private static final String FILENAME_UTENTI = "./src/Backup/utentiregistrati.json"; //Riferimento a file json degli utenti registrati a WORTH

	private static UtentiDB udb ;					  //Database utenti registrati
	private static HashMap<String, Project> projects; //Associazione Nome Progetto-Progetto
	public static ChatAddress chatAddr;				  //Oggetto di tipo chatAddress utilizzato per generare indirizzi IP multicast


	public static void main(String[] args){

		try{   

			//Backup per ripristinare stato sistema
			backup();

			//Operazioni per rmi
			NotifyEventServer nes=startRMI();

			System.out.println("Starting tcp connection");

			//Serversocket utilizzata dal server per mettersi in ascolto su una porta e accettare richieste
			ServerSocket serverM = new ServerSocket();

			//Associo la socket al local host
			serverM.bind(new InetSocketAddress(InetAddress.getLocalHost(), TCPport));		

			//ThreadPool, utilizzo cachedThreadpool per avere elasticità sulla dimensione
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();   

			//In quanto il server è stato visto come un'entità che deve essere sempre reperibile utilizzo un while true
			//l'unico modo per fermare il server è forzatamente.
			while(true) {

				System.out.println("Server waiting for incoming connections");
				Socket socket = serverM.accept();
				System.out.println("Accepted connection");

				//Quando una connessione viene accettata eseguo tramite threadpool un task operations e passo le variabili ad esso
				//da ora in poi il client richiedente connessione si interfaccia con il thread
				executor.execute(new Operations(socket,nes,udb,projects,chatAddr) );

			}


		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	//Ripristina stato sistema dopo riavvio
	private static void backup() throws IOException{

		File FileUtenti = new File(FILENAME_UTENTI);
		projects = new HashMap<String, Project>();
		chatAddr = new ChatAddress();
		
		
		try {
			//Se il file contenente gli utenti registrati non esiste lo creo
			if(FileUtenti.exists()==false || FileUtenti.length()==0) {
				
				FileUtenti.createNewFile();
				
				udb = new UtentiDB();
				
			}
			else {

				
				System.out.println("Starting Backup");

				
				//utilizzo la libreria Jackson per deserializzare i file json
				ObjectMapper mapper = new ObjectMapper(); 

				//converto il file degli utenti registrati a database utenti
				udb =  mapper.readValue(FileUtenti,UtentiDB.class);

				//cambio lo stato di tutti gli utenti in offline
				udb.setOffline();

				//recupero progetti
				File backup = new File("./src/Backup/");

				//Per tutti i file contenuti nella cartella di backup
				for(File currFile : backup.listFiles()) {
					
					//se il file è una directory allora è un progetto
					if(currFile.isDirectory()) {
						
						//recupero nome progetto
						String pjn = currFile.getName();
						
						//creo nuova istanza di progetto
						Project project = new Project(pjn);

						//Per tutti i file contenuti nella cartella del progetto
						for (File file : currFile.listFiles()) {

							//se il file è members.json recupero membri progetto
							if(file.getName().equals("members.json")) {

								@SuppressWarnings("unchecked")
								LinkedList<String> members= mapper.readValue(file,LinkedList.class);

								//inserisco i membri nel progetto tramite una copia 
								project.CopyMembers(members);		

							}
							//se il file è una card recupero informazioni card
							if(file.getName().startsWith("Card_")) {
								
								Card card=mapper.readValue(file,Card.class);

								//inserisco la card nella lista che corrispondeva alla lista corrente
								project.insertInList(card);
								
							}

						}//fine for file contenuti in progetto

						//genero nuovo indirizzo IP multicast e lo setto come indirizzo del progetto
						project.setChatAddress(chatAddr.newAddress());	

						//inserisco progetto cui stato è stato ripristinato all'interno list progetti
						projects.put(pjn,project);	

					}



				}//fine for
				
				System.out.println("Backup completed succesfully");
			  
			}//fine else
			
		}catch (IOException e) { e.printStackTrace(); }

	}

	//RMI per registrazione e callback
	private static NotifyEventServer startRMI() throws Exception{

		System.out.println("Starting rmi connection");

		//Creo registro per RMI
		LocateRegistry.createRegistry(RMIport);

		//Chiedo riferimento ad un registro RMI 			
		Registry register = LocateRegistry.getRegistry(RMIport);

		//Creo istanza dell'oggetto registration
		Registration registration = new Registration(udb);

		//Esporto l'oggetto registration
		Registration_interface stub = (Registration_interface) UnicastRemoteObject.exportObject(registration, 0);

		//Creo collegamento tra nome simbolico REGISTRATION e oggetto appena esportato
		register.rebind("REGISTRATION", stub);

		//Creo registry per RMICALLBACK
		LocateRegistry.createRegistry(RMICALLBACKport);

		//Chiedo riferimento al registro
		Registry registryCallback=LocateRegistry.getRegistry(RMICALLBACKport);

		//RMIcallback
		NotifyEventServer Nes = new NotifyEventServer();

		//Esporto l'oggetto NotifyEventServer
		NotifyServer_interface stubC = (NotifyServer_interface) UnicastRemoteObject.exportObject(Nes, 39000);

		//Creo collegamento tra nome simbolico NOTIFICATION e oggetto appena esportato
		registryCallback.rebind("NOTIFICATION",stubC);

		return Nes;
	}

}
