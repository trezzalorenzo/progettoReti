package utilities;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


//metodo privato di stupporto per spedire ul messaggio al canale a cui
//key fa riferimento
public class Connection {
	public static void rispondi(String risposta,SelectionKey key,ByteBuffer buffer) {
		//ottengo il collegamento con il socketchannel riferito al client
		SocketChannel client = (SocketChannel) key.channel();
		//creo un byte array per la risposta
		byte[] reply=risposta.getBytes();
		//preparo il buffer per la risposta
		buffer.clear();
		//memorizzo la lunghezza del messaggio che sto per inviare
		buffer.putInt(reply.length);
		//memorizzo il messaggio da iniviare
		buffer.put(reply);
		//rimetto il buffer in ascolto
		buffer.flip();
		try {
			//invio
			client.write(buffer);
		} catch (IOException e) {
			System.err.println("errore in scrittura su buffer");
			e.printStackTrace();
		}
	}
}
