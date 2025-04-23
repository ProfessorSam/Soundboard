package de.professorsam.soundboard;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class SoundCallHandler implements Handler {

    private final int id;
    private final Logger logger = LoggerFactory.getLogger("SoundCallHandler");

    public SoundCallHandler(int id) {
        this.id = id;
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {
        if(context.method().name().equals("GET")) {
            handleGet(context);
        }
        if(context.method().name().equals("POST")) {
            handlePost(context);
        }
        if(context.method().name().equals("PUT")) {

        }
    }

    private void handlePut(Context context){

    }

    private void handleGet(Context ctx) {
        ctx.header("Content-Type", "audio/mpeg");
        ctx.header("Content-Disposition", "inline; filename=\"" + id + "\"");
        var inputStream = getClass().getResourceAsStream("/" + id + ".mp3");
        if (inputStream == null) {
            logger.error("Could not find " + id + ".mp3 file");
            ctx.status(404).result("File not found");
            return;
        }
        ctx.contentType("audio/mpeg");
        ctx.result(inputStream);
    }


    private void handlePost(Context context) {
        if(context.cookieStore().get("id") == null) {
            context.status(504);
            return;
        }
        if(!isAllowed(context.cookieStore().get("id"))){
            context.status(429);
            return;
        }
        resetCooldown(context.cookieStore().get("id"));
        context.status(200);
        SoundBoard.getInstance().playSound(id, context.cookieStore().get("id").toString().substring(0, 7));
    }

    private void resetCooldown(String id) {
        UUID uuid = UUID.fromString(id);
        SoundBoard.getInstance().getUser().put(uuid, Instant.now());
    }

    private boolean isAllowed(String struuid){
        UUID id;
        try {
            id = UUID.fromString(struuid);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if(!SoundBoard.getInstance().getUser().containsKey(id)){
            return false;
        }
        Instant lastPlayed = SoundBoard.getInstance().getUser().get(id).plus(SoundBoard.getInstance().getDefaultCooldown(), ChronoUnit.SECONDS);
        Instant now = Instant.now();
        return !now.isBefore(lastPlayed);
    }
}
