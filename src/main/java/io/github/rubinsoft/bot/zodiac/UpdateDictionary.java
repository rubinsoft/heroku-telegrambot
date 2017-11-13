package io.github.rubinsoft.bot.zodiac;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.github.rubinsoft.pengrad.openshift.DBConnector;

@WebServlet("/updateDictionary")
public class UpdateDictionary extends HttpServlet {
	private static final long serialVersionUID = 2345840592318816589L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
    	StringBuilder sb = new StringBuilder();
		try{
			Statement statement = DBConnector.getStatement("zodiac");
			ResultSet rsDictionary = statement.executeQuery("SELECT * FROM dictionary WHERE active = 'W'");
			//active ha 3 stati: Y-attivo,N-non attivo,W-in attesa di attivazione.
			//Update dictionary attiva tutto ci� che � in attesa di attivazione
			if(!rsDictionary.first()) {
				out.write("Nessun record da aggiornare");
				return ; // non ho nulla da aggiornare e quindi esco
			}
			rsDictionary.absolute(1);
			sb.append("Sto aggiungendo al dizionario le seguenti frasi:\n\n");
			do{
				sb.append(rsDictionary.getString("type")+": "+rsDictionary.getString("text"));
				rsDictionary.updateString("active","Y");
				rsDictionary.updateRow();
			}while(rsDictionary.next());
		}catch(Exception e){
			StringBuilder sb1 = new StringBuilder();
			for (int i = 0; i < e.getStackTrace().length; i++) {
				sb1.append("" + e.getStackTrace()[i] + "<br>");
			}
			out.write(("class:"+e.getClass() + "\nMessage: "+ e.getMessage() +"\nStacktrace: " + sb1.toString()));
			return;
		}
		out.write(sb.toString());
	}

}
