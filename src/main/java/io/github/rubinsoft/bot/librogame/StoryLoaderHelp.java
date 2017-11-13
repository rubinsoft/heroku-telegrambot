package io.github.rubinsoft.bot.librogame;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.github.rubinsoft.pengrad.openshift.DBConnector;

@WebServlet("/storyloaderhelp")
public class StoryLoaderHelp extends HttpServlet{
	private static final long serialVersionUID = -8794083090749771602L;

	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
		String content = "";
		try {
			content = DBConnector.getParam("librogame", "HelpPageContent");
		} catch (Exception e) {
			out.write("Error: "+e.getMessage());
		}
		out.write(
				"<!DOCTYPE html><html lang=\"en\">"
				+ "<head><title>Scrivere una storia</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head>"
				+ "<body>" + content + "</body></html>");
	}
}
