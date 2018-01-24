package ru.mail.polis.saveliyGevel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Created by admin on 24.01.2018.
 */
public interface MyDAO {
    @NotNull
    byte[] get(@NotNull String key) throws NoSuchElementException, IllegalArgumentException, IOException;

    void upsert(@NotNull String key, @NotNull byte[] value) throws IllegalArgumentException, IOException;
    void delete(@NotNull String key) throws IllegalArgumentException, IOException;
}
