package de.professorsam.soundboard;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;

import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class SoundBoard {

    private final Map<UUID, Instant> user = new HashMap<>();
    private final Logger logger = Logger.getLogger("Soundboard");

    public static void main(String[] args) {
        new SoundBoard().start();
    }

    public void start() {
        JavalinJte.init(createTemplateEngine(true));
        Javalin server = Javalin.create().start(8000);
        server.get("/", ctx -> {
            long cooldown = 60;
            if(ctx.cookieStore().get("id") != null){
                String id = ctx.cookieStore().get("id");
                UUID uuid;
                try {
                    uuid = UUID.fromString(id);
                } catch (IllegalArgumentException e) {
                    uuid = UUID.randomUUID();
                    ctx.cookieStore().set("id", uuid.toString());
                    user.put(uuid, Instant.now());
                    ctx.render("soundboard.jte", Collections.singletonMap("context", new SoundBoardContext((int) cooldown)));
                    return;
                }
                Instant lastPlayed = user.get(uuid);
                if(lastPlayed == null){
                    user.put(uuid, Instant.now());
                    ctx.render("soundboard.jte", Collections.singletonMap("context", new SoundBoardContext((int) cooldown)));
                    return;
                }
                lastPlayed = lastPlayed.plus(60, ChronoUnit.SECONDS);
                Instant now = Instant.now();
                if(now.isBefore(lastPlayed)){
                    cooldown = lastPlayed.getEpochSecond() - now.getEpochSecond();
                } else {
                    cooldown = 0;
                }
            } else {
                UUID uuid = UUID.randomUUID();
                user.put(uuid, Instant.now());
                ctx.cookieStore().set("id", uuid.toString());
            }
            ctx.render("soundboard.jte", Collections.singletonMap("context", new SoundBoardContext((int) cooldown)));
        });
        for(int i = 1; i <= 12; i++) {
            logger.info("register sound " + i);
            server.post("/api/sound/" + i, new SoundCallHandler(i));
        }
    }

    private TemplateEngine createTemplateEngine(boolean isDevSystem) {
        if (isDevSystem) {
            DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Path.of("Server","src", "main", "jte"));
            return TemplateEngine.create(codeResolver, ContentType.Html);
        } else {
            return TemplateEngine.createPrecompiled(ContentType.Html);
        }
    }
}
