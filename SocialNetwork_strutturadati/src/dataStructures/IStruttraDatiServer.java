package dataStructures;


public interface IStruttraDatiServer {

	//requires True 
	//modifies niente
	//throws   IllegalArgumentException
	//effects  dato un id,invoco la funzione "getAtutoreDaIndice" per ottenere il nome di chi ha scritto il post
	//	   	   estraggo la lista dei post di quel determinato utente e la scorro in ricerca del post
	//		   se non presente lancio l'eccezione "IllegalArgumentException"
	//return   ritorna il post con indice id
//	Post getPostDaId(int id) throws IllegalArgumentException;

	
	//requires True
	//modifies niente
	//throws   niente
	//effects  dato il nome di un utente estrae la sua lista di post di un utente
	//return   ritorna la lista di post di un utente
//	ArrayList<Post> getBlog(String utente) throws IllegalArgumentException;

	
	
	//requires True
	//modifies niente
	//throws   IllegalArgumentException
	//effects  dato il nome di un utente determina tutti gli utenti da lui seguiti,estrae il loro blog concetanandoli tutti
	//         in un unica lista,ordinandola per data
	//return   ritorna il feed di un utente
//	ArrayList<Post> getFeed(String utente) throws IllegalArgumentException;

//	Boolean utenteRegistrato(String nome) throws IllegalArgumentException;
	
	//requires True
	//modifies niente
	//throws   IllegalArgumentException
	//effects  controllando che l'username non sia già stato usato da un altro utente
	//         che la password non sia null e che il numero di tag non sia piu di 5
	//	 	   creo un oggetto di tipo Utente che viene memorizzato nella lista "utenti"
	//		   i tag vengono modificati e messi in uppercase,per evitare controlli lato client 
	//		   di ciò che sto spedendo(o lato server per capire cio che sto ricevendo)
	//return   void,modifica l'attributo "utenti"
//	void register(String username, String password, ArrayList<String> tag) throws IllegalArgumentException;

	
	
	//requires True
	//modifies niente
	//throws   IllegalArgumentException
	//effects  dato il nome di un utente,ricava la sua lista di tag con il metodo ausiliario "utenteDaNome"
	//		   scorro gli elementi nella lista utenti determinando con la funzione "tagInComune" quali utenti hanno
	//         un tag in comune 
	//return   la lista dei nomi degli utenti che hanno un tag in comune,una lista vuota altrimenti
	//ArrayList<String> utentiIntesessiSimili(String nome) throws IllegalArgumentException;

	
	//ArrayList<String> listaFollowingDiUtente(String nome) throws IllegalArgumentException;

	//requires True
	//modifies utenti
	//throws   niente
	//effects  aggiorna la lista "following" relativa all'utente che chiama il metodo e
	//         aggiorna la lista "follower" relativa all'utente che si vuole seguire
	//return   niente
//	void seguiUtente(String nome, String chiSeguire) throws IllegalArgumentException;
	
//ArrayList<String> listaFollowerDiUtente(String nome) throws IllegalArgumentException;
	
//	void creaPost(String nomeAutore, String titolo, String contenuto) throws IllegalArgumentException;

//	void smettiDiSeguireUtente(String nome, String chiNonSeguire) throws IllegalArgumentException;
}