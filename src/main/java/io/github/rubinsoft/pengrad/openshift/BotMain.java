package io.github.rubinsoft.pengrad.openshift;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import io.github.rubinsoft.bot.fruitmasterbot.FruitMasterBot;
import io.github.rubinsoft.bot.librogame.LibrogameBot;
import io.github.rubinsoft.bot.librogame.StoryLoader;
import io.github.rubinsoft.bot.librogame.StoryLoaderHelp;
import io.github.rubinsoft.bot.librogame.StoryUploader;
import io.github.rubinsoft.bot.rpgdice.RPGDiceBot;
import io.github.rubinsoft.bot.wimpb.BrainLoader;
import io.github.rubinsoft.bot.wimpb.FileUploadServlet;
import io.github.rubinsoft.bot.wimpb.WorkIsMyPrisonBot;
import io.github.rubinsoft.bot.zodiac.UpdateDictionary;
import io.github.rubinsoft.bot.zodiac.ZodiacBot;

public class BotMain {
    public static void main(String[] args) {
        ipAddress(args[0]);
        port(Integer.parseInt(args[1]));
//        new RefreshBot();
        
        // Bot handler
        post("/myfmb", new FruitMasterBot()); 
        post("/mywimpb", new WorkIsMyPrisonBot());  
        post("/myrpgdice", new RPGDiceBot());
        post("/mylgb", new LibrogameBot());
        post("/myzodiac", new ZodiacBot());

        // GET/POST handler
        
        
        // GET handler
        get("/webconsole", new WebConsole());
        get("/brainloader", new BrainLoader());
        get("/storyloader", new StoryLoader()); 
        get("/storyloaderhelp", new StoryLoaderHelp()); 
        get("/", new Homepage());
        get("/updateDictionary", new UpdateDictionary());

        // POST handler
        post("/upload", new FileUploadServlet());
        post("/uploadStory", new StoryUploader());
        
    }

}
