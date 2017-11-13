package io.github.rubinsoft.bot.librogame;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.github.rubinsoft.bot.librogame.storybuilder.StoryBuilder;
import io.github.rubinsoft.pengrad.openshift.DBConnector;
import io.github.rubinsoft.pengrad.openshift.MyBotUtils;

@WebServlet(name = "StoryUploader", urlPatterns = {"/uploadStory"})
@MultipartConfig
public class StoryUploader extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int MAX_FILE_SIZE= 10000000;
	private final static Logger LOGGER = 
			Logger.getLogger(StoryUploader.class.getCanonicalName());

	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		if (request.getAttribute("org.eclipse.jetty.multipartConfig") == null) {
			MultipartConfigElement multipartConfigElement = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));
			request.setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
		}
		// Create path components to save the file
		final String chatid = request.getParameter("chatid");
		final String storyTitle = request.getParameter("storyTitle");
		String token = request.getParameter("debug");
		final boolean debug = (token!=null)?token.equals("true"):false;
		String content = request.getParameter("content");
		final String alfa = request.getParameter("alfa");

		InputStream filecontent = null;
		final PrintWriter writer = response.getWriter();
		if(debug) writer.println("DEBUG ON:<br>");
		//if(debug) writer.println("file="+ fileName+ " filesize="+filePart.getSize()+"<br>");
		try {
			//controllo sulla massima dimensione
			if(content.length() > MAX_FILE_SIZE)
				throw new IllegalArgumentException("Il file sottomesso supera i 10 MB: "+content.length() );
			//step di validazione della storia
			StoryBuilder story = new StoryBuilder(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
			boolean first = true;
			for (Exception w : story.getWarningsList()){
				if(first){
					writer.println("<br/> Lo step di validazione ha rilevato i seguenti warning:");
					first = false;
				}
				writer.println("<br/>"+w.getMessage());
			}
			//prova di caricamento su DB
			Statement statement = DBConnector.getStatement("librogame");
			ResultSet rsUser = statement.executeQuery("SELECT * FROM user WHERE chatid = '"+ chatid + "'");
			if(!rsUser.first())
				throw new IllegalArgumentException("L'utente non risulta anagrafato");
			statement.close();
			statement = DBConnector.getStatement("librogame");
			boolean firstBrain = false;
			ResultSet rsBrain = statement.executeQuery("SELECT * FROM story WHERE storyTitle = '"+ storyTitle + "'");
			if(!rsBrain.first()){
				firstBrain = true;
				rsBrain.moveToInsertRow();
				rsBrain.updateString("storyTitle", storyTitle);
				rsBrain.updateInt("version", 1);
				rsBrain.updateString("author", chatid);
				rsBrain.updateTimestamp("crTimestamp", new Timestamp(System.currentTimeMillis()));
			}else{
				rsBrain.absolute(1);
				rsBrain.updateTimestamp("chTimestamp", new Timestamp(System.currentTimeMillis()));
				rsBrain.updateInt("version", (rsBrain.getInt("version")+1));
			}
			rsBrain.updateString("alfaStory", (alfa!=null)?"Y":"N");
			content = MyBotUtils.html2db(content);
			rsBrain.updateString("storyContent", content);
			if(firstBrain){
				rsBrain.insertRow();
				rsBrain.moveToCurrentRow();
			} else
				rsBrain.updateRow();

			writer.println("La storia <b>\"" + storyTitle + "\"</b> e' stata caricata con successo");
			LOGGER.log(Level.INFO, "File{0}being uploaded to {1}", 
					new Object[]{storyTitle, "DB"});
		} catch (Exception fne) {
			writer.println("Sono stati riscontrati i seguenti errori:");
			writer.println("<br/><b>"+fne.getMessage()+"</b>");
			if(!(fne instanceof IllegalArgumentException) || debug ) //solo se non e' un'eccezione voluta, allora stampa lo stack
				for(StackTraceElement e:fne.getStackTrace())
					writer.println("<br/>"+e.toString());

			LOGGER.log(Level.SEVERE, "Problems during file upload. Error: {0}", 
					new Object[]{fne.getMessage()});
		} finally {
			if (filecontent != null) {
				filecontent.close();
			}
			if (writer != null) {
				writer.close();
			}
		}
	}

	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	processRequest(req, resp);
	}
	
}
