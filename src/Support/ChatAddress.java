package src.Support;



public class ChatAddress {
	
	//classe di supporto per generare gli indirizzi IP multicast utilizzati per le chat dei progetti
	
	private int firstByte;
	private int secondByte;
	private int thirdByte;
	private int fourthByte;
	
	
	public ChatAddress() {
		
		//224.0.0.0 to 239.255.255.255
		firstByte=224;
		secondByte=0;
		thirdByte=0;
		fourthByte=0;
		
	}
	
	
	public String newAddress() {
		
		if(fourthByte<255) {
			fourthByte++;
		}
		else if(thirdByte<255) {
			thirdByte++;
		}
		else if(secondByte<255) {
			secondByte++;
		}
		else if(fourthByte<239){
			firstByte++;
			secondByte=0;
			thirdByte=0;
			fourthByte=0;
		}
		return firstByte + "." + secondByte + "." + thirdByte + "." + fourthByte;
		
		
	}
	
	
	
}
