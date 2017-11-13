package io.github.rubinsoft.bot.wimpb;

import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.annotation.WebServlet;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import io.github.rubinsoft.bot.fruitmasterbot.utils.Lang;
import io.github.rubinsoft.pengrad.openshift.BotHandler;
import io.github.rubinsoft.pengrad.openshift.DBConnector;

/**
 * @since 15.06.2017
 * @author firebone
 *
 */
@SuppressWarnings("deprecation")
@WebServlet("/mywimpb")
public class WorkIsMyPrisonBot extends BotHandler {
	private static final long serialVersionUID = 1917378482937759905L;
	public static final String BOT_TOKEN = "403342162:AAE1DwOQxmF2VZSspJaSeY1tEbIlHVARSNM";
	public static final String WEB_HOOK= "https://fruitmaster-botfactory.rhcloud.com/mywimpb";
	public static final String DB_NAME = "WIMP";
	
	TelegramBot bot = TelegramBotAdapter.buildDebug(BOT_TOKEN);

	public static final int tentativi = 10;
	

	public Connection conn;

	@Override
	protected boolean onStart(Message message) {
		//inizializzo la lingua
		Lang lang = new Lang(message);
		Long chatId = message.chat().id();
		//messaggio di benvenuto
		sendMessage(chatId, lang.WIMPB_START_MESSAGE );
		//genero il menu
		startMenu(message);
		log(message, "has joined", false);
		return true;
	}

	private SendResponse sendMessage(Long chatId, String text){
		SendMessage request = new SendMessage(chatId, text)
				.parseMode(ParseMode.HTML)
				.disableWebPagePreview(true)
				.disableNotification(true)
				.replyToMessageId(0);
		//		        .replyMarkup(new ForceReply());
		//				.replyMarkup(new ReplyKeyboardRemove());
		// sync
		SendResponse sendResponse = bot.execute(request);
//		boolean ok = sendResponse.isOk();
		return sendResponse;
	}

	private SendResponse sendMessage(Long chatId, String text, Keyboard key){
		SendMessage request = new SendMessage(chatId, text)
				.parseMode(ParseMode.HTML)
				.disableWebPagePreview(true)
				.disableNotification(true)
				.replyToMessageId(0)
				.replyMarkup(key);

		// sync
		SendResponse sendResponse = bot.execute(request);
//		boolean ok = sendResponse.isOk();
		return sendResponse;
	}


	private void startMenu(Message message) {
		Lang lang = new Lang(message);
		String[][] kbArray = { new String[]{lang.WIMPB_PLAYER_VS_COMPUTER},
				new String[]{lang.WIMPB_COMPUTER_VS_COMPUTER},
				new String[]{lang.WIMPB_LOAD_BRAIN},
				new String[]{lang.WIMPB_COMPUTER_LIST},
				new String[]{lang.WIMPB_HELP_LABEL},
				new String[]{lang.WIMPB_CONTACT_US}
		};
		Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(kbArray)
				.oneTimeKeyboard(true)   // optional
				.resizeKeyboard(true)    // optional
				.selective(true);        // optional
		sendMessage(message.chat().id(), lang.TEXT_MENU_1,replyKeyboardMarkup);
	}

	@Override
	protected void onWebhookUpdate(Update update) {
		Message message = update.message();
		Lang lang = new Lang(message);
		String input = message.text();
		boolean debug = false;
		//parsing messaggio
		if (input == null || input.equals("")) return;	// se messaggio null, esci
		input = input.toLowerCase().trim();
		if (input.charAt(0) == '/') 					// elimino eventuali caratteri di comando
			input = input.substring(1, input.length());
		if (input.startsWith("--debug")){ 				// attivazione debug
			debug = true;
			input = input.substring(7,input.length()).trim();
		}
		if(debug) error(message, "Debug attivo.");
		if(debug) error(message, "input: "+input);
//		if(debug) error(message, "json: "+ message.toString());
		//logica bot
		if (input.startsWith("pvc") || input.startsWith(lang.WIMPB_PLAYER_VS_COMPUTER))
			playPvC(message, debug, input);
		else if (input.startsWith("cvc") || input.startsWith(lang.WIMPB_COMPUTER_VS_COMPUTER))
			playCvC(message, debug, input);
		else if (input.startsWith("load") || input.startsWith(lang.WIMPB_LOAD_BRAIN))
			loadJar(message, debug, input);
		else if (input.startsWith(lang.WIMPB_COMPUTER_LIST))
			getComputerList(message, debug);
		else if (input.startsWith(lang.WIMPB_HELP_LABEL))
			help(message, debug);
		else if (input.equals(lang.WIMPB_CONTACT_US))
			contactUs(message);
		else if (input.equals("__manutenzione"))
			maintenance(message);
		else if (!isGameMessage(message, debug, input))
			sorry(message);
		//end if
	}

	private void loadJar(Message message, boolean debug, String input) {
//		String fileDir = "https://api.telegram.org/file/bot"+BOT_TOKEN+"/";
		sendMessage(message.chat().id(),new Lang(message).WIMPB_LOAD_YOUR_BRAIN.replaceAll("&1", ""+message.chat().id()));
	}

	private void maintenance(Message message) {
		sendMessage(message.chat().id(),new Lang(message).MAINTENANCE_MESSAGE);
	}

	private void contactUs(Message message) {
		String msg = "info:\nmail->blackrubin@gmail.com\ntelegram->@firebone";
		sendMessage(message.chat().id(),msg);
	}

	private boolean isGameMessage(Message message, boolean debug, String token) {
		//TODO
//		Long chatID = message.chat().id();
//		Statement statement = null;
		//qui dovrei gestire le partite PvC - per le CvC, ci sara' solo output 
		//		try {
		//			statement = getStatement(message, debug);
		//			ResultSet rsGame = statement
		//					.executeQuery("SELECT * FROM game WHERE chatid = '"+chatID+"' AND active = 'Y'");
		//			if (!rsGame.first()){
		//				if (debug) error(message, new Lang(message).E001_GAME_NOT_EXISTS);	//la partita non esiste
		//				return false;
		//			}
		//			if (debug) error(message, "GAME: recuperato partita");	
		//			rsGame.absolute(1);
		//			//recupero combo
		//			String combo = rsGame.getString("combo");
		//			if(combo == null){
		//				if (debug) error(message, new Lang(message).E003_COMBO_NULL);
		//				return false;
		//			}
		//			if (debug) error(message, "GAME: combo "+combo);
		//			//controllo che il formato del messaggio in input sia valido
		//			token = formattaMessaggio(token, combo.length());
		//			if(token == null){
		//				if (debug) error(message, new Lang(message).E002_NO_INPUT_VALID+token);	//input non valido 
		//				return false;
		//			}
		//			if (debug) error(message, "GAME: formattato messaggio");
		//			//recupero numero tentativo
		//			int tentativi = rsGame.getInt("try") + 1 ;
		//			String response = new Lang(message).TRY_NR+tentativi+": ";
		//			if(token.equals(combo)){//caso migliore: ho indovinato
		//				response += new Lang(message).WIN_MESSAGE;
		//				rsGame.updateString("active", "N");//resetto la partita
		//				rsGame.updateString("win", "Y");//setto lo stato
		//			}else if(tentativi >= WorkIsMyPrisonBot.tentativi){//terminato numero tentativi
		//				response += new Lang(message).RETRY_MESSAGE;
		//				rsGame.updateString("active", "N");//resetto la partita
		//				rsGame.updateString("win", "N");//setto lo stato
		//			}else{//e' un tentativo valido ma non ho indovinato. Passo alla correzione degli errori
		//				int posCorretta=0, posErrata=0;
		//				boolean[] posCombo = new boolean[combo.length()];
		//				boolean[] posToken = new boolean[combo.length()];
		//				for(int i=0; i<combo.length();i++)
		//					if(combo.charAt(i)==token.charAt(i)){
		//						posCorretta++;
		//						posCombo[i] = true;
		//						posToken[i] = true;
		//					}
		//
		//				for(int i=0; i<token.length();i++)		//loop su token
		//					for(int j=0; j<combo.length();j++){	//loop su combo
		//						if (i==j) continue;
		//						if (posCombo[j]||posToken[i]) continue;
		//						if (combo.charAt(i)!=token.charAt(j)) continue;
		//						posErrata++;
		//						posCombo[j] = true;
		//						posToken[i] = true;
		//					}
		//				error(message, ""+ posCorretta + " corrette, " + posErrata + " corrette in posizione errata");
		//				rsGame.updateInt("try", tentativi);
		//			}
		//			rsGame.updateTimestamp("chdate", new Timestamp(System.currentTimeMillis()));
		//			rsGame.updateRow();	//aggiornamento dati a DB
		//			sendMessage(message.chat().id(),response);
		//		} catch (Exception e) {
		//			if(debug) 
		//				error(message, e);
		//			else 
		//				error(message, new Lang(message).E000_GENERIC_ERROR);
		//		}
		return false;
	}

	private void sorry(Message message) {
		//mando sorry solo se e' una chat privata
		if(message.chat().type().equals(Chat.Type.Private))
			sendMessage(message.chat().id(), new Lang(message).WIMPB_SORRY_MESSAGE);
	}

	private void playPvC(Message message, boolean debug, String args) {
		//TODO
//		Lang lang = new Lang(message);
//		String[] arg = args.split(" ");
//		sendMessage(message.chat().id(), lang.WIMPB_PVC_MESSAGE);
		//controlla se ci sono i parametri,
		//se non ci sono
		//	mostra la lista delle macchine, consiglia il comando pvc +macchina ed esci
		//altrimenti
		//	seleziona la macchina indicata e inizializza la partita
	}

	private void playCvC(Message message, boolean debug, String args) {
		String thisComputer = "";
		String rivalComputer = "";
		String thisComputerPath = "";
		String rivalComputerPath = "";
		String thisArgs = "";
		String rivalArgs = "";
		String thisLastMove = "";
		String rivalLastMove = "";
		boolean endOfGame = false;
		int turn = 1;
		int thisPoints = 0;
		int rivalPoints = 0;
		int maxPoints = 500;
		int maxTurns = 100;

		String[] arg = args.split(" ");
		//controlla se esiste una macchina associata all'utente chiamante
		Statement statement = null;
		try {
			statement = DBConnector.getStatement(DB_NAME);//getStatement(message, debug);

			ResultSet rsBrain = statement.executeQuery("SELECT * FROM brain WHERE chatid = '"+ message.chat().id() + "'");
			//se non trovata
			//	esci invitandolo a caricare una macchina
			if ( !rsBrain.first() ){
				sendMessage(message.chat().id(), new Lang(message).WIMPB_NO_COMPUTER_FOUND);
				return;
			}
			rsBrain.absolute(1);
			thisComputer = rsBrain.getString("brainName");
			thisComputerPath = rsBrain.getString("brainPath");
			if(debug) error(message, "SQL: trovato cervello 1 "+thisComputer);
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			if(debug) 
				error(message, e);
			else 
				error(message, new Lang(message).WIMPB_E001_GENERIC_ERROR );
		}
		//controlla se ci sono i parametri,
		//se non ci sono
		//	mostra la lista delle macchine, consiglia il comando cvc +macchina ed esci
		if(arg.length<2 || !arg[1].matches("[0-9]+")){
			sendMessage(message.chat().id(), new Lang(message).WIMPB_NO_ARGS_FOUND);
			return;
		}
		//altrimenti
		//	seleziona la macchina indicata e se esiste inizializza la partita
		try {
			statement = DBConnector.getStatement(DB_NAME);//getStatement(message, debug);
			ResultSet rsBrain = statement.executeQuery("SELECT * FROM brain WHERE count = '"+ arg[1] + "'");
			//se non trovata
			//	esci invitandolo a selezionare una macchina valida
			if ( !rsBrain.first() ){
				sendMessage(message.chat().id(), new Lang(message).WIMPB_NO_RIVAL_COMPUTER_FOUND);
				return;
			}
			rsBrain.absolute(1);
			rivalComputer = rsBrain.getString("brainName");
			rivalComputerPath = rsBrain.getString("brainPath");
			if(debug) error(message, "SQL: trovato cervello 2 "+thisComputer);
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			if(debug) 
				error(message, e);
			else 
				error(message, new Lang(message).WIMPB_E001_GENERIC_ERROR );
		}
		//carica statistiche giocatore
		try{
			statement = DBConnector.getStatement(DB_NAME);//getStatement(message, debug);
			//fare select sulla tabella degli utenti con chatID
			ResultSet rsUser = statement.executeQuery("SELECT * FROM user WHERE chatid = '"+ message.chat().id() + "'");
			if(rsUser.absolute(1)){
				maxTurns = rsUser.getInt("defaultTurns");
				maxPoints = rsUser.getInt("defaultPoints");
				if(debug) error(message, "SQL:caricate variabili giocatore");
			}
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			if(debug) 
				error(message, e);
			else 
				error(message, new Lang(message).WIMPB_E001_GENERIC_ERROR );
		}
		//inizia sfida
		while(!endOfGame){
			System.out.println("turno "+turn+":");
			if (turn == 1) {
				thisArgs = "gameid=" + message.chat().id() + " initialize";
				rivalArgs = "gameid=" + message.chat().id() + " initialize";
			} else {
				thisArgs = "gameid=" + message.chat().id() + " move=" + rivalLastMove;
				rivalArgs = "gameid=" + message.chat().id() + " move=" + thisLastMove;
			}
			//processo thisComputer
			try {
				Process proc = Runtime.getRuntime().exec("java -jar "+ thisComputerPath + thisComputer + ' ' + thisArgs);
				proc.waitFor();
				//get Error
				InputStream err = proc.getErrorStream();
				byte c[]=new byte[err.available()];
				err.read(c,0,c.length);
				if(c.length > 0)
					throw new IOException(new Lang(null).WIMPB_E000_JAR_ERROR + thisComputer + "\n"+ new String(c));
				// Then retrive the process output
				InputStream in = proc.getInputStream();
				byte b[] = new byte[in.available()];
				in.read(b,0,b.length);
				if(debug) error(message, ""+thisComputer + " risponde " +  new String(b));
				thisLastMove = new String(b).trim();;
				if (!thisLastMove.equals("0") && !thisLastMove.equals("1"))
					throw new IOException("valore non ammesso");
			} catch (IOException|InterruptedException e) {
				if(debug) 
					error(message, e);
				else 
					error(message, new Lang(message).WIMPB_E000_JAR_ERROR + thisComputer);
				return;
			}

			//processo rivalComputer
			try {
				Process proc = Runtime.getRuntime().exec("java -jar "+ rivalComputerPath + rivalComputer + ' ' + rivalArgs);
				proc.waitFor();
				//get Error
				InputStream err = proc.getErrorStream();
				byte c[]=new byte[err.available()];
				err.read(c,0,c.length);
				if(c.length > 0)
					throw new IOException(new Lang(null).WIMPB_E000_JAR_ERROR + rivalComputer + "\n"+ new String(c));
				// Then retrive the process output
				InputStream in = proc.getInputStream();
				byte b[] = new byte[in.available()];
				in.read(b,0,b.length);
				if(debug) error(message, ""+rivalComputer + " risponde " +  new String(b));
				rivalLastMove = new String(b).trim();
				if (!rivalLastMove.equals("0") && !rivalLastMove.equals("1"))
					throw new IOException("valore non ammesso");
			} catch (IOException|InterruptedException e) {
				if(debug) 
					error(message, e);
				else 
					error(message, new Lang(message).WIMPB_E000_JAR_ERROR + rivalComputer);
				return;
			}
			//avanzamento
			turn++;
			//assegnazione punti
			if (thisLastMove.equals("0") && rivalLastMove.equals("0")){
				thisPoints++;
				rivalPoints++;
			} else if (thisLastMove.equals("0") && rivalLastMove.equals("1")){
				thisPoints+=7;
			} else if (thisLastMove.equals("1") && rivalLastMove.equals("0")){
				rivalPoints+=7;
			} else if (thisLastMove.equals("1") && rivalLastMove.equals("1")){
				thisPoints+=6;
				rivalPoints+=6;
			}
			if(debug) error(message, "Punteggio: "+thisComputer+"="+thisPoints+" "+rivalComputer+"="+rivalPoints);
			//condizioni di fine partita
			if (maxPoints != 0 && 
					(thisPoints >= maxPoints || rivalPoints >= maxPoints )) {
				endOfGame = true;
			}
			if (maxTurns != 0 && turn > maxTurns) {
				endOfGame = true;
			}
		}
		turn--;
		sendMessage(message.chat().id(), "Partita terminata! Risultati:\nTurni giocati: "+turn+
				'\n' + "Tuo Cervello ("+thisComputer+"): "+ thisPoints + " punti\n"
				+ "Cervello rivale ("+rivalComputer+"): "+ rivalPoints + " punti");

		//storicizzazione nel DB
		try{
			statement = DBConnector.getStatement(DB_NAME);//getStatement(message, debug);
			ResultSet rsGame = statement.executeQuery("SELECT MAX(count) FROM game WHERE chatid = '"+ message.chat().id() + "'");
			if (debug) error(message, "salvataggio dati partita..");
			long nextCount = (rsGame.first())? rsGame.getLong("count") + 1 : 1;
			rsGame.moveToInsertRow();
			rsGame.updateLong("chatid", message.chat().id());
			rsGame.updateLong("count", nextCount);
			rsGame.updateString("player1", thisComputer);
			rsGame.updateString("player2", rivalComputer);
			rsGame.updateString("active", "N");
			rsGame.updateString("gametype", (maxTurns!=0&&maxPoints!=0)?"mix":(maxTurns==0)?"point":"turn");
			rsGame.updateTimestamp("crtimestamp", new java.sql.Timestamp(System.currentTimeMillis()));
			rsGame.updateInt("player1Points", thisPoints);
			rsGame.updateInt("player1Points", rivalPoints);
			rsGame.updateInt("roundsLimit", maxTurns);
			rsGame.updateInt("pointsLimit", maxPoints);
			rsGame.insertRow();
			rsGame.moveToCurrentRow();
		}catch(Exception e){
			if(debug)
				error(message, e);
			else
				error(message, "unexpected error: contact the developer (@firebone)");
		}
	}

	private void help(Message message, boolean debug) {
		sendMessage(message.chat().id(), new Lang(message).WIMP_RULES);
	}

	private void error(Message message, Exception e) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < e.getStackTrace().length; i++) {
			sb.append(""+ e.getStackTrace()[i] + '\n');
		}
		sendMessage(message.chat().id(), "" + e.getClass() + ':' + e.getMessage() + '\n' + sb.toString());
	}

	private void error(Message message, String s) {
		sendMessage(message.chat().id(), s);
	}

//	private Statement getStatement(Message message, boolean debug) throws SQLException{
//		if (conn == null || conn.isClosed()){
//			try {
//				Class.forName("com.mysql.jdbc.Driver").newInstance();
//				conn = DriverManager.getConnection("jdbc:mysql://127.6.211.2/WIMPB",
//						"adminq1HeHLQ","5dBUip1aD2Wh");
//			} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
//				if (debug) error(message, e);
//			}
//		}
//		return conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
//	}

	private void log(Message message, String log, boolean debug) {
		Statement statement = null;
		try{
			statement = DBConnector.getStatement(DB_NAME);//getStatement(message, debug);
			//fare select sulla tabella degli utenti con chatID
			ResultSet rsUser = statement.executeQuery("SELECT * FROM user WHERE chatid = '"+ message.chat().id() + "'");
			if (debug) error(message, "LOG: ricerca chatid anagrafato: "+ rsUser);
			if ( !rsUser.first() ){//se non trovo utente, anagrafare
				rsUser.moveToInsertRow();
				rsUser.updateLong("chatid", message.chat().id());
				rsUser.updateString("username", message.from().username());
				rsUser.updateString("firstname", message.from().firstName());
				rsUser.updateString("lastname", message.from().lastName());
				rsUser.updateTimestamp("crtimestamp", new java.sql.Timestamp(System.currentTimeMillis()));
				rsUser.updateInt("defaultPoints", 0);
				rsUser.updateInt("defaultTurns", 50);
				rsUser.insertRow();
				rsUser.moveToCurrentRow();
				if (debug) error(message, "LOG: l'utente e' stato anagrafato");
			} else { 
				if (debug) error(message, "LOG: utente gia' anagrafato");
			}
			//				//log
			//				ResultSet rsLog = statement.executeQuery("SELECT MAX(count) as count FROM log WHERE chatid = '"+ message.chat().id() + "'"); //recupero la tabella
			//				if (debug) error(message, "LOG: recupero tabella log: "+ rsLog);
			//				//se ho trovato un MAX allora lo assegno, altrimenti e' il primo log
			//				long nextCount = (rsLog.first())? rsLog.getLong("count") + 1 : 1;
			//	//			rsLog.close();
			//				//recupero uno statement aggiornabile
			//				rsLog = statement.executeQuery("SELECT * FROM log WHERE chatid = '"+ message.chat().id() + "'");
			//				rsLog.moveToInsertRow();
			//				rsLog.updateLong("chatid", message.chat().id());
			//				rsLog.updateLong("count", nextCount);
			//				rsLog.updateString("message", log);
			//				rsLog.insertRow();
			//				rsLog.moveToCurrentRow();
			//				if (debug) error(message, "LOG: log effettuato: "+ rsLog);
		}catch(Exception e){
			if(debug)
				error(message, e);
			else
				error(message, "unexpected error: contact the developer (@firebone)");
		}
	}


	private void getComputerList(Message message, boolean debug) {
		Statement statement = null;
		try {
			statement = DBConnector.getStatement(DB_NAME);//getStatement(message, debug);
			ResultSet rsBrainList = statement.executeQuery("SELECT count, brainName FROM brain WHERE 1");
			StringBuilder sb = new StringBuilder();
			for(int i = 1; rsBrainList.absolute(i); i++)
				sb.append("" + rsBrainList.getInt(1) + " - " + rsBrainList.getString(2)+'\n');
			if(sb.toString() == null || sb.toString().equals("") ){
				if(debug) error(message, "Attenzione DB cervelli VUOTO");
				else error(message, "unexpected error: contact the developer (@firebone)");
				return;
			}
			sendMessage(message.chat().id(), ""+sb.toString());
		} catch (Exception e) {
			if(debug)
				error(message, e);
			else
				error(message, "unexpected error: contact the developer (@firebone)");
		}

	}

}
