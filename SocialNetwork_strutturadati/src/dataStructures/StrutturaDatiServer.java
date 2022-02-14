package dataStructures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import eccezioni.ElementNotPresentException;

public class StrutturaDatiServer implements IStruttraDatiServer {

	private ConcurrentHashMap<String, ArrayList<Post>> autoriPost;
	private ArrayList<Utente> utenti;
	private ArrayList<Tripla<Integer, Integer, String>> indice;
	private int lastid;
	public StrutturaDatiServer() {

		this.autoriPost = new ConcurrentHashMap<String, ArrayList<Post>>();
		this.utenti = new ArrayList<Utente>();
		this.indice = new ArrayList<Tripla<Integer, Integer, String>>();
		this.lastid=-1;
	}

	/*--------------------------METODI_PRIVATI------------------------*/

	// la funzione che dato un id post interroga l'indice e restituisce l'autore
	private synchronized String getAutoreDaIndice(int id) throws IllegalArgumentException {
		// scorro la lista di triple cercando l'elemento con indice uguale a id
		Optional<Tripla<Integer, Integer, String>> coppia = indice.stream().filter(e -> e.getFirst() == id).findFirst();
		// controllo che in Optional vi sia effettivamente qualcosa
		if (!coppia.isPresent()) {
			System.err.println("Post con id: " + id + " non presente");
			throw new IllegalArgumentException();
		}
		return coppia.get().getThird();
	}

	// dati due utenti determina se hanno tag in comune
	private synchronized Boolean tagInComune(Utente u, Utente l) throws IllegalArgumentException {
		if (u == null || l == null) {
			throw new IllegalArgumentException();

		}
		ArrayList<String> commonItems = new ArrayList<>(l.getTag());
		commonItems.retainAll(u.getTag());
		if (commonItems.isEmpty())
			return false;
		else
			return true;
	}

	// dato il nome di un utente restituisce l'oggetto di tipo Utente a lui riferito
	public synchronized Utente utenteDaNome(String nome) throws ElementNotPresentException {
		if (nome == null || nome.isEmpty()) {
			throw new IllegalArgumentException();
		}
		Optional<Utente> utenteTrovato = utenti.stream().filter(e -> e.getNome().equals(nome)).findFirst();
		if (!utenteTrovato.isPresent()) {

			throw new ElementNotPresentException();
		}
		return utenteTrovato.get();
	}

	// dato un id restituisce un post
	public synchronized Post getPostDaId(int id) throws IllegalArgumentException {
		String autore = getAutoreDaIndice(id);
		ArrayList<Post> listaPostUtente = this.autoriPost.get(autore);
		Optional<Post> postUtente = listaPostUtente.stream().filter(e -> e.getIdpost() == id).findFirst();
		// controllo l'optional
		if (!postUtente.isPresent()) {
			System.err.println("Post con id: " + id + " non presente");
			throw new IllegalArgumentException();
		}
		return postUtente.get();
	}

	// dato un utente restituisce il suo blog
	public synchronized ArrayList<Post> getBlog(String utente)
			throws IllegalArgumentException, ElementNotPresentException {
		if (utente == null || utente.isEmpty()) {
			throw new IllegalArgumentException();
		}
		if (!utenteRegistrato(utente)) {
			throw new ElementNotPresentException();
		}
		return this.autoriPost.get(utente);
	}

	// dato un utente restituisce il suo feed
	public synchronized ArrayList<Post> getFeed(String utente)
			throws IllegalArgumentException, ElementNotPresentException {

		if (utente == null || utente.isEmpty()) {
			throw new IllegalArgumentException();
		}
		if (!utenteRegistrato(utente)) {
			throw new ElementNotPresentException();
		}

		ArrayList<Post> feed = new ArrayList<Post>();
		Optional<Utente> followingList = utenti.stream().filter(e -> e.getNome().equals(utente)).findFirst();
		if (!followingList.isPresent()) {
			System.err.println("errore nell'estrazione del feed");
			throw new IllegalArgumentException();
		}
		// estraggo la lista dei follower
		ArrayList<String> seguiti = followingList.get().getFollowing();
		// scorro la lista dei follower
		for (String i : seguiti) {
			// per ogni follower estraggo il suo log e lo concateno agli altri blog fin ora
			// estratti
			feed = (ArrayList<Post>) Stream.concat(feed.stream(), getBlog(i).stream()).collect(Collectors.toList());
		}

		return feed;
	}

	// verifica se l'utente è registrato,ritorna true se l'utente è registrato,false
	// altrimenti
	public synchronized Boolean utenteRegistrato(String nome) throws IllegalArgumentException {
		if (nome == null || nome.isEmpty()) {
			throw new IllegalArgumentException();
		}
		Optional<Utente> utente = utenti.stream().filter(e -> e.getNome().equals(nome)).findFirst();
		if (!utente.isPresent())
			return false;
		else
			return true;
	}

	// Dato nome utente, password, lista di tag salva l'utente in "utenti"
	// se la lista di tag è maggiore di 5 viene rilanciato l'errore per essere
	// gestita dal server
	public synchronized void register(String username, String password, ArrayList<String> tag)
			throws IllegalArgumentException {
		if (username == null || password == null || tag == null) {
			throw new IllegalArgumentException();
		}
		if (tag.size() > 5 || tag.size() == 0 || password.isEmpty() || username.isEmpty()
				|| utenteRegistrato(username)) {
			throw new IllegalArgumentException();
		}
		ArrayList<String> tagParsati = new ArrayList<String>();
		for (String i : tag) {
			tagParsati.add(i.toLowerCase());

		}
		Utente u = new Utente(username, password, tagParsati);
		autoriPost.put(username, new ArrayList<Post>());
		this.utenti.add(u);
	}

	// dato il nome di un utente restituisce tutti gli utenti che hanno almeno un
	// tag in comune con lui
	public synchronized ArrayList<String> utentiIntesessiSimili(String nome)
			throws IllegalArgumentException, ElementNotPresentException {
		if (nome == null || nome.isEmpty()) {
			throw new IllegalArgumentException("");
		}
		ArrayList<String> returner = new ArrayList<String>();
		Utente u = utenteDaNome(nome);
		for (Utente i : utenti) {
			if (!i.getNome().equals(nome)) {
				if (tagInComune(u, i)) {
					returner.add(i.getNome());
				}
			}
		}
		return returner;
	}

	// estrae la lista following di "nome"
	public synchronized ArrayList<String> listaFollowingDiUtente(String nome)
			throws IllegalArgumentException, ElementNotPresentException {
		if (nome == null || nome.isEmpty()) {
			throw new IllegalArgumentException();
		}
		return utenteDaNome(nome).getFollowing();
	}

	// aggiorna la lista following di "nome" e la lista follower di "chiSeguire"
	public synchronized void seguiUtente(String nome, String chiSeguire)
			throws IllegalArgumentException, ElementNotPresentException {
		if (nome.equals(chiSeguire) || nome == null || chiSeguire == null || nome.isEmpty() || chiSeguire.isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		// controllo se gli utenti sono registrati
		if (!this.utenteRegistrato(nome) || !this.utenteRegistrato(chiSeguire)) {
			throw new IllegalArgumentException();
		}
		Utente u = utenteDaNome(nome);
		if(u.getFollowing().contains(chiSeguire)) {
			throw new IllegalArgumentException();
		}
		Utente v = utenteDaNome(chiSeguire);
		// se gli utenti non hanno tag in comune
		if (!tagInComune(u, v)) {
			throw new IllegalArgumentException();
		}
		u.aggiungiFollowing(chiSeguire);
		v.aggiungiFollower(nome);
	}

	// metodo di utilita ma probabilmente non sara usato dal server
	public synchronized ArrayList<String> listaFollowerDiUtente(String nome)
			throws IllegalArgumentException, ElementNotPresentException {
		if (nome == null || nome.isEmpty()) {
			throw new IllegalArgumentException();
		}
		return utenteDaNome(nome).getFollower();
	}

	// metodo per inserire un post nell'hashmap
	public synchronized void creaPost(String nomeAutore, String titolo, String contenuto)
			throws IllegalArgumentException {
		if (titolo.length() > 20 || contenuto.length() > 500 || !utenteRegistrato(nomeAutore)) {
			System.err.println("errore nei parametri");
			throw new IllegalArgumentException();
		}
		if (nomeAutore == null || titolo == null || contenuto == null || titolo.isEmpty() || contenuto.isEmpty()) {
			System.err.println("errore nei parametri");
			throw new IllegalArgumentException();
		}

		int id = getnewid();
		Post p = new Post(id, nomeAutore, titolo, contenuto, nomeAutore, id);
		autoriPost.get(nomeAutore).add(p);
		Tripla<Integer, Integer, String> t = new Tripla<Integer, Integer, String>(id, id, nomeAutore);
		indice.add(t);
	}

	// metodo per togliere il follow ad un utente
	public synchronized void smettiDiSeguireUtente(String nome, String chiNonSeguire)
			throws IllegalArgumentException, ElementNotPresentException {
		if (nome == null || chiNonSeguire == null || nome.isEmpty() || chiNonSeguire.isEmpty()) {
			throw new IllegalArgumentException();
		}
		Utente u = utenteDaNome(nome);
		u.togliFollowing(chiNonSeguire);
		Utente v = utenteDaNome(chiNonSeguire);
		v.togliFollower(nome);
	}

	// metodo per pubblicare un rewin,viene aggiornata l'hashmap e l'indice dei post
	public synchronized void pubblicaRewin(String nome, int idPost) throws IllegalArgumentException {
		if (nome == null || nome.isEmpty()) {
			throw new IllegalArgumentException();
		}
		Post p = this.getPostDaId(idPost);
		// se è un rewind di un rewind
		if (p.getOriginalId() != p.getIdpost()) {
			System.out.println("rewin di rewin");
			throw new IllegalArgumentException();
		}
		int id = getnewid();
		Post rewin = new Post(id, nome, p.getTitolo(), p.getContenuto(), p.getAutore(), p.getIdpost());
		if (autoriPost.containsKey(nome) == false) {
			System.err.println(nome + " non presente");
			throw new IllegalArgumentException();
		}
		Tripla<Integer, Integer, String> t = new Tripla<Integer, Integer, String>(id, p.getIdpost(), nome);
		indice.add(t);
		autoriPost.get(nome).add(rewin);
	}

	// metodo per aggiungere un commento ad un post
	public synchronized void aggiungiCommento(int id, String commento, String nomeAutoreCommento)
			throws IllegalArgumentException, ElementNotPresentException {
		if (commento == null || nomeAutoreCommento == null || commento.isEmpty() || nomeAutoreCommento.isEmpty()) {
			throw new IllegalArgumentException();
		}
		Post p = this.getPostDaId(id);
		if (p.getAutore().equals(nomeAutoreCommento)) {
			throw new IllegalArgumentException();
		}
		Utente b=utenteDaNome(p.getAutore());
		Utente l=utenteDaNome(nomeAutoreCommento);
		
		if (!tagInComune(b,l)) {
			throw new IllegalArgumentException();
		}
		p.aggiungiCommento(commento, nomeAutoreCommento);
	}

	// aggiungi un like ad un post,si aspetta come voto una stringa formattata come
	// "+1" per voti positivi o "-1" altrimenti
	public synchronized void votaPost(int id, String voto, String nome) throws IllegalArgumentException {
		if (voto == null || nome == null) {
			throw new IllegalArgumentException();
		}
		if(!voto.equals("-1")) {
			if(!voto.equals("+1")) {
				throw new IllegalArgumentException();
			}
		}
		Post p = this.getPostDaId(id);
		if (p.getUpVote().contains(nome) || p.getDownVote().contains(nome)) {
			throw new IllegalArgumentException();
		}
		if (voto.equals("+1")) {
			p.aggiungiLike(nome);
		}
		if (voto.equals("-1")) {
			p.aggiungiDislike(nome);
		}
	}
	

	// elimina un post e tutti i suoi rewin(elimina anche i rewin dei rewin a
	// cascata)
	public synchronized void cancellaPost(int id, String nome) throws IllegalArgumentException {
		if (nome == null || nome.isEmpty()) {
			throw new IllegalArgumentException();
		}
		Optional<Tripla<Integer, Integer, String>> elementoDaEliminare = indice.stream().filter(q -> q.getFirst() == id)
				.findFirst();
		if (!elementoDaEliminare.isPresent()) {
			throw new IllegalArgumentException();
		}
		// se il post che voglio eliminare "non mi appartiene" lancio eccezione
		if (!nome.equals(elementoDaEliminare.get().getThird())) {
			throw new IllegalArgumentException();
		}

		autoriPost.get(nome).remove(getPostDaId(id));
		indice.remove(elementoDaEliminare.get());
		ArrayList<Tripla<Integer, Integer, String>> daEliminare = new ArrayList<Tripla<Integer, Integer, String>>();
		// scorro l'indice cercando un elemento con indice secondario (op) uguale
		// all'indiche che voglio eliminare
		for (Tripla<Integer, Integer, String> t : indice) {
			if (t.getSecond() == id) {
				autoriPost.get(t.getThird()).remove(getPostDaId(t.getFirst()));
				daEliminare.add(t);
			}
		}
		for (Tripla<Integer, Integer, String> t : daEliminare) {
			indice.remove(t);
		}
	}

	// stampa per debug
	public synchronized void stampaHash() {
		if (this.autoriPost != null) {
			for (String key : autoriPost.keySet()) {
				System.out.print(key + " : ");
				for (Post p : autoriPost.get(key)) {
					System.out.print(p.getIdpost() + " , ");
				}
				System.out.println();
			}
		}
	}

	// stampa per debug
	public synchronized void stampaUtentiTag() throws ElementNotPresentException {
		if (this.autoriPost != null) {
			for (String key : autoriPost.keySet()) {
				System.out.print(key + " : ");
				for (String p : utenteDaNome(key).getTag()) {
					System.out.print(p + " , ");
				}
				System.out.println();
			}
		}
	}

	// stampa per debug
	public synchronized void stampaIndice() {
		for (Tripla<Integer, Integer, String> t : this.indice) {
			System.out.println("indice post " + t.getFirst() + " indice originalPost " + t.getSecond() + " autore "
					+ t.getThird());
		}
	}

	// stampa per debug
	public synchronized void stampaUtenti() {
		for (Utente p : utenti) {
			System.out.print(p.getNome() + ",	");
		}
	}

	/*
	 * funzione che si occupa di scandire tutti i valori dell'hashmap e per ogni
	 * post calcolarne il Reward
	 * 
	 * @param niente
	 * 
	 * @return ritorna un arraylist di triple,ogni tripla contiene come informazioni
	 * il nome dell'autore del post,il guadagno proveniente dal post e una
	 * lista(senza ripetizioni) dei curatori con cui spartire il reward
	 * 
	 * @throws niente
	 * 
	 */

	public synchronized ArrayList<Tripla<String, Double, Set<String>>> valutaPosts() {
		ArrayList<Tripla<String, Double, Set<String>>> contabilita = new ArrayList<Tripla<String, Double, Set<String>>>();
		for (String key : autoriPost.keySet()) {
			for (Post p : autoriPost.get(key)) {
				// HashSet per eliminare duplicati
				Set<String> curatori = new HashSet<String>();
				curatori.addAll(p.commentatori());
				curatori.addAll(p.getUpVote());

				contabilita.add(new Tripla<String, Double, Set<String>>(key, p.valutaPost(), curatori));
			}
		}
		return contabilita;
	}

	public int getnewid() {
		this.lastid++;
		return lastid;
	}

}
