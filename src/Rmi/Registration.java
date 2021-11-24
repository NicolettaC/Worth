package src.Rmi;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import com.fasterxml.jackson.databind.ObjectMapper;


import src.Interfaces.Registration_interface;
import src.Support.Utente;
import src.Support.UtentiDB;

public class Registration implements Registration_interface{

   
    private UtentiDB udb ;
    private static final String FILENAME_UTENTI = "./src/Backup/utentiregistrati.json";
  
    

    public Registration(UtentiDB udb) throws RemoteException {
        
    	super();
        this.udb=udb;
      
    }



    public boolean registerUser(String username,String password) throws RemoteException {

        
        if(username==null || password==null ){
            throw new NullPointerException("Valori non validi");
        }

        System.out.println("Received registration request");

        Utente newU = new Utente(username,password);
        
        if(username.isEmpty()==true || password.isEmpty()==true || udb.isRegistered(newU)==true ){
            // se i parametri in input sono vuoti allora non si può registrare utente
        	// se l'utente è già registrato non si può registrare di nuovo
            return false;
        }
        else{
         this.udb.addUtente(newU);
         
         //aggiorno file di backup 
         try { 
        	 
        	 ObjectMapper mapper = new ObjectMapper();
             
             File UR=new File(FILENAME_UTENTI); 
             
             mapper.writeValue(UR,udb);
 
         }
         catch (IOException e) { 
             e.printStackTrace(); 
         } 
 

         //registrazione avvenuta con successo
          return true;
        }

    }



  }

