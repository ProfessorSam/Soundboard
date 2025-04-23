package de.professorsam.soundboard;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class SoundBoardHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        Map<UUID, Instant> user = SoundBoard.getInstance().getUser();
        long cooldown = SoundBoard.getInstance().getDefaultCooldown();
        if (ctx.cookieStore().get("id") != null) {
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
            if (lastPlayed == null) {
                user.put(uuid, Instant.now());
                ctx.render("soundboard.jte", Collections.singletonMap("context", new SoundBoardContext((int) cooldown)));
                return;
            }
            lastPlayed = lastPlayed.plus(SoundBoard.getInstance().getDefaultCooldown(), ChronoUnit.SECONDS);
            Instant now = Instant.now();
            if (now.isBefore(lastPlayed)) {
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
    }
}
