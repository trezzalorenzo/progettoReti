package gestioneRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;


public interface IRMIregistration extends Remote {
	public void registraUtente(String nome,String password,ArrayList<String> listaTag) throws IllegalArgumentException,RemoteException;
	
	
	public ArrayList<String> inizializzaListaFollower(String nome) throws IllegalArgumentException,RemoteException;
public String getMulticastadd() throws RemoteException;
	
	public int getPortMulticast() throws RemoteException;
}