package gestioneRMI;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.HashMap;

public class RMIcallback extends RemoteObject implements IRMIcallback {
	//mantendo una hashmap con la chiave che è il nome dell'utente e
	//il valore della stub ad esso associata
	private static final HashMap<String, IRMIfollowers> client=new HashMap<String, IRMIfollowers>();
	
	public void registerForCallback(String username,IRMIfollowers stub) throws RemoteException{
		if(!client.containsKey(username)) {
			client.put(username, stub);
		}
	}
	public void unregisterForCallback(String username) throws RemoteException{
		client.remove(username);
	}
	
	public static void notificaFollow(String username,String aggiornamento) throws RemoteException{
		client.get(username).notificationEvent(aggiornamento);
	}
	
}
