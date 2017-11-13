package io.github.rubinsoft.bot.librogame.storybuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * prende lo scanner che contiene il file di impostazione e restituisce la
 * prossima porzione di tag necessaria per costruire un XMLNode
 */
public class Parser {
	private String original, toBeParsed;
	private String cor;

	public Parser(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		byte[] buffer = new byte[1024];
		int len = is.read(buffer);
		while (len > 0) {
			buffer = Arrays.copyOf(buffer, len);
			sb.append(new String(buffer));
			buffer = new byte[1024];
			len = is.read(buffer);
		}
		original = sb.toString();
		toBeParsed = sb.toString();
		/*
		 * int i=0; while(hasNext()){ System.out.println(""+(++i)+next()); }
		 */
	}

	public String look() {
		if (cor == null)
			cor = next();
		return cor;
	}

	public String next() {
		if (cor != null) {
			String next = cor;
			cor = null;
			return next;
		}
		String token = null;
		if (toBeParsed.charAt(0) == '#')
			toBeParsed = toBeParsed.substring(toBeParsed.indexOf('\n') + 1);
		if (toBeParsed.charAt(0) == '<') {
			token = toBeParsed.substring(0, toBeParsed.indexOf('>') + 1).trim();
			toBeParsed = toBeParsed.substring(toBeParsed.indexOf('>') + 1)
					.trim();
		} else {
			token = toBeParsed.substring(0, toBeParsed.indexOf('<')).trim();
			toBeParsed = toBeParsed.substring(toBeParsed.indexOf('<')).trim();
		}
		toBeParsed = toBeParsed.trim();
		return token;
	}

	public boolean hasNext() {
		return !toBeParsed.equals("");
	}

	public void reset() {
		toBeParsed = "" + original;
	}
}
