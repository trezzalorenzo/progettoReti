package gestioneRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRMIcallback extends Remote {
	public void registerForCallback(String username,IRMIfollowers stub) throws RemoteException;
	public void unregisterForCallback(String username) throws RemoteException;
	
}
