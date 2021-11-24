package src.Support;


public class CardLog {
	
	private  String listIn;   //lista di partenza
	private  String listOut;  //lista di destinazione
	private  String member;   //membro che ha eseguito spostamento

	public CardLog() {}
	
	public CardLog(String listIn,String listOut,String member) {
		
		this.listIn = listIn;
        this.listOut = listOut;
        this.member=member;
        
	}
	
	public String getlistIn() {
		return this.listIn;
	}

	public String getlistOut() {
		return this.listOut;
	}
	

	public String getmember() {
		return this.member;
	}
	
	


	
	
}
