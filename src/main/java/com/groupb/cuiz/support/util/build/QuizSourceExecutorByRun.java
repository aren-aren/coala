package com.groupb.cuiz.support.util.build;

import org.apache.commons.exec.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.Duration;

@Component
public class QuizSourceExecutorByRun implements QuizSourceExecutor {

    /**
     * 자바 UTF-8 컴파일 명령어 생성 후
     * 생성된 컴파일 명령어로 컴파일 하고 결과를 리턴한다.
     * @param path
     * @return
     */
    public void compileJava(String path) throws RuntimeException, IOException {
        //자바파일을 컴파일
        String command = "javac -encoding UTF-8 " + path;
        sendCommandToScript(command, null);
    }

    /**
     * 하나의 Input에 대해 script명령어를 만들고 실행한다.
     * 실행 후 output을 리턴한다
     * @param path
     * @param filename
     * @param input
     * @return
     * @throws IOException
     */
    public String runCode(String path, String filename, String input) throws RuntimeException, IOException {
        String command = String.format("java -Dfile.encoding=UTF8 -cp %s %s", path, filename);
        return sendCommandToScript(command, input);
    }
}
