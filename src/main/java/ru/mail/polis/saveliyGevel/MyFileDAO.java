package ru.mail.polis.saveliyGevel;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.NoSuchElementException;

/**
 * Created by admin on 24.01.2018.
 */
public class MyFileDAO implements MyDAO {
    @NotNull
    private final File dir;

    public MyFileDAO(@NotNull final File dir){
        this.dir = dir;
    }

    @NotNull
    private File getFile(@NotNull String key){
        return new File(dir, key);
    }

    @NotNull
    @Override
    public byte[] get(@NotNull final String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        final File file = getFile(key);
        if(!file.exists()){
            throw new NoSuchElementException();
        }
        final byte[] value = new byte[(int) file.length()];
        try(InputStream is = new FileInputStream(file)){
            if (is.read(value) != value.length){
                throw new IOException("Can not read file");
            } else {
                return value;
            }
        }
    }
    @Override
    public void upsert(@NotNull final String key,@NotNull final byte[] value) throws IllegalArgumentException, IOException {
        try(OutputStream os = new FileOutputStream(getFile(key))){
            os.write(value);
            os.close();
        }
    }

    @Override
    public void delete(@NotNull final String key) throws IllegalArgumentException, IOException {
        getFile(key).delete();
    }
}
