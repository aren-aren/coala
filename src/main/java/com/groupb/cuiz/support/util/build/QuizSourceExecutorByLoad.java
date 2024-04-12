package com.groupb.cuiz.support.util.build;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class QuizSourceExecutorByLoad implements QuizSourceExecutor {
    SourceCodeClassLoader classLoader;
    Class importedClass;

    public QuizSourceExecutorByLoad() {
        this.classLoader = new SourceCodeClassLoader();
    }

    @Override
    public void compileJava(String path) throws RuntimeException, IOException, ClassNotFoundException {
        String command = "javac -encoding UTF-8 " + path;
        sendCommandToScript(command, null);
        importedClass = classLoader.findClass(path.replaceFirst("\\.java", ".class"));
    }

    @Override
    public String runCode(String path, String filename, String input) throws RuntimeException, IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        if(importedClass == null){
            throw new RuntimeException("클래스파일을 읽어오지 못했습니다.");
        }

        Method solution = importedClass.getMethod("solution", int.class);
        Object result = solution.invoke(importedClass.newInstance(), Integer.parseInt(input));

        return result.toString();
    }
}
