package src.Support;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.LinkedList;

public class ProjectChat implements Runnable{

	
	
	private String address;				 //Indirizzo ip multicast
	private int port;					 //porta della socket
	private LinkedList<String> chatMsgs; // messaggi della chat non letti
	private InetAddress ia; 			 //inetAddress
	private MulticastSocket ms;			 //Multicastsocket per messaggi di gruppo multicast
	
	
	private Boolean on;        			     						 // usato per chiudere la chat
	private String CLOSESTRING="Worth2021: message 20201 cl0s3 n0w"; //usato per chiudere la chat 'svegliando' la receive
	
	
	public ProjectChat(String projectAddress) {
		
		this.on=true;
		this.port=4000;
		this.address=projectAddress;  //indirizzo multicast del progetto
		this.chatMsgs=new LinkedList<String>();
		
		try {
			//unisco la chat al gruppo multicast del progetto
			this.ia=InetAddress.getByName(address);
			ms = new MulticastSocket(this.port);
			ms.joinGroup(ia);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void run() {
		
		//byte array per contenere i dati del payload del datagramma
		byte[] data=new byte[128];
		
		DatagramPacket dp = new DatagramPacket(data,data.length);
		
		//finchè non viene chiamato metodo stop() ricevo messaggi nella multicastSocket
		while(on) {
			
			 try {
			//ricevo sulla multicastSocket il pacchetto dp
			 ms.receive(dp);
			 }
			 catch(IOException e) {
				 e.printStackTrace();
			 }
			 
			 //Creo stringa
			 String s= new String(dp.getData(), dp.getOffset(), dp.getLength());
		
			 //se è arrivato messaggio di chiusura non devo aggiungere il messaggio tra i messaggi non letti
			 if(s.equals(CLOSESTRING)==false) {
				 
			  //Inserisco nuovo messaggio tra i messaggi non letti
			  chatMsgs.addLast(s);
			  
			 }
		}
		
		//E' stata richiesta chiusura quindi posso chiudere socket
		try {
		ms.leaveGroup(ia);
		ms.close();
		}
		catch(IOException e) {
			 e.printStackTrace();
		 }
		
		
	}

	public LinkedList<String> readMessages(){
		
		LinkedList<String> temp = new LinkedList<String>();
		
		//Leggo i messaggi non ancora letti cioè quelli contenuti nella list chatMsgs
		for(String mess:chatMsgs) {
			temp.addLast(mess);
			
		}
		//Cancello messaggi
		chatMsgs.clear();
		
		//Restituisco messaggi appena letti
		return temp;
		
	}
	
	public void sendMessage(String message) {
		
		byte[] data= message.getBytes();
		
		DatagramPacket dp = new DatagramPacket(data,data.length,ia,port);
        
		try {
        ms.send(dp);
        }
        catch(IOException e) {
			 e.printStackTrace();
		 }
	}
	
	public void stop() {
		
		//fermo la ricezione dei messaggi
		//essendo la receive bloccante mando un messaggio "particolare" di default per svegliarla
		on=false;
		sendMessage(CLOSESTRING);
		
	}

}
