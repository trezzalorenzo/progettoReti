package gestioneRMI;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


public class ClientMain {


	private static int bufSize = 2024;
	private static int portRMI;
	private static String serviceName;
	private static int portTCP;
	private static String nomeServer;
	private static int portCallback;
	public static List<String> follower = Collections.synchronizedList(new ArrayList<String>());
	private static String nomeServizioCallback;
	private static String username;
	private static String multiadd;
	private static int multicastport;

	public static void main(String[] args) throws Exception {
		//	GestioneGson.salvaConfig(new ConfigReader(), "config.json");
		ConfigReader conf = GestioneGson.caricaConfig("config.json");
		portRMI = conf.getPortRMI();
		serviceName = conf.getServiceName();
		portTCP = conf.getPortTCP();
		nomeServer = conf.getNomeServer();
		portCallback = conf.getPortCallback();
		nomeServizioCallback = conf.getNomeServizioCallback();
		// Apro il SocketChannel per la comunicazione con il server.
		SocketChannel sc=null;
		try {
		sc = SocketChannel.open(new InetSocketAddress(nomeServer, portTCP));
		}catch(ConnectException e) {
			System.err.println("server non raggiungibile");
			System.exit(0);
		}
		ByteBuffer buffer = ByteBuffer.allocate(bufSize);
		// ottengo il riferimento al registry
		Registry r = LocateRegistry.getRegistry(portRMI);
		IRMIregistration a = (IRMIregistration) r.lookup(serviceName);
		multiadd = a.getMulticastadd();
		multicastport = a.getPortMulticast();
		MulticastSocket ms=new MulticastSocket(multicastport);
		Thread notifiche = new Thread(new TaskNotifiche(multiadd, ms));
		notifiche.start();
		IRMIfollowers callbackStub=null;
		IRMIcallback server =null;
		IRMIfollowers callbackobj=null;
		InputStreamReader reader=new InputStreamReader(System.in);
		while (true) {
			try {
			System.out.print("< inserisci operazione da eseguire\n> ");
			// leggo i dati dall'utente
			
			BufferedReader bufferRead = new BufferedReader(reader);
			String s = bufferRead.readLine();
			// se è una richiesta di registrazione uso l'RMI
			if (s.contains("register")) {
				String[] tokens = s.split(" ");
				String nome = tokens[1];
				String password = tokens[2];
				ArrayList<String> listaTag = new ArrayList<String>();
				for (int i = 3; i < tokens.length; i++) {
					listaTag.add(tokens[i]);
				}
				try {
					a.registraUtente(nome, password, listaTag);
				} catch (IllegalArgumentException | RemoteException e) {
					System.out.println("< utente già registrato o lista di tag vuota");
				}
				continue;
			} else if (s.contains("login")) {
				if (username != null) {
					System.err.println("< hai già effettuato il login");
					continue;
				}
				String[] tokens = s.split(" ");
				String nome = tokens[1];
				String risposta = doCommand(s, buffer, sc, "");
				// se c'è stato un errore nel login ricomincio il while senza fare chiamate
				// all'RMI
				if (risposta.contains("fallimento")) {
					System.err.print(risposta + "\n");
					continue;
				} else if (risposta.contains("successo")) {
					username = nome;
					Registry callbackregistry = LocateRegistry.getRegistry(portCallback);
					server = (IRMIcallback) callbackregistry.lookup(nomeServizioCallback);
					 callbackobj = new RMIfollower();
					//è wuesta
					callbackStub = (IRMIfollowers) UnicastRemoteObject.exportObject(callbackobj,0);
					server.registerForCallback(username, callbackStub);
					follower = a.inizializzaListaFollower(username);
					System.out.println("login effettuato,followers:");
					for (String i : follower) {
						System.out.print(i);
					}
					System.out.println("");
				}
				// questa operazione viene gestita in locale
			} else if (s.contains("list followers")) {
				for (String string : follower) {
					System.out.print(string + ", ");
				}
				System.out.print("\n");
				// in caso di logout chiudo il client
			} else if (s.contains("logout")) {
				String risposta=doCommand(s, buffer, sc, username);
				if(risposta.contains("successo")){
				break;
				}
				else continue;
			} else if(s.contains("shoutdown")) {
				String risposta=doCommand(s, buffer, sc, username);
				if(risposta.contains("successo")){
				break;
				}
				else continue;
			}
			// altrimenti mando la richiesta al server che sarà eseguita
			else {
				System.out.println(doCommand(s, buffer, sc, username));
			}

		}
			catch(IOException e) {
				System.err.println("server non raggiungibile");
				System.exit(0);
			}
		}
		notifiche.interrupt();
		ms.close();
		notifiche.join();
		try{
			server.unregisterForCallback(username);
			
			UnicastRemoteObject.unexportObject(callbackobj,true);
		}catch (NoSuchObjectException e) {}
		System.out.println("chiuso");
		reader.close();
		
	}

	private static String doCommand(String command, ByteBuffer buffer, SocketChannel sc, String nome)
			throws IOException {
		String s = command + " " + nome;
		byte[] message = s.getBytes();
		buffer.clear();
		buffer.putInt(message.length);
		buffer.put(message);
		buffer.flip();
		sc.write(buffer);
		buffer.clear();
		sc.read(buffer);
		buffer.flip();
		int r = buffer.getInt();
		byte[] reply = new byte[r];
		buffer.get(reply);
		return new String(reply);
	}
}
