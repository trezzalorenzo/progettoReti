package gestioneRMI;

public class ConfigReader {
private int portRMI;
private String serviceName;
private int portTCP;
private String nomeServer;
private int portCallback;
private String nomeServizioCallback;
public ConfigReader() {
	this.portRMI=12120;
	this.serviceName="RMIregistration";
	this.portTCP=1234;
	this.nomeServer="localhost";
	this.portCallback=12120;
	this.nomeServizioCallback="RMIcallback";
}

public int getPortRMI() {
	return portRMI;
}
public String getServiceName() {
	return serviceName;
}
public int getPortTCP() {
	return portTCP;
}
public String getNomeServer() {
	return nomeServer;
}
public String getNomeServizioCallback() {
	return nomeServizioCallback;
}
public int getPortCallback() {
	return portCallback;
}

}
