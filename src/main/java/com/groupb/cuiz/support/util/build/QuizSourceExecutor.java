package com.groupb.cuiz.support.util.build;

import org.apache.commons.exec.*;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;

public interface QuizSourceExecutor {
    void compileJava(String path) throws RuntimeException, IOException, ClassNotFoundException;
    String runCode(String path, String filename, String input) throws RuntimeException, IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException;

    /**
     * 만들어진 명령어와 input으로 줄 데이터를 받아 script를 통해 실행한 결과를 리턴한다
     * @param command
     * @param input
     * @return
     * @throws IOException
     */
    default String sendCommandToScript(String command, String input) throws RuntimeException, IOException {
        //스크립트에 command를 보내고 출력값을 받아온다
        System.out.println("스크립트 실행 : command = " + command);
        //출력을 받을 outputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();

        //command를 파싱
        CommandLine commandLine = CommandLine.parse(command);

        //터미널과 연결해줄 객체
        DefaultExecutor executor = DefaultExecutor.builder().get();

        //input이 있으면 입력함
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if(input != null) {
            OutputStreamWriter ow = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(ow);

            bw.write(input);
            bw.flush();
            bw.close();
            ow.close();
        }
        ByteArrayInputStream ins = new ByteArrayInputStream(os.toByteArray());
        os.close();

        //연결후 데이터를 주고 받는 handler
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorOutputStream, ins);

        //시간 제한
        ExecuteWatchdog watchdog = ExecuteWatchdog.builder().setTimeout(Duration.ofSeconds(10)).get();

        executor.setExitValues(new int[]{0});
        executor.setStreamHandler(streamHandler);
        executor.setWatchdog(watchdog);

        //command 보내기
        try{
            executor.execute(commandLine);
        } catch (ExecuteException e){
            String msg;
            if(watchdog.killedProcess()){
                msg = "timeout";
            } else {
                msg = errorOutputStream.toString("UTF-8");
            }
            throw new RuntimeException(msg);
        }

        System.out.println("result = " + outputStream.toString("UTF-8"));
        return outputStream.toString("UTF-8");
    }
}
