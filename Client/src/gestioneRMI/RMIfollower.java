package gestioneRMI;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

public class RMIfollower extends RemoteObject implements IRMIfollowers {
	
	public void notificationEvent(String update) throws RemoteException{
		String follower;
		if(update.startsWith("+")) {
			follower=update.substring(1);
			ClientMain.follower.add(follower);
		}
		else if(update.startsWith("-")) {
			follower=update.substring(1);
			ClientMain.follower.remove(follower);
		}else {
			System.err.println("notifica non valida");
		}
	}
}
