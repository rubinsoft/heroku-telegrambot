package io.github.rubinsoft.bot.zodiac;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

import javax.servlet.annotation.WebServlet;

import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.SendMessage;

import io.github.rubinsoft.pengrad.openshift.BotHandler;
import io.github.rubinsoft.pengrad.openshift.DBConnector;

/**
 * @author firebone
 * @version V1 - 20161217
 */
@WebServlet("/myzodiac")
public class ZodiacBot extends BotHandler {
	private static final long serialVersionUID = 8104139281064756967L;
	public static final String BOT_TOKEN = "317861028:AAGCfG9Cy8rwb3_gjv6r4CTnbWaCvfVIXto";
	public static final String WEB_HOOK = "https://zodiac-botfactory.rhcloud.com/myzodiac";

	@SuppressWarnings("deprecation")
	TelegramBot bot = TelegramBotAdapter.buildDebug(BOT_TOKEN);

	private void sendMessage(long chatId, String text){
		SendMessage request = new SendMessage(chatId, text)
				.parseMode(ParseMode.HTML)
				.disableWebPagePreview(true)
				.disableNotification(true);
		//				.replyToMessageId(message.messageId());
		//		        .replyMarkup(new ForceReply());
		//				.replyMarkup(new ReplyKeyboardRemove());

		// sync
		bot.execute(request);
		//		SendResponse sendResponse = bot.execute(request);
		//		boolean ok = sendResponse.isOk();
		//		Message message = sendResponse.message();
	}

	@Override
	protected boolean onStart(Message message) {
		Long chatId = message.chat().id();
		// messaggio di benvenuto
		sendMessage(chatId, Constants.WELCOME);
		// salvo le statistiche
		String name = message.chat().firstName() + '[' + message.chat().username() + ']';
		log(message, "" + name + " si e' unito/a a noi.", false);
		return true;
	}

	@Override
	protected void onWebhookUpdate(Update update) {
		Message message = update.message();
		if(isInlineMessage(update.inlineQuery())) return;
		String input = message.text();
		boolean messageProcessed = false;
		boolean debug = false;
		updateDictionary(message, debug, false); // aggiorno il dizionario se necessario
		//parsing messaggio
		if (input == null || input.equals("")) return;	// se messaggio null, esci
		input = input.toLowerCase().trim();
		if (input.charAt(0) == '/') 					// elimino eventuali caratteri di comando
			input = input.substring(1, input.length());
		if (input.startsWith("--debug")){ 				// attivazione debug
			debug = true;
			input = input.substring(7,input.length()).trim();
		}
		if(debug) error(message, "Debug attivo. ");
		if(debug) error(message, "input: "+input);
		//logica bot
		try {
			if (!messageProcessed) messageProcessed = changelog(message, debug, input);
			if (!messageProcessed) messageProcessed = forceRefresh(message, debug, input);
			if (!messageProcessed) messageProcessed = oroscopoSegno(message, debug, input);
			if (!messageProcessed) 
				if(message.chat().type().equals(Chat.Type.Private))
					error(message, "Scusa, non ho capito.");
		} catch (Exception e) {
			if(debug)
				error(message, e);
			else
				error(message, "unexpected error: contact the developer (@firebone)");
		} // se il cast da eccezione, esco

	}

	private boolean forceRefresh(Message message, boolean debug, String input) {
		if(message.from().username().equalsIgnoreCase("firebone")
				&& input.equalsIgnoreCase("--refresh")){
			String upd = updateDictionary(message, debug, true);
			if(upd!=null){
				sendMessage(message.chat().id(), upd);
				return true;
			}
		}
		return false;
	}

	private boolean isInlineMessage(InlineQuery inlineQuery) {
		if(inlineQuery == null) return false; 
		String resp = null;
		try{
			resp = oroscopoSegnoInline(inlineQuery, inlineQuery.query());
		}catch(Exception e) { return false; } // se non riconosco una stringa valida, esco
		if(resp==null || resp.equals("")) return false;
		InlineQueryResultArticle r1 = new InlineQueryResultArticle(inlineQuery.id(), "Oroscopo del segno: "+inlineQuery.query(), "**"+inlineQuery.query()+"**\n"+resp);
		bot.execute(new AnswerInlineQuery(inlineQuery.id(), r1));
		return true;
	}


	private boolean changelog(Message message, boolean debug, String input) {
		if (input.equalsIgnoreCase("changelog")
				|| input.equals("news") ){
			String news = "ChangeLog v1.3:\n-Inserito il tredicesimo segno (Ofiuco)\n-Aggiunto oroscopo cinese\n-Interventi di miglioramento delle performance";
			error(message, news);
			log(message, "asked for new functions", debug);
			return true;
		}
		return false;
	}

	private String updateDictionary(Message message, boolean debug, boolean force){
		String resp = null;
		try{
			String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			Double lastUpd = Double.parseDouble(DBConnector.getParam("zodiac", "_lastUpdate"));
			Double lastSWUpd = Double.parseDouble(DBConnector.getParam("zodiac", "_lastSWUpdate"));
			if(debug) error(message, "comparing: "+lastUpd+" and "+new Date());
			//se ho superato la data di previsto UPD e non ho gia' fatto UPD
			// oppure sto facendo una richiesta di UPD forzato
			if((lastUpd < Double.parseDouble(timestamp) && lastSWUpd < lastUpd) 
					|| force){
				new UpdateDictionary().doGet(null, null);
				//aggiorno lastSWUpd
				DBConnector.setParam("zodiac","_lastSWUpdate", timestamp);
				log(message, "has updated dictionary", debug);
			}
		} catch (Exception e) {
			error(message, e);
		}
		return resp;
	}

	/**
	 * invia messaggio dividendo per:
	 * Generale
	 * Famiglia
	 * Lavoro
	 * Chiusura
	 * @param message
	 * @param input
	 * @return 
	 * @throws FileNotFoundException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private String oroscopoSegnoInline(InlineQuery inlineQuery, String input) throws FileNotFoundException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		long seed = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new Date()))*100; //seed = data concatenata con 2 zeri
		
		int segno=0;
		segno = recuperaSegno(input);
		seed = seed + segno;
		log(Long.parseLong(inlineQuery.id()), 
				inlineQuery.from().firstName(), 
				inlineQuery.from().username(), 
				"no data", 
				"seed:"+seed,
				"inline", 
				false);
		return generaOroscopo(seed);
	}

	/**
	 * invia messaggio dividendo per:
	 * Generale
	 * Famiglia
	 * Lavoro
	 * Chiusura
	 * @param message
	 * @param input
	 * @return 
	 * @throws FileNotFoundException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private boolean oroscopoSegno(Message message, boolean debug, String input) throws FileNotFoundException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		long seed = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new Date()))*100; //seed = data concatenata con 2 zeri
		String jsonString = jsonFile2String(new File(Constants.LOCAL_DB_PATH));
		if (jsonString == null || jsonString.length() == 0){
			error(message, "Impossibile recuperare il Dizionario");
			return false;
		}
		if(debug) error(message, "dictionary length: "+jsonString.length());
		//<---26.01.17: START conversione da CASE a segno da DB --->//
		int segno=0;
		try{
			segno = recuperaSegno(input);
			seed = seed + segno;
		}catch(Exception e){
			if(debug) error(message, "for input:" + input + "-> result: " + segno);
			if(debug) error(message, e);
			return false;
		}
		//<---26.01.17: END conversione da CASE a segno da DB --->//
		String resp = generaOroscopo(seed);
		sendMessage(message.chat().id(), resp);
		log(message, "seed:"+seed, debug);
		return true;
	}
	/**
	 * metodo per prelevare l'intero corrispondente al segno passato in input da DB
	 * @param message
	 * @param debug
	 * @param input
	 * @return
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private int recuperaSegno(String input) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Statement statement;
		int segnoValue = -1;
		statement = DBConnector.getStatement("zodiac");
		//JsonArray generale = object.get("generale").asArray();
		//TODO
		ResultSet rsDictionary = statement.executeQuery("SELECT * FROM segno WHERE type = 'generale'");
		
		
//		mongoClient = getDatabaseInstance();
//		MongoDatabase mdb = mongoClient.getDatabase("zodiac");
//		//		if(debug) error(message, "parsing input.. : "+input);
//		MongoCollection<Document> coll = mdb.getCollection("options");
//		//		if(debug) error(message, "getting DB options..");
//		Document doc = coll.find().iterator().next();
//		//		if(debug) error(message, "trying to connect to DB for Input : "+input);
		
//		//prendo l'array dei segni e cerco l'input
//		for(JsonValue jv:Json.parse(doc.toJson()).asObject().get("segno").asArray()){
//			JsonValue result_=jv.asObject().get(input);
//			if (result_ != null) {
//				segnoValue = Integer.parseInt(result_.asString());
//				break;
//			}
//		}
		//se non lo trovo, return false
		if(segnoValue <= 0 ) throw new IllegalArgumentException("error while parsing input from DB \"Segno\"");
		return segnoValue;
	}

	private String generaOroscopo(long seed) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		StringBuilder sb = new StringBuilder();
		Statement statement = DBConnector.getStatement("zodiac");
		//JsonArray generale = object.get("generale").asArray();
		ResultSet rsDictionary = statement.executeQuery("SELECT * FROM dictionary WHERE active = 'Y' AND type = 'generale'");
		rsDictionary.absolute((int)((seed+1)%rsDictionary.getFetchSize()));
		sb.append(""+rsDictionary.getString("text")+". ");

//		JsonArray lavoro = object.get("lavoro").asArray();
//		sb.append(""+lavoro.get((int)((seed+2)%lavoro.size())).asObject().getString("text", "")+". ");
		rsDictionary = statement.executeQuery("SELECT * FROM dictionary WHERE active = 'Y' AND type = 'lavoro'");
		rsDictionary.absolute((int)((seed+1)%rsDictionary.getFetchSize()));
		sb.append(""+rsDictionary.getString("text")+". ");

//		JsonArray famiglia = object.get("famiglia").asArray();
//		sb.append(""+famiglia.get((int)((seed+3)%famiglia.size())).asObject().getString("text", "")+". ");
		rsDictionary = statement.executeQuery("SELECT * FROM dictionary WHERE active = 'Y' AND type = 'famiglia'");
		rsDictionary.absolute((int)((seed+1)%rsDictionary.getFetchSize()));
		sb.append(""+rsDictionary.getString("text")+". ");

//		JsonArray chiusura = object.get("chiusura").asArray();
//		sb.append(""+chiusura.get((int)((seed+4)%chiusura.size())).asObject().getString("text", ""));
		rsDictionary = statement.executeQuery("SELECT * FROM dictionary WHERE active = 'Y' AND type = 'chiusura'");
		rsDictionary.absolute((int)((seed+1)%rsDictionary.getFetchSize()));
		sb.append(""+rsDictionary.getString("text")+". ");

		return sb.toString();
	}

	private String jsonFile2String(File f) throws FileNotFoundException{
		StringBuilder sb = new StringBuilder();
		Scanner sc = new Scanner(f);
		while (sc.hasNext()) {
			sb.append(sc.nextLine());
		}
		sc.close();
		return sb.toString();
	}

	private void error(Message message, Exception e) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < e.getStackTrace().length; i++) {
			sb.append(""+ e.getStackTrace()[i] + '\n');
		}
		sendMessage(message.chat().id(), "" + e.getClass() + ':' + e.getMessage() + '\n' + sb.toString());
		log(message , "" + e.getClass() + ':' + e.getMessage() + ' ' + sb.toString(), false);
	}

	private void error(Message message, String s) {
		sendMessage(message.chat().id(), s);
	}

	private void log(Message message, String log, boolean debug) {
		try{
			log(message.chat().id(), 
					message.from().firstName(), 
					message.from().username(), 
					(message.location()==null)?"no data":message.location().toString(), 
							log, 
							"classic",
							debug);
		}catch(Exception e){
			if(debug)
				error(message, e);
			else
				error(message, "unexpected error: contact the developer (@firebone)");
		}
	}

	private void log(Long chatID, String firstname, String username, String location, String log, String mode, boolean debug) {
		String timestamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
		//TODO - adattare il log a mysql
//		mongoClient = getDatabaseInstance();
//		MongoDatabase mdb = mongoClient.getDatabase("zodiac");
//		//mdb.createCollection("log");
//
//		Document fullLog = new Document();
//		fullLog.put("timestamp", timestamp);
//		fullLog.put("chatID", chatID);
//		fullLog.put("firstname", firstname);
//		fullLog.put("username", username);
//		fullLog.put("location", location);
//		fullLog.put("callType", mode);
//		fullLog.put("log", log);
//
//		MongoCollection<Document> coll = mdb.getCollection("log");
//		coll.insertOne(fullLog);
	}

//	private MongoClient getDatabaseInstance(){
//		if(mongoClient == null) {
//			MongoCredential mongoCredential = MongoCredential.createCredential("admin", "zodiac","lwU73Rc1nQld".toCharArray());
//			return new MongoClient(new ServerAddress("127.13.108.2", 27017), Arrays.asList(mongoCredential));
//		}
//		return mongoClient;
//	}

}
