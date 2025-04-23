package de.professorsam.soundboard;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.http.sse.SseClient;
import io.javalin.rendering.template.JavalinJte;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoundBoard {

    private static SoundBoard instance;

    private final Map<UUID, Instant> user = new HashMap<>();
    private final Logger logger = Logger.getLogger("Soundboard");
    private final List<SseClient> sseClients = new ArrayList<>();
    private final Map<Integer, String> idTagMap = new HashMap<>();

    private int defaultCooldown = 5;

    public static void main(String[] args) {
        new SoundBoard().start();
    }

    public void start() {
        instance = this;
        var map = idTagMap;
        map.put(1, "Nichts gemackt");
        map.put(2, "Meow Meow");
        map.put(3, "Wow");
        map.put(4, "Fail");
        map.put(5, "Furz");
        map.put(6, "Emotional Damage");
        map.put(7, "Ewwww");
        map.put(8, "Tante Marianne");
        map.put(9, "Buzzer");
        map.put(10, "Booo!");
        map.put(11, "Applaus");
        map.put(12, "Schock");
        Javalin server = Javalin.create(javalinConfig -> {
            JavalinJte jte = new JavalinJte(createTemplateEngine(false));
            javalinConfig.fileRenderer(jte);
        });
        server.get("/", new SoundBoardHandler());
        server.get("/client", ctx -> ctx.render("client.jte"));
        server.post("/api/setcountdown", ctx -> {
            logger.info(ctx.body());
            String body = ctx.body();
            Pattern pattern = Pattern.compile("\"countdown\"\\s*:\\s*(\\d+)");
            Matcher matcher = pattern.matcher(body);
            try {
                if(matcher.find()) {
                    int cooldown = Integer.parseInt(matcher.group(1));
                    defaultCooldown = cooldown;
                }
            } catch (Exception e) {

            }
        });
        for (int i = 1; i <= 12; i++) {
            logger.info("register sound " + i);
            server.post("/api/sound/" + i, new SoundCallHandler(i));
            server.get("/api/sound/" + i, new SoundCallHandler(i));
        }
        server.sse("/partyclientsse", client -> {
            client.keepAlive();
            sseClients.add(client);
            logger.info("client " + client.ctx().ip() + " connected");
        });
        server.start(8000);
    }

    private TemplateEngine createTemplateEngine(boolean isDevSystem) {
        if (isDevSystem) {
            DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Path.of("Server", "src", "main", "jte"));
            return TemplateEngine.create(codeResolver, ContentType.Html);
        } else {
            return TemplateEngine.createPrecompiled(ContentType.Html);
        }
    }

    public void playSound(int id, String emitter) {
        logger.info("Play sound " + id);
        List<SseClient> terminatedClients = new ArrayList<>();
        for (SseClient c : sseClients) {
            if (c.terminated()) {
                terminatedClients.add(c);
                continue;
            }
            String payload = "{ \"id\": \"" + id + "\", \"tag\": \"" + idToTag(id) + "\", \"emitter\": \"" + emitter + "\" }";
            c.sendEvent("play", payload);
        }
        for (SseClient c : terminatedClients){
            logger.info("terminated client " + c.ctx().ip());
            c.close();
            sseClients.remove(c);
        }
    }

    public String idToTag(int id) {
        return idTagMap.get(id);
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