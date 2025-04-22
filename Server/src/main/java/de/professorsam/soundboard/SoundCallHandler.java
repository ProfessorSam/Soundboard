package de.professorsam.soundboard;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class SoundCallHandler implements Handler {

    private final int id;

    public SoundCallHandler(int id) {
        this.id = id;
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {

        context.status(200);
    }
}
