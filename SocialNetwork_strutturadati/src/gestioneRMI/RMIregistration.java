package gestioneRMI;

import java.rmi.RemoteException;
import java.util.ArrayList;

import eccezioni.ElementNotPresentException;
import server.ServerMain;

public class RMIregistration implements IRMIregistration {
	public RMIregistration() {

	}

	public void registraUtente(String nome, String password, ArrayList<String> listaTag)
			throws IllegalArgumentException, RemoteException {
		try {
			ServerMain.hash.register(nome, password, listaTag);
		} catch (IllegalArgumentException e) {
			// rilancio l'eccezione per evitare che il client venga a conoscenza
			// dell'errorstack trace del server
			throw new IllegalArgumentException();
		}

	}

	public ArrayList<String> inizializzaListaFollower(String nome) throws IllegalArgumentException, RemoteException {
		ArrayList<String> returner;
		try {
			returner = ServerMain.hash.utenteDaNome(nome).getFollower();
		} catch (ElementNotPresentException | IllegalArgumentException e) {
			throw new IllegalArgumentException();
		}
		return returner;
	}
	public String getMulticastadd() throws RemoteException{
		return ServerMain.getAddressMulti();
	}
	public int getPortMulticast() throws RemoteException{
		return ServerMain.getPortMulticast();
	}
}
