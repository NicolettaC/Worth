package src.Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Registration_interface extends Remote {

    boolean registerUser(String Username,String password) throws RemoteException;

   


}
