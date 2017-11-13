package io.github.rubinsoft.bot.fruitmasterbot;

import java.sql.DriverManager;
import java.sql.SQLException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.servlet.annotation.WebServlet;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;

import io.github.rubinsoft.bot.fruitmasterbot.utils.Lang;
import io.github.rubinsoft.pengrad.openshift.BotHandler;

/**
 * @since 01.06.2016
 * @author firebone
 *
 */
@SuppressWarnings("deprecation")
@WebServlet("/myfmb")
public class FruitMasterBot extends BotHandler {
	private static final long serialVersionUID = -6753334421532415974L;
	public static final String BOT_TOKEN = "217127485:AAG0IiwrpcXD24NMw45g9l6iARARyRTIjV4";
	public static final String WEB_HOOK= "https://fruitmaster-botfactory.rhcloud.com/myfmb";

	TelegramBot bot = TelegramBotAdapter.buildDebug(BOT_TOKEN);

	public static final int tentativi = 10;

	public static final String FRAGOLA_ROSSA = "a";//":strawberry:"; 
	public static final String LIMONE_GIALLO = "b";//":lemon:";
	public static final String UVA_VIOLA = "c";//":grapes:";
	public static final String MELA_VERDE = "d";//":green_apple:";
	public static final String[] FRUTTI = new String[]{FRAGOLA_ROSSA, LIMONE_GIALLO, UVA_VIOLA, MELA_VERDE};
	public static final String OK_POS_CORRETTA = ":large_blue_circle:";
	public static final String OK_POS_NON_CORRETTA = ":red_circle:";

	public Connection conn;

	@Override
	protected boolean onStart(Message message) {
		//inizializzo la lingua
		Lang lang = new Lang(message);
		Long chatId = message.chat().id();
		//messaggio di benvenuto
		sendMessage(chatId, lang.START_MESSAGE );
		//genero il menu
		startMenu(message);
		//TODO log(message, "has joined", false);
		return true;
	}
	
	private void sendMessage(Long chatId, String text){
		SendMessage request = new SendMessage(chatId, text)
		        .parseMode(ParseMode.HTML)
		        .disableWebPagePreview(true)
		        .disableNotification(true)
		        .replyToMessageId(0)
		        .replyMarkup(new ForceReply());
//				.replyMarkup(new ReplyKeyboardRemove());

		// sync
		bot.execute(request);
//		SendResponse sendResponse = bot.execute(request);
//		boolean ok = sendResponse.isOk();
//		Message message = sendResponse.message();
	}
	
	private void sendMessage(Long chatId, String text, Keyboard key){
		SendMessage request = new SendMessage(chatId, text)
		        .parseMode(ParseMode.HTML)
		        .disableWebPagePreview(true)
		        .disableNotification(true)
		        .replyToMessageId(0)
		        .replyMarkup(key);

		// sync
		bot.execute(request);
//		SendResponse sendResponse = bot.execute(request);
//		boolean ok = sendResponse.isOk();
//		Message message = sendResponse.message();
	}

	private void startMenu(Message message) {
		Lang lang = new Lang(message);
		String[][] kbArray = { new String[]{lang.PLAY_SINGLE_1_MESSAGE},
				new String[]{lang.HELP_MESSAGE},
				new String[]{lang.CONTACT_US}
		};

//		ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup(kbArray,false, true, false);
//		sendMessage(message.chat().id(), lang.TEXT_MENU_1,ParseMode.Markdown,false,0,rkm);
		Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(kbArray)
                .oneTimeKeyboard(true)   // optional
                .resizeKeyboard(true)    // optional
                .selective(true);        // optional
		sendMessage(message.chat().id(), lang.TEXT_MENU_1,replyKeyboardMarkup);
	}

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
		//logica bot
		if (input.equals("help") || input.equals(lang.HELP_MESSAGE))
			help(message);
		else if (input.startsWith("play") || input.startsWith(lang.PLAY_SINGLE_1_MESSAGE))
			playSingle1(message, debug, input);
		else if (input.equals(lang.CONTACT_US))
			contactUs(message);
		else if (input.equals("__manutenzione"))
			maintenance(message);
		else if (!isGameMessage(message, debug, input))
			sorry(message);
		//end if
	}

	private void maintenance(Message message) {
		sendMessage(message.chat().id(),new Lang(message).MAINTENANCE_MESSAGE);
	}

	private void contactUs(Message message) {
		String msg = "info:\nmail->blackrubin@gmail.com\ntelegram->@firebone";//a
		sendMessage(message.chat().id(),msg);
	}

	private boolean isGameMessage(Message message, boolean debug, String token) {
		Long chatID = message.chat().id();
		Statement statement = null;
		try {
			statement = getStatement(message, debug);
			ResultSet rsGame = statement
					.executeQuery("SELECT * FROM game WHERE chatid = '"+chatID+"' AND active = 'Y'");
			if (!rsGame.first()){
				if (debug) error(message, new Lang(message).E001_GAME_NOT_EXISTS);	//la partita non esiste
				return false;
			}
			if (debug) error(message, "GAME: recuperato partita");	
			rsGame.absolute(1); //mi posiziono sul primo record
			//recupero combo
			String combo = rsGame.getString("combo");
			if(combo == null){
				if (debug) error(message, new Lang(message).E003_COMBO_NULL);
				return false;
			}
			if (debug) error(message, "GAME: combo "+combo);
			//controllo che il formato del messaggio in input sia valido
			token = formattaMessaggio(token, combo.length());
			if(token == null){
				if (debug) error(message, new Lang(message).E002_NO_INPUT_VALID+token);	//input non valido 
				return false;
			}
			if (debug) error(message, "GAME: formattato messaggio");
			//recupero numero tentativo
			int tentativi = rsGame.getInt("try") + 1 ;
			String response = "";
			if(token.equals(combo)){//caso migliore: ho indovinato
				response += new Lang(message).WIN_MESSAGE;
				rsGame.updateString("active", "N");//resetto la partita
				rsGame.updateString("win", "Y");//setto lo stato
			}else if(tentativi >= FruitMasterBot.tentativi){//terminato numero tentativi
				response += new Lang(message).RETRY_MESSAGE;
				rsGame.updateString("active", "N");//resetto la partita
				rsGame.updateString("win", "N");//setto lo stato
			}else{//e' un tentativo valido ma non ho indovinato. Passo alla correzione degli errori
				int posCorretta=0, posErrata=0;
				boolean[] posCombo = new boolean[combo.length()];
				boolean[] posToken = new boolean[combo.length()];
				for(int i=0; i<combo.length();i++)
					if(combo.charAt(i)==token.charAt(i)){
						posCorretta++;
						posCombo[i] = true;
						posToken[i] = true;
					}

				for(int i=0; i<token.length();i++)		//loop su token
					for(int j=0; j<combo.length();j++){	//loop su combo
						if (i==j) continue;
						if (posCombo[j]||posToken[i]) continue;
						if (combo.charAt(i)!=token.charAt(j)) continue;
						posErrata++;
						posCombo[j] = true;
						posToken[i] = true;
					}
				error(message, ""+ posCorretta + " corrette, " + posErrata + " corrette in posizione errata");
				rsGame.updateInt("try", tentativi);
				response = new Lang(message).TRY_NR+tentativi+": ";
			}
			//rsGame.updateTimestamp("chtimestamp", new Timestamp(System.currentTimeMillis()));
			rsGame.updateRow();	//aggiornamento dati a DB
			sendMessage(message.chat().id(),response);
		} catch (Exception e) {
			if(debug) 
				error(message, e);
			else 
				error(message, new Lang(message).E000_GENERIC_ERROR);
		}
		return true;
	}

	private String formattaMessaggio(String text, int comboLen) {
		text = text.replaceAll(" ", "");
		text = text.replaceAll("/", "");
		if ( text.length() != comboLen) return null;
		return text;
	}

	private void sorry(Message message) {
		if(message.chat().type().equals(Chat.Type.Private))
			sendMessage(message.chat().id(), new Lang(message).SORRY_MESSAGE);
	}

	private void playSingle1(Message message, boolean debug, String args) {
		Lang lang = new Lang(message);
		String[] arg = args.split(" ");
		sendMessage(message.chat().id(), lang.GENERATE_FRUIT_MIX_MESSAGE);
		//---genero combinazione---//
		int comboLen = 4;
		if ( arg.length>=2 && arg[1].matches("[4-9]")) comboLen = Integer.parseInt(arg[1]);
		String combo = "";
		for(int i = 0; i<comboLen; i++)
			combo += FRUTTI[((int)(Math.random()*1000))%FRUTTI.length];
		//salvo il file della partita
		//disattivo eventuali partite attive
		Statement statement = null;
		try{
			statement = getStatement(message, debug);
			//recupero il contatore
			ResultSet rsGame = statement.executeQuery("SELECT MAX(count) as count FROM game WHERE chatid = '"+ message.chat().id() + "'");
			long nextCount = (rsGame.first())?rsGame.getLong("count") + 1 : 1;
			//reset eventuali partite attive
			rsGame = statement.executeQuery("SELECT * FROM game WHERE chatid = '"+ message.chat().id() + "' and active = 'Y'");
			if (debug) error(message, "PLAYSINGLE1: ricerca vecchia partita: "+ rsGame);
			if (rsGame.first()){//se trovato qualcosa
				rsGame.absolute(1);
				if (debug) error(message, "trovata vecchia partita: "+ rsGame.getString("chatid") + '-' +  rsGame.getLong("count"));
				do{
					rsGame.updateString("active", "N");
					rsGame.updateRow();
				}while(rsGame.next());
			}
			if (debug) error(message, "partita numero: "+ nextCount);
			//avvio nuova partita
			rsGame.moveToInsertRow();
			rsGame.updateLong("chatid", message.chat().id());
			rsGame.updateLong("count", nextCount);
			rsGame.updateString("combo", combo);
			rsGame.updateInt("try", 1);
			rsGame.updateTimestamp("crtimestamp", new Timestamp(System.currentTimeMillis()));
			rsGame.updateString("active", "Y");
			rsGame.insertRow();
			rsGame.moveToCurrentRow();
			if (debug) error(message, "NEWGAME: creata nuova partita: "+ rsGame);
			sendMessage(message.chat().id(), lang.END_FRUIT_MIX_P1_MESSAGE+tentativi+lang.END_FRUIT_MIX_P2_MESSAGE);
			sendMessage(message.chat().id(), new Lang(message).TRY_NR+1+": ");
			log(message, "generata la combo: "+combo+'\n', debug);
		}catch(Exception e){
			if(debug) error(message, e);
		}
	}

	private void help(Message message) {
		// TODO Auto-generated method stub
		sendMessage(message.chat().id(), new Lang(message).WIP_MESSAGE);
	}

	private void error(Message message, Exception e) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < e.getStackTrace().length; i++) {
			sb.append(""+ e.getStackTrace()[i] + '\n');
		}
		sendMessage(message.chat().id(), "" + e.getClass() + ':' + e.getMessage() + '\n' + sb.toString());
		//log(message , "" + e.getClass() + ':' + e.getMessage() + ' ' + sb.toString(), false);
	}

	private void error(Message message, String s) {
		sendMessage(message.chat().id(), s);
	}

	private Statement getStatement(Message message, boolean debug) throws SQLException{
		if (conn == null || conn.isClosed()){
			try {
				//				Class.forName("com.mysql.jdbc.Driver");
				//				Class.forName("com.mysql.fabric.jdbc.Driver");
				//				conn    = (Connection) DriverManager.getConnection(
				//						"jdbc:mysql://127.6.211.2:32674/fruitmaster",
				//						"adminq1HeHLQ",
				//						"5dBUip1aD2Wh");
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				conn = DriverManager.getConnection("jdbc:mysql://127.6.211.2/fruitmaster",
						"adminq1HeHLQ","5dBUip1aD2Wh");
			} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				if (debug) error(message, e);
			}
		}
		return conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
	}

	private void log(Message message, String log, boolean debug) {
		Statement statement = null;
		try{
			statement = getStatement(message, debug);
			//fare select sulla tabella degli utenti con chatID
			ResultSet rsUser = statement.executeQuery("SELECT * FROM user WHERE chatid = '"+ message.chat().id() + "'");
			if (debug) error(message, "LOG: ricerca chatid anagrafato: "+ rsUser);
			if ( !rsUser.first() ){//se non trovo utente, anagrafare
				rsUser.moveToInsertRow();
				rsUser.updateLong("chatid", message.chat().id());
				rsUser.updateString("username", message.from().username());
				rsUser.updateString("firstname", message.from().firstName());
				rsUser.updateString("lastname", message.from().lastName());
				rsUser.insertRow();
				rsUser.moveToCurrentRow();
				if (debug) error(message, "LOG: l'utente e' stato anagrafato");
			} else { 
				if (debug) error(message, "LOG: utente gia' anagrafato");
			}
			//log
			ResultSet rsLog = statement.executeQuery("SELECT MAX(count) as count FROM log WHERE chatid = '"+ message.chat().id() + "'"); //recupero la tabella
			if (debug) error(message, "LOG: recupero tabella log: "+ rsLog);
			//se ho trovato un MAX allora lo assegno, altrimenti e' il primo log
			long nextCount = (rsLog.first())? rsLog.getLong("count") + 1 : 1;
//			rsLog.close();
			//recupero uno statement aggiornabile
			rsLog = statement.executeQuery("SELECT * FROM log WHERE chatid = '"+ message.chat().id() + "'");
			rsLog.moveToInsertRow();
			rsLog.updateLong("chatid", message.chat().id());
			rsLog.updateLong("count", nextCount);
			rsLog.updateString("message", log);
			rsLog.insertRow();
			rsLog.moveToCurrentRow();
			if (debug) error(message, "LOG: log effettuato: "+ rsLog);
		}catch(Exception e){
			if(debug)
				error(message, e);
			else
				error(message, "unexpected error: contact the developer (@firebone)");
		}
	}


}
