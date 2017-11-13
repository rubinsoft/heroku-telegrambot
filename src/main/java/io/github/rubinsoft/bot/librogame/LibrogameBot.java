package io.github.rubinsoft.bot.librogame;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

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

import io.github.rubinsoft.bot.fruitmasterbot.utils.Lang;
import io.github.rubinsoft.bot.librogame.storybuilder.StoryBuilder;
import io.github.rubinsoft.pengrad.openshift.BotHandler;

/**
 * @since 29.07.2017
 * @author firebone
 *
 */
@SuppressWarnings("deprecation")
@WebServlet("/mylgb")
public class LibrogameBot extends BotHandler {
	private static final long serialVersionUID = 1L;
	public static final String BOT_TOKEN = "183511958:AAEkGp_G-jV2IUyzQSL4RVXQSnP8WSom8ho";
//	public static final String WEB_HOOK= "https://fruitmaster-botfactory.rhcloud.com/mylgb";

	TelegramBot bot = TelegramBotAdapter.buildDebug(BOT_TOKEN);

	public Connection conn;

	@Override
	protected boolean onStart(Message message) {
		//inizializzo la lingua
		Lang lang = new Lang(message);
		Long chatId = message.chat().id();
		//messaggio di benvenuto
		sendMessage(chatId, lang.LGB_START_MESSAGE );
		//genero il menu
		startMenu(message);
		log(message, "has joined", false);
		return true;
	}

	private void sendMessage(Long chatId, String text){
		SendMessage request = new SendMessage(chatId, text)
				.parseMode(ParseMode.HTML)
				.disableWebPagePreview(true)
				.disableNotification(true);
		//		        .replyToMessageId(0)
		//				.replyMarkup(new ForceReply());
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
				//		        .replyToMessageId(0)
				.replyMarkup(key);

		// sync
		bot.execute(request);
		//		SendResponse sendResponse = bot.execute(request);
		//		boolean ok = sendResponse.isOk();
		//		Message message = sendResponse.message();
	}

	private void startMenu(Message message) {
		Lang lang = new Lang(message);
		choosesToMessage(message.chat().id(), 
				lang.LGB_TEXT_MENU_1, 
				new String[]{lang.LGB_PLAY_SINGLE_1_MESSAGE,
						lang.LGB_LOAD_STORY,
						lang.LGB_HELP_MESSAGE,
						lang.LGB_CONTACT_US}
				);
	}

	private void choosesToMessage(Long chatId, String text, String[] strings ){
		String[][] al = new String[strings.length][1];
		for(int i=0; i<strings.length; i++){
			al[i][0] = strings[i];
		}
		Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(al)
				.oneTimeKeyboard(false)   // optional
				.resizeKeyboard(true)    // optional
				.selective(true);        // optional
		sendMessage(chatId, text, replyKeyboardMarkup);
	}

	protected void onWebhookUpdate(Update update) {
		Message message = update.message();
		GameConstants.inizialize();
		Lang lang = new Lang(message);
		String input = message.text();
		boolean debug = false;
		//parsing messaggio
		if (input == null || input.equals("")) return;	// se messaggio null, esci
		input = input.trim();
		if (input.charAt(0) == '/') 					// elimino eventuali caratteri di comando
			input = input.substring(1, input.length());
		if (input.toLowerCase().startsWith("--debug")){ // attivazione debug
			debug = true;
			input = input.substring(7,input.length()).trim();
		}
		if(debug) error(message.chat().id(), "Debug attivo.");
		if(debug) error(message.chat().id(), "input: "+input);
		//logica bot
		if (input.equalsIgnoreCase("help") || input.equalsIgnoreCase(lang.LGB_HELP_MESSAGE))
			help(message);
		else if (input.equalsIgnoreCase(lang.LGB_END_OF_STORY))
			startMenu(message);
		else if (input.equalsIgnoreCase(lang.LGB_LOAD_STORY))
			sendMessage(message.chat().id(),new Lang(message).LGB_LOAD_YOUR_STORY.replaceAll("&1", ""+message.chat().id()));
		else if (continueGame(message, debug, input)) return;
		else if (input.toLowerCase().startsWith(lang.LGB_PLAY_SINGLE_1_MESSAGE))
			playSingle1(message, debug, input);
		else if (input.equalsIgnoreCase(lang.LGB_CONTACT_US))
			contactUs(message);
		else if (!isGameMessage(message, debug, input))
			sorry(message);
		//end if
	}

	private boolean continueGame(Message message, boolean debug, String input) {
		Long chatId = message.chat().id();
		Statement statement = null;
		try {
			statement = getStatement(message, debug);
			ResultSet rsGame = statement.executeQuery("SELECT * FROM game WHERE chatid = '"+chatId+"' AND active = 'Y'");
			if (!rsGame.first()) return false; //la partita non esiste
			rsGame.absolute(1);
			String storyTitle = rsGame.getString("storyTitle");
			if (storyTitle == null) throw new IllegalArgumentException("Game title not found: "+storyTitle);
			String lastCap = rsGame.getString("lastCap");
			//carico lo zaino
			Zaino equip = new Zaino(rsGame.getString("backpack"));
			if(input.equals(new Lang(message).LGB_BACKPACK) || input.equals(new Lang(message).LGB_STAT)){
				if(debug) error(chatId, equip.serializza());
				else sendMessage(chatId, equip.getContenuto());
				return true; 
			}

			//trucchi :-)
			if(input.startsWith("--cheaton")){
				rsGame.updateString("cheatOn", "Y");
				rsGame.updateRow();
				sendMessage(chatId, "cheat ON!");
				return true; 
			}
			if(isACheat(rsGame, input, equip))
				return true;
			//carico la partita
			ResultSet rsStory = statement.executeQuery("SELECT * FROM story WHERE storyTitle = '"+storyTitle+"'");
			if (!rsStory.first()) throw new IllegalArgumentException("This story not exists: "+storyTitle);
			rsStory.absolute(1);
			StoryBuilder story = new StoryBuilder(new ByteArrayInputStream(rsStory.getString("storyContent").getBytes(StandardCharsets.UTF_8)));
			story.setZaino(equip);
			for (Exception w : story.getWarningsList())
				if(debug) sendMessage(chatId, w.getMessage());
			//analizzo l'input - verifica rispetto al cap corrente
			boolean sceltaTrovata = false;
			String nextCap = null;
			for(Scelta s:story.getListaScelte(lastCap)){
				if (s.getText().equals(input)) {
					sceltaTrovata = true;
					String sceltaNonConsentita = sceltaApplicabile(s, equip);
					if(sceltaNonConsentita != null){
						sendMessage(chatId, sceltaNonConsentita);
						return true;
					}
					nextCap = s.nextCap();
				}
			}
			if(input.startsWith("--goto")&&rsGame.getString("cheatOn").equals("Y")){
				nextCap=input.substring(7, input.length()).trim();
				sceltaTrovata=true;
			}
			//se la risposta non e' valida, ritorno falso ed esco
			if (!sceltaTrovata) {
				return false;
			}
			//se la risposta e' valida, processo la scelta e vado al cap successivo o termino la partita
			boolean eos = display(chatId, debug, story, nextCap);
			//rileggo il salvataggio perche' l'ho sovrascritto a causa della SELECT sulla storia
			rsGame = statement.executeQuery("SELECT * FROM game WHERE chatid = '"+chatId+"' AND active = 'Y'");
			rsGame.absolute(1);//a questo punto esiste sicuramente
			if(eos){//partita terminata
				rsGame.updateString("active", "N");
			}else{
				rsGame.updateString("lastCap", nextCap);
			}
			rsGame.updateString("backpack", equip.serializza());
			rsGame.updateTimestamp("chtimestamp", new Timestamp(System.currentTimeMillis()));
			rsGame.updateRow();
		} catch (Exception e) {
			if(debug) 
				error(message.chat().id(), e);
			else 
				error(message.chat().id(), new Lang(message).E000_GENERIC_ERROR);
		}
		return true;
	}

	private boolean isACheat(ResultSet rsGame, String input, Zaino zaino ) throws Exception {
		if(!rsGame.getString("cheatOn").equals("Y")) return false;
		if(input.startsWith("--add")){
			input=input.substring(6, input.length());
			zaino.addOggetto(new Oggetto(input));
			rsGame.updateString("backpack", zaino.serializza());
			rsGame.updateRow();
			return true; 
		}
		if(input.startsWith("--remove")){
			input=input.substring(9, input.length());
			zaino.removeOggetto(new Oggetto(input));
			rsGame.updateString("backpack", zaino.serializza());
			rsGame.updateRow();
			return true; 
		}
		return false;
	}

	private String sceltaApplicabile(Scelta s, Zaino zaino) {
		for(Oggetto o:s.getListaOggettiObbligatori())
			if(!zaino.containsOggetto(o)){
				String ret = new Lang().LGB_CANNOT_SELECT_1;
				ret = ret.replaceAll("&1", o.getNome()).replaceAll("&2", ""+o.getQuantita());
				return ret;
			}
		for(Oggetto o:s.getListaOggettiRichiesti())
			if(!zaino.containsOggetto(o)){
				String ret = new Lang().LGB_CANNOT_USE_1;
				ret = ret.replaceAll("&1", o.getNome()).replaceAll("&2", ""+o.getQuantita());
				return ret;
			}
			else
				zaino.removeOggetto(o);
		for(Oggetto o:s.getListaOggettiRaccolti())
			zaino.addOggetto(o);
		return null;
	}

	private void contactUs(Message message) {
		String msg = "info:\nmail->blackrubin@gmail.com\ntelegram->@firebone";
		sendMessage(message.chat().id(),msg);
	}

	private boolean isGameMessage(Message message, boolean debug, String token) {
		return false;
	}

	private void sorry(Message message) {
		if(message.chat().type().equals(Chat.Type.Private))
			sendMessage(message.chat().id(), new Lang(message).LGB_SORRY_MESSAGE);
	}

	private void playSingle1(Message message, boolean debug, String args) {
		Long chatId = message.chat().id();
		//log(message, "", false);
		String[] arg = args.split("-");
		if(debug) error(message.chat().id(), "args len:" + arg.length);
		if(debug) for(String s:arg) error(message.chat().id(), "arg:"+s);
		Statement statement = null;
		try{
			statement = getStatement(message, debug);
			if(arg.length == 1){// non e' stata specificata una storia, fornisco la lista
				ResultSet rsStory = statement.executeQuery("SELECT * FROM story WHERE active = 'Y' AND ( alfaStory='N' OR (alfaStory='Y' AND author='"+message.chat().id()+"'))");
				if(!rsStory.first()){
					sendMessage(chatId, new Lang(message).LGB_NO_STORY);
					return;
				}
				ArrayList<String> elencoStorie = new ArrayList<>();
				rsStory.absolute(1);
				do{
					String alfa = (rsStory.getString("alfaStory").equals("Y"))?" - [alfa]":"";
					elencoStorie.add(new Lang(message).LGB_PLAY_SINGLE_1_MESSAGE + " - " + rsStory.getString("storyTitle") + alfa);
				}while(rsStory.next());
				choosesToMessage(message.chat().id(), new Lang(message).LGB_STORY_LIST, elencoStorie.toArray(new String[elencoStorie.size()]));
				return;
			}//altrimenti e' maggiore o uguale a 2
			String storyTitle = arg[1].trim();
			//recupero il contatore
			ResultSet rsGame = statement.executeQuery("SELECT MAX(count) as count FROM game WHERE chatid = '"+ message.chat().id() + "'");
			long nextCount = (rsGame.first())?rsGame.getLong("count") + 1 : 1;
			//reset eventuali partite attive
			rsGame = statement.executeQuery("SELECT * FROM game WHERE chatid = '"+ message.chat().id() + "' and active = 'Y'");
			if (debug) error(message.chat().id(), "PLAYSINGLE1: ricerca vecchia partita: "+ rsGame);
			if (rsGame.first()){//se trovato qualcosa
				rsGame.absolute(1);
				do{
					if (debug) error(message.chat().id(), "trovata vecchia partita: "+ rsGame.getString("chatid") + '-' +  rsGame.getLong("count"));
					rsGame.updateString("active", "N");
					rsGame.updateRow();
				}while(rsGame.next());
			}
			if (debug) error(message.chat().id(), "partita numero: "+ nextCount);
			//avvio nuova partita
			rsGame.moveToInsertRow();
			rsGame.updateLong("chatid", message.chat().id());
			rsGame.updateLong("count", nextCount);
			rsGame.updateString("lastCap", "1");
			rsGame.updateString("storyTitle", storyTitle);
			rsGame.updateTimestamp("crtimestamp", new Timestamp(System.currentTimeMillis()));
			rsGame.updateString("active", "Y");
			rsGame.insertRow();
			rsGame.moveToCurrentRow();
			if (debug) error(message.chat().id(), "PLAYSINGLE1: creata nuova partita: "+ rsGame);

			//carico la partita
			ResultSet rsStory = statement.executeQuery("SELECT * FROM story WHERE storyTitle = '"+storyTitle+"' and active = 'Y'");
			if (!rsStory.first()) throw new IllegalArgumentException("This story not exists: "+storyTitle);
			StoryBuilder story = new StoryBuilder(new ByteArrayInputStream(rsStory.getString("storyContent").getBytes(StandardCharsets.UTF_8)));
			display(message.chat().id(), debug, story, "1");
		}catch(Exception e){
			if(debug) 
				error(message.chat().id(), e);
			else
				error(message.chat().id(), new Lang(message).LGB_E001_GENERIC_ERROR);
		}
	}

	private boolean display(Long chatId, boolean debug, StoryBuilder story, String cap) throws Exception {
		for (Exception w : story.getWarningsList())
			if(debug) sendMessage(chatId, w.getMessage());
		String text = "<i>"+story.getCapSubtitle(cap) + "</i>\n" + story.getCapText(cap);
		//interprete comandi
		text = text.replaceAll("&capid&", cap)
				.replaceAll("&italicOn&", "<i>")
				.replaceAll("&italicOff&", "</i>")
				.replaceAll("&boldOn&", "<b>")
				.replaceAll("&boldOff&", "</b>");
		
		if(debug) error(chatId, "text: " + text);
		if(story.getListaScelte(cap)==null){//partita terminata
			choosesToMessage(chatId, text, new String[]{new Lang().LGB_END_OF_STORY});
			return true;
		}else{
			ArrayList<String> strings = new ArrayList<>();
			for(Scelta s:story.getListaScelte(cap))
				strings.add(s.getText());
			if(debug) for(String s:strings) error(chatId, "Scelte: "+s);
			choosesToMessage(chatId, text, strings.toArray(new String[strings.size()]));
		}
		return false;
	}

	private void help(Message message) {
		sendMessage(message.chat().id(), new Lang(message).LGB_HELP_MESSAGE_RESP);
	}

	private void error(Long chatId, Exception e) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < e.getStackTrace().length; i++) {
			sb.append(""+ e.getStackTrace()[i].toString().replaceAll("<", "&lt;").replaceAll(">", "&gt") + '\n');
		}
		sendMessage(chatId, "" + e.getClass() + ':' + e.getMessage() + '\n' + sb.toString());
	}

	private void error(Long chatId, String s) {
		sendMessage(chatId, s);
	}

	private Statement getStatement(Message message, boolean debug) throws SQLException{
		if (conn == null || conn.isClosed()){
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				conn = DriverManager.getConnection("jdbc:mysql://127.6.211.2/librogame?useUnicode=true&characterEncoding=UTF-8",
						"adminq1HeHLQ","5dBUip1aD2Wh");
			} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				if (debug) error(message.chat().id(), e);
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
			if (debug) error(message.chat().id(), "LOG: ricerca chatid anagrafato: "+ rsUser);
			if ( !rsUser.first() ){//se non trovo utente, anagrafare
				rsUser.moveToInsertRow();
				rsUser.updateLong("chatid", message.chat().id());
				rsUser.updateString("username", message.from().username());
				rsUser.updateString("firstname", message.from().firstName());
				rsUser.updateString("lastname", message.from().lastName());
				rsUser.insertRow();
				rsUser.moveToCurrentRow();
				if (debug) error(message.chat().id(), "LOG: l'utente e' stato anagrafato");
			} else { 
				if (debug) error(message.chat().id(), "LOG: utente gia' anagrafato");
			}
			//log
			//			ResultSet rsLog = statement.executeQuery("SELECT MAX(count) as count FROM log WHERE chatid = '"+ message.chat().id() + "'"); //recupero la tabella
			//			if (debug) error(message.chat().id(), "LOG: recupero tabella log: "+ rsLog);
			//			//se ho trovato un MAX allora lo assegno, altrimenti e' il primo log
			//			long nextCount = (rsLog.first())? rsLog.getLong("count") + 1 : 1;
			////			rsLog.close();
			//			//recupero uno statement aggiornabile
			//			rsLog = statement.executeQuery("SELECT * FROM log WHERE chatid = '"+ message.chat().id() + "'");
			//			rsLog.moveToInsertRow();
			//			rsLog.updateLong("chatid", message.chat().id());
			//			rsLog.updateLong("count", nextCount);
			//			rsLog.updateString("message", log);
			//			rsLog.insertRow();
			//			rsLog.moveToCurrentRow();
			//			if (debug) error(message.chat().id(), "LOG: log effettuato: "+ rsLog);
		}catch(Exception e){
			if(debug)
				error(message.chat().id(), e);
			else
				error(message.chat().id(), "unexpected error: contact the developer (@firebone)");
		}
	}


}
