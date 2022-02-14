package utilities;

import java.io.IOException;
import java.net.InetAddress;

public class ConfigReader {
	private int secondiAttesaChiusura;
	private int portRegistryRMI;
	private String serviceName;
	private String serviceCallbackReg;
	private String fileJson;
	private int portaServer;
	private int portaMulticast;
	private String indirizzoMulticast;
	private int percentualePremioAutore;
	private int msecondiProssimoConteggioReward;
	private int msecondiProssimoSalvataggio;

	public ConfigReader() {
		this.secondiAttesaChiusura = 160000;
		this.portRegistryRMI = 12120;
		this.serviceName = "RMIregistration";
		this.serviceCallbackReg = "RMIcallback";
		this.fileJson = "saved.json";
		this.portaServer = 1234;
		this.portaMulticast = 44444;
		this.indirizzoMulticast = "239.255.32.32";
		this.percentualePremioAutore = 70;
		this.msecondiProssimoConteggioReward = 2000;
		this.msecondiProssimoSalvataggio = 160000;
	}

	public int getSecondiAttesaChiusura() {
		return secondiAttesaChiusura;
	}

	public int getSecondiProssimoConteggioReward() {
		return msecondiProssimoConteggioReward;
	}

	public int getPercentualePremioAutore() {
		return percentualePremioAutore;
	}

	public int getPortaMulticast() {
		return portaMulticast;
	}

	public String getIndirizzoMulticast() {
		return indirizzoMulticast;
	}

	public String getServiceCallback() {
		return serviceCallbackReg;
	}

	public String getServiceName() {
		return serviceName;
	}

	public int getPortaRMI() {
		return portRegistryRMI;
	}

	public int getPortaServer() {
		return portaServer;
	}

	public String getFileJson() {
		return fileJson;
	}

	public int getMsecondiProssimoSalvataggio() {
		return msecondiProssimoSalvataggio;
	}

	/*
	 * deserializzo il file config.java e controllo i parametri
	 * in caso di errori uso dei valori di default e genero un nuovo
	 * file di configurazione
	 * 
	 * @param niente
	 * 
	 * @return ConfigReader con i parametri letti dal file config o se c'è un errore
	 * durante la lettura del file json o "indirizzoMulticast" non è un indirizzo
	 * multicast viene usata la configurazione di defaul che sarà salvata nel file
	 * config
	 * 
	 * @throws IOException
	 * 
	 */
	public static ConfigReader configuraServer() throws IOException {
		ConfigReader configurazione;
		try {
			configurazione = GestioneGson.caricaConfig("config.json");
			InetAddress group = InetAddress.getByName(configurazione.getIndirizzoMulticast());
			if (!group.isMulticastAddress()) {
				configurazione = new ConfigReader();
				GestioneGson.salvaConfig(configurazione, "config.json");
			}
		} catch (Exception e) {
			ConfigReader c = new ConfigReader();
			GestioneGson.salvaConfig(c, "config.json");
			return c;
		}
		return configurazione;
	}

	/*
	 * funzione per stampare i valore di configurazione
	 * 
	 * @param niente
	 * 
	 * @return niente
	 * 
	 * @throws niente
	 * 
	 */
	public void stampaConfigurazione() {
		System.out.println("salvataggio su file ogni: " + this.msecondiProssimoSalvataggio + " ms");
		System.out.println("conteggio premi ogni: " + this.msecondiProssimoConteggioReward + " ms");
		System.out.println("percentuale guadagno degli autori: " + this.percentualePremioAutore + "%");
		System.out.println("indirizzo multicast: " + this.indirizzoMulticast);
		System.out.println("porta multicast: " + this.portaMulticast);
		System.out.println("porta server: " + this.portaServer);
		System.out.println("nome file su cui salvare: " + this.fileJson);
		System.out.println("nome servizio RMI callback: " + this.serviceCallbackReg);
		System.out.println("nome servizio RMI registrazione: " + this.serviceName);
		System.out.println("porta registro: " + this.portRegistryRMI);
		System.out.println("chiusura forzata del server entro: " + this.secondiAttesaChiusura + " ms");
		System.out.println("");
	}

}