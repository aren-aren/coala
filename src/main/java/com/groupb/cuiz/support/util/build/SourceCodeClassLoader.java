package com.groupb.cuiz.support.util.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SourceCodeClassLoader extends ClassLoader{
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        File file = new File(name);

        if(!file.exists()){
            throw new ClassNotFoundException();
        }

        if(!file.isFile()){
            throw new RuntimeException();
        }

        byte[] bytecode = null;
        try {
            bytecode = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(bytecode == null){
            throw new ClassNotFoundException();
        }

        return defineClass("Solution", bytecode, 0, bytecode.length);
    }
}
