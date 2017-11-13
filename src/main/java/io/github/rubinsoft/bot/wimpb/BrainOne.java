package io.github.rubinsoft.bot.wimpb;

public class BrainOne {

	/**
	 * false equivale a "non confessa"<br>
	 * true equivale a "confessa"<br><br>
	 * 
	 * <b>parametri</b>:<br>
	 * <i>gameid</i> - chiave partita (<i>es. gameid=123451</i>)<br>
	 * <i>initialize</i> - mossa di apertura (<i>es. initialize</i>)<br>
	 * <i>move</i> - mossa al turno X (<i>es. move=1</i>)<br>
	 * @param args lista dei parametri
	 */
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			//parsing
			String[] param = args[i].split("=");		
			switch(param[0]){
			case "initialize":
				System.out.println(0);
				break;
			case "move":
				System.out.println(1);
				break;
			}
		}

	}
}
