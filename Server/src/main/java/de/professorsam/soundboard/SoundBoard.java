package de.professorsam.soundboard;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;

import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class SoundBoard {

    private static SoundBoard instance;

    private final Map<UUID, Instant> user = new HashMap<>();
    private final Logger logger = Logger.getLogger("Soundboard");

    private int defaultCooldown = 5;

    public static void main(String[] args) {
        new SoundBoard().start();
    }

    public void start() {
        instance = this;
        Javalin server = Javalin.create(javalinConfig -> {
            JavalinJte jte = new JavalinJte(createTemplateEngine(true));
            javalinConfig.fileRenderer(jte);
        });
        server.get("/", new SoundBoardHandler());
        for(int i = 1; i <= 12; i++) {
            logger.info("register sound " + i);
            server.post("/api/sound/" + i, new SoundCallHandler(i));
        }
        server.start(8000);
    }

    private TemplateEngine createTemplateEngine(boolean isDevSystem) {
        if (isDevSystem) {
            DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Path.of("Server","src", "main", "jte"));
            return TemplateEngine.create(codeResolver, ContentType.Html);
        } else {
            return TemplateEngine.createPrecompiled(ContentType.Html);
        }
    }

    public void playSound(int id) {
        logger.info("Play sound " + id);
    }

    public Map<UUID, Instant> getUser() {
        return user;
    }

    public static SoundBoard getInstance() {
        return instance;
    }

    public int getDefaultCooldown() {
        return defaultCooldown;
    }
}
