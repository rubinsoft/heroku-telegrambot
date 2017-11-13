package io.github.rubinsoft.bot.wimpb;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import io.github.rubinsoft.pengrad.openshift.DBConnector;

@WebServlet(name = "FileUploadServlet", urlPatterns = {"/upload"})
@MultipartConfig
public class FileUploadServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private static final int MAX_FILE_SIZE= 10000000;
	private final static Logger LOGGER = 
			Logger.getLogger(FileUploadServlet.class.getCanonicalName());

	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		if (request.getAttribute("org.eclipse.jetty.multipartConfig") == null) {
			MultipartConfigElement multipartConfigElement = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));
			request.setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
		}
		// Create path components to save the file
		final String chatid = request.getParameter("chatid");
		String token = request.getParameter("debug");
		final boolean debug = (token!=null)?token.equals("true"):false;
		final Part filePart = request.getPart("file");
		final String fileName = getFileName(filePart);

		InputStream filecontent = null;
		final PrintWriter writer = response.getWriter();
		if(debug) writer.println("DEBUG ON:<br>");
		if(debug) writer.println("file="+ fileName+ " filesize="+filePart.getSize()+"<br>");
		try {
			//controllo su estensione
			if(!fileName.toLowerCase().endsWith(".jar"))
				throw new IllegalArgumentException("Atteso file .jar: "+fileName.toLowerCase());

			//			//controllo sulla massima dimensione
			if(filePart.getSize() > MAX_FILE_SIZE)
				throw new IllegalArgumentException("Il file sottomesso supera i 10 MB: "+filePart.getSize() );
			//prova di caricamento su DB
			Statement statement = DBConnector.getStatement(WorkIsMyPrisonBot.DB_NAME);//getStatement(message, debug);
			ResultSet rsUser = statement.executeQuery("SELECT * FROM user WHERE chatid = '"+ chatid + "'");
			if(!rsUser.first())
				throw new IllegalArgumentException("L'utente non risulta anagrafato");
			statement.close();
			statement = DBConnector.getStatement(WorkIsMyPrisonBot.DB_NAME);
			boolean firstBrain = false;
			ResultSet rsBrain = statement.executeQuery("SELECT * FROM brain WHERE chatid = '"+ chatid + "'");
			if(!rsBrain.first()){
				firstBrain = true;
				rsBrain.moveToInsertRow();
				rsBrain.updateLong("chatid", Long.parseLong(chatid));
				rsBrain.updateLong("count", 1);
				rsBrain.updateString("brainPath", "/brain");
			}else{
				rsBrain.absolute(1);
			}
			rsBrain.updateString("brainName",fileName);
			rsBrain.updateBinaryStream("brainJar", filePart.getInputStream(), filePart.getSize());

			if(firstBrain){
				rsBrain.insertRow();
				rsBrain.moveToCurrentRow();
			} else
				rsBrain.updateRow();

			writer.println("Il Cervello " + fileName + " e' stato creato");
			LOGGER.log(Level.INFO, "File{0}being uploaded to {1}", 
					new Object[]{fileName, "DB"});
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

	private String getFileName(final Part part) {
		final String partHeader = part.getHeader("content-disposition");
		LOGGER.log(Level.INFO, "Part Header = {0}", partHeader);
		for (String content : part.getHeader("content-disposition").split(";")) {
			if (content.trim().startsWith("filename")) {
				return content.substring(
						content.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}

//	@Override
//	public Object handle(Request request, Response response) throws Exception {
//		processRequest(request, response);
//		return LOGGER.toString();
//	}
	
//	private Statement getStatement(PrintWriter writer){
//		try {
//			Class.forName("com.mysql.jdbc.Driver").newInstance();
//			Connection conn = DriverManager.getConnection("jdbc:mysql://127.6.211.2/WIMPB",
//					"adminq1HeHLQ","5dBUip1aD2Wh");
//			 return conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
//		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
//			writer.println("Errore tecnico durante l'accesso al DB: "+e.getMessage());
//		}
//		return null;
//	}
}
