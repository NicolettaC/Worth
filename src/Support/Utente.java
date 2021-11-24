package src.Support;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize; 
import java.io.Serializable;


@JsonDeserialize(as = Utente.class)
@JsonIgnoreProperties({"status"}) 
public class Utente implements Serializable {

	private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private String status;  //Online o offline, questo viene ignorato nella serializzazione perchè l'informazione è stata considerata superflua
    

    public Utente() {}

    public Utente(String user, String password){
        this.username=user; 
        this.password=password;
        this.status="OFFLINE";
    }

    public void setStatus(String status) throws IllegalArgumentException{
    	if(status.equals("ONLINE")==false && status.equals("OFFLINE")==false) {
    		throw new IllegalArgumentException();
    	}
        this.status=status;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }
    
    public String getStatus(){
        return this.status;
    }

    



}
