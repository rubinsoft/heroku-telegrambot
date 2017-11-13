package io.github.rubinsoft.bot.rpgdice;

import javax.servlet.annotation.WebServlet;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.SendMessage;

import io.github.rubinsoft.pengrad.openshift.BotHandler;

/**
 * @author firebone
 * @version V1 - 20161211
 */
@SuppressWarnings("deprecation")
@WebServlet("/myrpgdice")
public class RPGDiceBot extends BotHandler {
	private static final long serialVersionUID = -70242415886803350L;
	public static final String BOT_TOKEN = "314456611:AAFFQci-805XsTx7qql17uuxmq2YXQvp3EM";
	public static final String WEB_HOOK = "https://bots-botfactory.a3c1.starter-us-west-1.openshiftapps.com/myrpgdice";

	TelegramBot bot = TelegramBotAdapter.buildDebug(BOT_TOKEN);
	// private Lang lang;//classe che contiene le stringhe di comunicazione con
	// l'utente.

	private void sendMessage(Message message, String text){
		SendMessage request = new SendMessage(message.chat().id(), text)
				.parseMode(ParseMode.HTML)
				.disableWebPagePreview(true)
				.disableNotification(true)
				.replyToMessageId(message.messageId());
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
		//		Long chatId = message.chat().id();
		// messaggio di benvenuto
		sendMessage(message, "RPG dice Bot:\nformat examples: /1d6, /3d10, /5d20");
		// salvo le statistiche
		//		try {
		//			String name = message.chat().firstName() + '[' + message.chat().username() + ']';
		//			log(message, "" + name + " si e' unito/a a noi.");
		//		} catch (IOException e) {
		//			// no-op
		//			return false;
		//		}
		return true;
	}

	@Override
	protected void onWebhookUpdate(Update update) {
		Message message = update.message();
		InlineQuery inlineQuery = update.inlineQuery();
		if(isInlinequery(inlineQuery));
		else roll(message);
	}

	private boolean isInlinequery(InlineQuery inlineQuery) {
		if(inlineQuery == null) return false; 
		String resp = null;
		try{
			resp = roll(inlineQuery.query(),inlineQuery.from().firstName());
		}catch(Exception e) { return false; } // se non riconosco una stringa valida, esco
		if(resp==null || resp.equals("")) return false;
		InlineQueryResultArticle r1 = new InlineQueryResultArticle(inlineQuery.id(), "Roll!", inlineQuery.query()+": "+resp);
		bot.execute(new AnswerInlineQuery(inlineQuery.id(), r1));
		return true;
	}

	private void roll(Message message) {

		if(message == null) return;
		String input = message.text();
		try{
			String resp = roll(input, message.from().firstName());
			sendMessage(message, resp);
			//log(message, input + " - " + sb.toString());
		} catch (NumberFormatException e) {
			error(message, "String malformed. Example: 1d10, 4d6 ");
		} // se il cast da eccezione, esco
	}

	private String roll (String input, String name){
		StringBuilder sb = new StringBuilder();// stringa output
		if(input == null || input.equals("")) return "";				// se messaggio null, esci
		input = input.toLowerCase().trim();
		int dPosition = input.indexOf('d');
		if (dPosition == -1) {
			return ""; // se non trovo la d, esco
		}
		String[] numbers = input.split("d");

		if (numbers[0].charAt(0) == '/') // elimino eventuali caratteri di
			// comando
			numbers[0] = numbers[0].substring(1, numbers[0].length());
		int number = Integer.parseInt(numbers[0]);
		int faces = Integer.parseInt(numbers[1]);
		int difficulty = -1;
		if (numbers.length >= 3)
			difficulty = Integer.parseInt(numbers[2]);
		//			String name = 
		//					(message.chat().username() == null 
		//					|| message.chat().username().equals("null"))
		//					? message.chat().firstName() : message.chat().username();
		//sb.append(name + ": ");
		int sum = 0;
		int success = 0;
		boolean criticalCheck = true;
		for (int i = 0; i < number; i++) {
			int roll = rollDice(faces);
			sum += roll;
			sb.append("" + roll + ' ');
			if (difficulty <= roll) {
				success++;
				criticalCheck = false;
			}
			success += (roll == 1) ? -1 : 0;
		}
		sb.append("(sum:" + sum + ')');
		if (difficulty > -1)							// se presente difficolta', scrivo numero di successi
			sb.append("(success=" + success + ')');
		if (success < 0 && criticalCheck)				// check su fallimento critico
			sb.append(" CRITICAL FAILURE!!!");
		return sb.toString();

	}

	private static int rollDice(int faces) {
		return ((int) (((Math.random() * 47 * 100) % faces) + 1));
	}

	@SuppressWarnings("unused")
	private void error(Message message, Exception e) {
		sendMessage(message, "" + e.getClass() + ':' + e.getMessage());
	}

	private void error(Message message, String s) {
		sendMessage(message, s);
	}

	//	private void log(Message message, String log) throws IOException {
	//		FileWriter fw = new FileWriter(new File(Constants.FILE_STATISTICHE), true);
	//		String timestamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
	//		fw.append(timestamp + " - " + "chat_id:" + message.chat().id() + ": " + log + '\n');
	//		fw.flush();
	//		fw.close();
	//	}
}
