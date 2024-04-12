package com.groupb.cuiz.support.util.build;

import com.groupb.cuiz.MyTest;
import com.groupb.cuiz.support.util.file.FileManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

public class QuizSourceExecutorByLoadTest extends MyTest {
    QuizSourceExecutor quizSourceExecutorByLoad;
    FileManager fileManager;

    public QuizSourceExecutorByLoadTest() {
        this.quizSourceExecutorByLoad = new QuizSourceExecutorByLoad();
        this.fileManager = new FileManager();
    }

    @Test
    public void sourceRunTest() throws Exception {
        String sourceCode ="public class Solution{\n\n" +
                "   public String solution(int i){\n" +
                "   /* 입력되는 Input에 대한 답을 출력해주세요 */\n" +
                " return i*100 + \"\"; " +
                "   }\n" +
                "}";
        String path = "c:/test/sourcecode/";
        String filename = "Solution";
        String extention = ".java";

        fileManager.fileSaveByString(path, filename, sourceCode, extention);

        quizSourceExecutorByLoad.compileJava(path + filename + extention);
        String result = quizSourceExecutorByLoad.runCode(null, null, "3");

        assertNotEquals("301", result);
        assertEquals("300", result);

    }
}
