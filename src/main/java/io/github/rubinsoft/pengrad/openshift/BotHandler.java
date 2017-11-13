package io.github.rubinsoft.pengrad.openshift;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

/**
 * rev: firebone
 * dtRev: 16/10/17
 */
abstract public class BotHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Update update = BotUtils.parseUpdate(MyBotUtils.extractPostRequestBody(req));//req.body
        Message message = update.message();
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        if (isStartMessage(message) && onStart(message)) {
        	out.write("ok");
        	return;
        } else {
            onWebhookUpdate(update);
        }
        out.write("ok");
    	return;
    }
	
//    @Override
//    public Object handle(Request request, Response response) throws Exception {
//        Update update = BotUtils.parseUpdate(request.body());
//        Message message = update.message();
//
//        if (isStartMessage(message) && onStart(message)) {
//            return "ok";
//        } else {
//            onWebhookUpdate(update);
//        }
//        return "ok";
//    }

    private boolean isStartMessage(Message message) {
        return message != null && message.text() != null && message.text().startsWith("/start");
    }

    protected boolean onStart(Message message) {
        return false;
    }

    protected abstract void onWebhookUpdate(Update update);
}
