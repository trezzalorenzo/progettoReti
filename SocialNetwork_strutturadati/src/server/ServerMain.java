package server;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.TimeUnit;

import dataStructures.StrutturaDatiServer;
import eccezioni.ElementNotPresentException;
import gestioneRMI.IRMIcallback;
import gestioneRMI.IRMIregistration;
import gestioneRMI.RMIcallback;
import gestioneRMI.RMIregistration;
import utilities.ConfigReader;
import utilities.GestioneGson;

public class ServerMain {
	// tempo massimo per la chiusura del pool di thread.
	private static int secondiAttesaChiusura;
	// porta relativa al registro
	private static int portRegistryRMI;
	// nome del servizio dell'RMI dedicato alla registrazione
	private static String serviceName;
	// nome del servizio dell'RMI dedicato alla callback
	private static String serviceCallbackReg;
	// nome del file json su cui salvare e da cui leggere i dati dal server
	private static String fileJson;
	// numero di porta su cui aprire la connessione TCP
	private static int portaServer;
	// numero di porta per la connessione UDP
	private static int portaMulticast;
	// indirizzo per il multicast
	private static String indirizzoMulticast;
	// percentuale da attribuire all'autore del post
	private static int percentualeAutore;
	// millisecondi tra ogni conteggio dei premi
	private static int msecondiProssimoConteggioReward;
	// millisecondi tra ogni salvataggio dei dati
	private static int msecondiProssimoSalvataggio = 3000;

	// Dimensione del buffer di risposta.
	public static final int bufSize = 2048;
	// valore per chiudere il server
	public volatile static Boolean chiudi = false;
	// struttura relativa al server
	public static StrutturaDatiServer hash;
	// lista per memorizzare gli utenti che hanno fatto il login
	public static List<String> loggeduser;

	public static void main(String[] args) throws IOException, IllegalArgumentException, ElementNotPresentException {
		// leggo il file di configurazione
		ConfigReader config = null;
		try {
			config = ConfigReader.configuraServer();
		} catch (Exception e) {
			System.err.println("errore durante l'accesso al file config.json");
			System.exit(0);
		}

		
		config.stampaConfigurazione();
		GestioneGson.salvaConfig(config, "config.json");
		secondiAttesaChiusura = config.getSecondiAttesaChiusura();
		portRegistryRMI = config.getPortaRMI();
		serviceName = config.getServiceName();
		serviceCallbackReg = config.getServiceCallback();
		fileJson = config.getFileJson();
		portaServer = config.getPortaServer();
		portaMulticast = config.getPortaMulticast();
		indirizzoMulticast = config.getIndirizzoMulticast();
		percentualeAutore = config.getPercentualePremioAutore();
		msecondiProssimoConteggioReward = config.getSecondiProssimoConteggioReward();
		msecondiProssimoSalvataggio = config.getMsecondiProssimoSalvataggio();

		// inizializzo la lista degli utenti connessi renmdendola synchronized per
		// renderla threadsafe
		loggeduser = Collections.synchronizedList(new ArrayList<String>());
		
		// carico eventuali dati salvati su file
		try {
			hash = GestioneGson.caricaDaFile(fileJson);
		} catch (IOException e) {
			System.err.println("errore durante il caricamento dei dati dal file" + fileJson);
			System.exit(0);
		}

		// attivo i servizii RMI per permettere al client di registrarsi al server o per
		// iscriversi alla callback
		RMIregistration obj = new RMIregistration();
		RMIcallback callback = new RMIcallback();
		try {
			// esporto l'oggetto,ottenendo lo stub corrispondente
			IRMIregistration stub = (IRMIregistration) UnicastRemoteObject.exportObject(obj, 0);
			// esporto l'oggetto,ottendneod lo stub corrispondente
			IRMIcallback stubb = (IRMIcallback) UnicastRemoteObject.exportObject(callback, 0);
			// creazione di un registry sulla porta specificata
			LocateRegistry.createRegistry(portRegistryRMI);
			Registry r = LocateRegistry.getRegistry(portRegistryRMI);
			// pubblicazione dello stub sul registry
			r.bind(serviceName, stub);
			r.bind(serviceCallbackReg, stubb);
			System.out.println("servizio: " + serviceName + " pronto sulla porta: " + portRegistryRMI);
			System.out.println("servizio: " + serviceCallbackReg + " pronto sulla porta: " + portRegistryRMI);
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
			System.err.println("errore durante la creazione del RMI");
			System.exit(0);
		}

		// creo il thread che si occupa del conteggio dei punti
		Thread assegnatoreRicompense = new Thread(new TaskCalcolatoreGuadagni(hash, msecondiProssimoConteggioReward,
				indirizzoMulticast, portaMulticast, percentualeAutore));
		assegnatoreRicompense.start();
		// creo il thread che si occupa del salvataggio periodico dei dati
		Thread salvataggioPeriodico = new Thread(
				new TaskSalvataggioPeriodico(fileJson, hash, msecondiProssimoSalvataggio));
		salvataggioPeriodico.start();
		// Apro il selettore e inizializzo il canale relativo alla ServerSocket
		Selector selector = Selector.open();
		ServerSocketChannel serverSocket = ServerSocketChannel.open();
		serverSocket.bind(new InetSocketAddress("localhost", portaServer));
		serverSocket.configureBlocking(false);
		serverSocket.register(selector, SelectionKey.OP_ACCEPT);
		// Alloco un buffer avente la dimensione fissata.
		ByteBuffer buffer = ByteBuffer.allocate(bufSize);
		System.out.println("Server pronto su porta " + portaServer +"\n");
		// Creo un cached pool thread
		ExecutorService pool = Executors.newCachedThreadPool();
		while (chiudi == false) {
			// ricavo i channel che hanno bisogno di attenzioni
			selector.select();

			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> iter = selectedKeys.iterator();
			// itero su tutti i channel
			while (iter.hasNext()) {
				try {
				SelectionKey key = iter.next();
				// Controllo se sul canale associato alla chiave

				// se c'è la possibilità di accettare una nuova connessione.
				if (key.isAcceptable()) {
					// Accetto la connessione e registro il canale ottenuto sul selettore.
					SocketChannel client = serverSocket.accept();
					client.configureBlocking(false);
					client.register(selector, SelectionKey.OP_READ);
				}
				// Se il canale associato alla chiave è leggibile leggo la stringa in arrivo dal
				// channel
				if (key.isReadable()) {
					// preparo il buffer per la lettura e leggo il messaggio in arrivo
					buffer.clear();
					((SocketChannel) key.channel()).read(buffer);
					buffer.flip();
					int receivedlenght = buffer.getInt();
					byte[] receivedByte = new byte[receivedlenght];
					buffer.get(receivedByte);
					String receivedString = new String(receivedByte);
					System.out.println("operazione ricevuta: " + receivedString);
					// ora sottometto al thread pool la stringa ricevuta
					pool.submit(new TaskServer(receivedString, hash, key, loggeduser));
				}
				iter.remove();
			}catch(SocketException e) {
				continue;
			}
				}
		}

		// uscito dal while devo chiudere
		try {
			System.out.print("server in chiusura");
			// ricavo dalla select tutti i channel ad essa registrati
			Iterator<SelectionKey> keys = selector.keys().iterator();
			while (keys.hasNext()) {
			SelectionKey key = keys.next();
			SelectableChannel channel = key.channel();
			channel.close();
			key.cancel();
			}
			// chiudo la select
			selector.close();
			// chiudo la server socket
			serverSocket.close();
			// chiudo il cached pool thread
			pool.shutdown();
			try {
				if (!pool.awaitTermination(secondiAttesaChiusura, TimeUnit.MILLISECONDS)) {
					pool.shutdownNow();
				}
			} catch (InterruptedException e) {
				pool.shutdownNow();
			}
			// chiudo RMI
			UnicastRemoteObject.unexportObject(obj, true);
			UnicastRemoteObject.unexportObject(callback, true);
			assegnatoreRicompense.interrupt();
			assegnatoreRicompense.join(1000);
			salvataggioPeriodico.interrupt();
			salvataggioPeriodico.join(1000);
			GestioneGson.salvaSuFile(hash, fileJson);
		} catch (Exception e) {
			System.err.print("errore in chiusura");
			System.exit(0);
		}
	}
	public static String getAddressMulti() {
		return indirizzoMulticast;
	}
	public static int getPortMulticast() {
		return portaMulticast;
	}
}