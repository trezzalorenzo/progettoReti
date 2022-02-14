package gestioneRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRMIfollowers extends Remote {
	public void notificationEvent(String update) throws RemoteException;
}