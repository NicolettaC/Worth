package src.Support;

import java.util.LinkedList;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = Card.class)
public class Card {
	
	private String cardName; 			 //Nome della card
	private String description; 		 //Descrizione della card
	private String currentList; 		 //Lista corrente della card
	private LinkedList<CardLog> cardLog; //History della card data dalle istanze cardLog
		
	public Card() {}

	public Card(String cardName,String description) {
		this.cardName=cardName;
		this.description=description;
		currentList="TODO";
		cardLog= new LinkedList<CardLog>();
	}

	public String getCurrentList(){
		return this.currentList;
	}
	
	public void setCurrentList(String list) {
		currentList=list;
	}
	
	public String getCardName() {
		return this.cardName;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public LinkedList<CardLog> getCardLog(){
		return this.cardLog;
	}
	
	public void addCardLog(CardLog cl) {
		cardLog.addLast(cl);	
	}
	
	public String toString() {
		return "Name: " + cardName + "| Description: " + description + "| Status: " + currentList;
	}
	
	//Restituisce la history della card
	public String ListCardLog(){
		
		 StringBuilder cardsLog = new StringBuilder();
		 
	     for (CardLog cl: cardLog) { 
	    	 cardsLog.append(" >> ");
	    	 cardsLog.append(cl.getlistIn()).append("->");
	    	 cardsLog.append(cl.getlistOut()).append(" ");
	    	 cardsLog.append("from: ");
	    	 cardsLog.append(cl.getmember()).append(" ");
	      }
	    return cardsLog.toString(); 
		
	}
}
