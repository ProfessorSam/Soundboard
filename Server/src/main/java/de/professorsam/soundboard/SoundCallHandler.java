package de.professorsam.soundboard;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class SoundCallHandler implements Handler {

    private final int id;

    public SoundCallHandler(int id) {
        this.id = id;
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {
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
        SoundBoard.getInstance().playSound(id);
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
        if(now.isBefore(lastPlayed)){
            return false;
        }
        return true;
    }
}
