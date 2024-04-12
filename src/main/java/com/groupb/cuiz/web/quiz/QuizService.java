package com.groupb.cuiz.web.quiz;

import com.groupb.cuiz.support.util.build.QuizSourceExecutor;
import com.groupb.cuiz.support.util.file.FileManager;
import com.groupb.cuiz.support.util.pager.Pager;
import com.groupb.cuiz.web.member.MemberDAO;
import com.groupb.cuiz.web.member.MemberDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletContext;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {
    @Autowired
    private FileManager fileManager;
    @Autowired
    private ServletContext servletContext;
    @Autowired
    private QuizDAO quizDAO;
    @Autowired
    private MemberDAO memberDAO;
    @Autowired
    private QuizSourceExecutor quizSourceExecutor;

    /**
     * DB의 QUIZ 테이블에 Quiz 정보를 넣고, 예제 input과 output, 실제 input과 output TESTCASE 테이블에 넣는다
     *
     * @param quizDTO
     * @param example_inputs
     * @param example_output
     * @param quiz_inputs
     * @param quiz_outputs
     * @return
     * @throws Exception
     */
    public int addQuiz(QuizDTO quizDTO, String[] example_inputs, String[] example_output, String[] quiz_inputs, String[] quiz_outputs) throws Exception {
        int result = 0;

        result += quizDAO.addQuiz(quizDTO);

        List<TestcaseDTO> testcaseDTOS = new ArrayList<>();
        addTestCasesToList(quizDTO, example_inputs, example_output, testcaseDTOS, "EXAMPLE"); //EXAMPLE Type의 TestcaseDTO를 List에 넣어줌
        addTestCasesToList(quizDTO, quiz_inputs, quiz_outputs, testcaseDTOS, "QUIZ"); //QUIZ Type의 TestcaseDTO를 List에 넣어줌

        result += quizDAO.addTestcases(testcaseDTOS) * 10;

        return result;
    }

    /**
     * inputs와 outputs에서 TestcaseDTO를 만들어 testcaseDTOs에 add 한다
     *
     * @param quizDTO
     * @param inputs
     * @param outputs
     * @param testcaseDTOS
     * @param testCaseType
     */
    private void addTestCasesToList(QuizDTO quizDTO, String[] inputs, String[] outputs, List<TestcaseDTO> testcaseDTOS, String testCaseType) {
        for (int i = 0; i < inputs.length; i++) {
            TestcaseDTO testcaseDTO = new TestcaseDTO();
            testcaseDTO.setQuiz_No(quizDTO.getQuiz_No());
            testcaseDTO.setTestcase_Input(inputs[i]);
            testcaseDTO.setTestcase_Output(outputs[i]);
            testcaseDTO.setTestcase_Type(testCaseType);
            testcaseDTOS.add(testcaseDTO);
        }
    }

    /**
     * 제출한 answerDTO를 채점하고 <br>
     * 결과를 DB에 저장하고 리턴한다.
     * @param answerDTO
     * @return
     */
    public MemberAnswerDTO submitQuiz(MemberAnswerDTO answerDTO, MemberDTO memberDTO) throws Exception {
        System.out.println("answerDTO = " + answerDTO);

        Map<String, Object> map = new HashMap<>();
        map.put("dto", answerDTO);
        map.put("type", "QUIZ");

        List<TestcaseDTO> testcaseDTOS = quizDAO.getTestCases(map);

        answerDTO = checkAnswer(answerDTO, testcaseDTOS, "QUIZ");
        answerDTO.setAnswer_Check(answerDTO.getTestcase_Results().stream()
                                                .allMatch(TestcaseResult::isResult));

        List<TestcaseDTO> buyedTestcases = quizDAO.getBuyedTestcase(answerDTO);
        int index = 0;
        for (TestcaseDTO buyedTestcase : buyedTestcases) {
            for (; index < testcaseDTOS.size(); index++) {
                /* 구매한 테스트케이스 번호와 결과의 번호를 비교하여 buyed를 변경 */
                if(buyedTestcase.getTestcase_No().equals(answerDTO.getTestcase_Results().get(index).getTestcase_No())){
                    answerDTO.getTestcase_Results().get(index).setBuyed(true);
                    break;
                }
            }
        }

        MemberAnswerDTO oldAnswer = quizDAO.getAnswer(answerDTO);
        System.out.println("oldAnswer = " + oldAnswer);

        if(oldAnswer == null) {
            quizDAO.setAnswer(answerDTO);
            System.out.println("Seted!");
        } else if(!oldAnswer.getAnswer_Check()){
            quizDAO.updateAnswer(answerDTO);
            System.out.println("Updated!");
        }

        Integer quizLevel = quizDAO.getQuizLevel(answerDTO);

        if(answerDTO.getAnswer_Check() && (oldAnswer == null || !oldAnswer.getAnswer_Check())){
            memberDTO.setMember_Coin(memberDTO.getMember_Coin() + QuizEnum.get(quizLevel).getPrice());
            memberDAO.setCoin(memberDTO);
            memberDTO.setMember_Jumsu(memberDTO.getMember_Jumsu() + QuizEnum.get(quizLevel).getJumsu());
            memberDAO.setJumsu(memberDTO);
        }

        return answerDTO;
    }

    /**
     * 실행해보는 기능. quiz_No에 해당하는 테스트케이스들을 가져와서 정답을 맞춰본다.
     *
     * @param answerDTO
     * @return
     * @throws Exception
     */
    public MemberAnswerDTO runExampleQuiz(MemberAnswerDTO answerDTO) throws Exception {
        System.out.println("answerDTO = " + answerDTO);

        Map<String, Object> map = new HashMap<>();
        map.put("dto", answerDTO);
        map.put("type", "EXAMPLE");

        List<TestcaseDTO> testcaseDTOS = quizDAO.getTestCases(map);
        testcaseDTOS.addAll(quizDAO.getBuyedTestcase(answerDTO));

        return checkAnswer(answerDTO, testcaseDTOS, "EXAMPLE");
    }

    /**
     * memberId를 기준으로 path를 만들어 해당 위치에 sourceCode를 java파일로 저장 <br>
     * java파일을 class파일로 컴파일 <br>
     * 컴파일된 파일을 실행 하여 inputs에 대한 outputs을 얻어 리턴한다
     *
     * @param memberId
     * @param sourceCode
     * @param inputs
     * @return
     */
    public List<String> quizSourceBuild(String memberId, String sourceCode, List<String> inputs) throws Exception {
        System.out.println("quizSourceBuild start");

        String realPath = servletContext.getRealPath("/resources/sourcecode/" + memberId);
        String filename = "Main";
        String extension = ".java";

        fileManager.fileSaveByString(realPath, filename, sourceCode, extension);

        //컴파일
        try {
            quizSourceExecutor.compileJava(realPath + "/" + filename + extension);
        } catch (RuntimeException e){
            System.out.println("e.getMessage() = " + e.getMessage().replace(realPath, ""));
            return List.of("컴파일 에러 : " + e.getMessage().replace(realPath, ""));
        }

        List<String> results = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            try {
                results.add(quizSourceExecutor.runCode(realPath, filename, inputs.get(i)));
            } catch (RuntimeException e) {
                System.out.println("e.getMessage() = " + e.getMessage().split("\n")[1]);
                results.add(e.getMessage().split("\n")[1]);
            }
        }

        System.out.println("quizSourceBuild end");
        return results;
    }

    /**
     * input과 코드를 입력받아 컴파일, 실행 후 output을 구해준다
     *
     * @param quizSampleDTO
     * @param inputs
     * @return
     * @throws Exception
     */
    public List<String> getSampleOutput(MemberAnswerDTO quizSampleDTO, List<String> inputs) throws Exception {
        List<String> outputs = quizSourceBuild(quizSampleDTO.getMember_ID(), quizSampleDTO.getSourcecode(), inputs);

        outputs.stream().map(output -> output.replaceAll("\r\n", "\n"));

        return outputs;
    }

    /**
     * 유저의 코드를 저장하고, 컴파일, 실행 후 채점하고 결과를 리턴한다
     *
     * @param answer
     * @param testcaseDTOS
     * @return
     * @throws Exception
     */
    public MemberAnswerDTO checkAnswer(MemberAnswerDTO answer, List<TestcaseDTO> testcaseDTOS, String checkType) throws Exception {
        List<String> inputs = new ArrayList<>();
        List<String> outputs = new ArrayList<>();

        for (TestcaseDTO testcaseDTO : testcaseDTOS) {
            inputs.add(testcaseDTO.getTestcase_Input());
            outputs.add(testcaseDTO.getTestcase_Output());
        }

        //코드 실행후 outputs를 얻어옴
        List<String> results = quizSourceBuild(answer.getMember_ID(), answer.getSourcecode(), inputs);

        //코드 실행 결과와 정답을 비교하여 채점
        List<TestcaseResult> testcaseResults = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            testcaseResults.add(checkTestcase(testcaseDTOS.get(i).getTestcase_No(), outputs.get(i).trim(), results.get(i).replaceAll("\r\n", "\n").trim(), checkType));
        }

        testcaseResults = testcaseResults.stream()
                .sorted(Comparator.comparingInt(TestcaseResult::getTestcase_No))
                .collect(Collectors.toList());

        answer.setTestcase_Results(testcaseResults);

        return answer;
    }

    /**
     * answer와 output을 비교하여 정답과 오답 판별 <br>
     * checkType이 'EXAMPLE' 일 경우 오답일 때 기댓값과 출력값을 같이 알려줌
     * @param answer
     * @param output
     * @param checkType
     * @return
     */
    private TestcaseResult checkTestcase(Integer testcase_No, String answer, String output, String checkType){
        TestcaseResult testcaseResult = new TestcaseResult();
        testcaseResult.setTestcase_No(testcase_No);

        System.out.println("answer = " + answer);
        System.out.println("output = " + output);
        if (output.equals("timeout")) {
            testcaseResult.setResult(false);
            testcaseResult.setResultMessage("시간초과");
            return testcaseResult;
        }

        if (answer.equals(output)) {
            testcaseResult.setResult(true);
            testcaseResult.setResultMessage("정답입니다.");
            return testcaseResult;
        }

        testcaseResult.setResult(false);
        if(checkType.equals("EXAMPLE")){
            testcaseResult.setResultMessage(String.format("기댓값: %s , 출력값: %s , 오답입니다.", answer, output));
        } else if (checkType.equals("QUIZ")) {
            testcaseResult.setResultMessage("오답입니다.");
        }

        return testcaseResult;
    }

    /**
     * 페이지 정보(페이지, 검색어)를 받아와 해당 페이지의 문제들을 출력한다.
     *
     * @param pager
     * @return
     */
    public List<QuizListDTO> getList(Pager pager) {
        Long totalCount = quizDAO.getQuizTotalCount(pager);
        System.out.println("totalCount = " + totalCount);

        pager.makeRow();
        pager.makeNum(totalCount);


        return quizDAO.getList(pager);
    }

    /**
     * quiz_No로 해당 quiz정보를 받아온다
     *
     * @param quizDTO
     * @return
     */
    public QuizDTO getDetail(QuizDTO quizDTO, String type) {
        QuizDTO quizDetail = quizDAO.getQuizDetail(quizDTO);
        quizDetail.setQuiz_Price(QuizEnum.get(quizDetail.getQuiz_Level()).getPrice());

        Map<String, Object> map = new HashMap<>();
        map.put("dto", quizDetail);
        map.put("type", type);

        List<TestcaseDTO> list = quizDAO.getTestCases(map);

        if(type != null && type.equals("EXAMPLE")){
            List<TestcaseDTO> buyedList = quizDAO.getBuyedTestcase(quizDTO);
            list.addAll(buyedList);
        }

        quizDetail.setTestcase(list);

        return quizDetail;
    }

    public MemberAnswerDTO getAnswer(MemberAnswerDTO answerDTO) {
        MemberAnswerDTO sourcecodeDTO = quizDAO.getAnswer(answerDTO);
        return sourcecodeDTO == null ? answerDTO : sourcecodeDTO;
    }

    public Boolean updateQuiz(QuizDTO quizDTO) {
        return quizDAO.updateQuiz(quizDTO) > 0;
    }

    public Boolean deleteTestcase(TestcaseDTO testcaseDTO) {
        return quizDAO.deleteTestcase(testcaseDTO) > 0;
    }

    public List<TestcaseResult> checkRun(MemberAnswerDTO checkDTO) throws Exception {
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setQuiz_No(checkDTO.getQuiz_No());
        quizDTO = quizDAO.getQuizDetail(quizDTO);

        checkDTO.setSourcecode(quizDTO.getQuiz_SampleCode());

        Map<String, Object> map = new HashMap<>();
        map.put("dto", quizDTO);

        List<TestcaseDTO> testcases = quizDAO.getTestCases(map);

        checkDTO = checkAnswer(checkDTO, testcases, "QUIZ");
        return checkDTO.getTestcase_Results();
    }

    public Boolean updateTestcases(List<TestcaseDTO> testcaseDTOS) throws Exception {
        testcaseDTOS.forEach(testcaseDTO -> testcaseDTO.setTestcase_Output(testcaseDTO.getTestcase_Output().replaceAll("\r\n", "\n")));

        return quizDAO.addTestcases(testcaseDTOS) > 0;
    }

    public List<TestcaseDTO> getTestcases(QuizDTO quizDTO) {
        Map<String, Object> map = new HashMap<>();
        map.put("dto", quizDTO);

        return quizDAO.getTestCases(map);
    }

    public List<AnswerShowDTO> getAnswers(QuizDTO quizDTO, Pager pager) {
        Map<String, Object> map = new HashMap<>();
        map.put("dto", quizDTO);
        map.put("pager", pager);
        Long totalCount = quizDAO.getAnswerTotalCount(map);

        pager.makeRow();
        pager.makeNum(totalCount);

        map.put("pager", pager);


        if(totalCount == 0){
            return null;
        }

        return quizDAO.getAnswers(map);
    }

    public List<MemberAnswerDTO> getAnswers(MemberDTO memberDTO) {
        return quizDAO.getMemberAnswers(memberDTO);
    }

    public QuizDTO getQuizInfo(QuizDTO quizDTO) {
        return quizDAO.getQuizDetail(quizDTO);
    }


    public JumsuUpdateDTO getJumsuData(QuizDTO quizDTO) {
        JumsuUpdateDTO jumsuUpdateDTO = quizDAO.getJumsuData(quizDTO);

        Integer level = jumsuUpdateDTO.getUpJumsu();
        jumsuUpdateDTO.setUpJumsu(QuizEnum.get(level).getJumsu());

        jumsuUpdateDTO.setOldJumsu(jumsuUpdateDTO.getOldJumsu() - jumsuUpdateDTO.getUpJumsu());

        return jumsuUpdateDTO;
    }

    public List<QuizDTO> getAllQuizs() {
        return quizDAO.getAllQuizs();
    }

    @Transactional
    public TestcaseDTO buyAndGetTestcase(TestcaseDTO testcaseDTO, MemberDTO member) throws Exception {
        QuizDTO quizDTO = quizDAO.getQuizDetail(testcaseDTO.getQuiz_No());

        Integer quizPrice = QuizEnum.get(quizDTO.getQuiz_Level()).getPrice();

        Map<String, Object> map = new HashMap<>();
        map.put("member", member);
        map.put("price", quizPrice);

        int result = memberDAO.buyTestcase(map);
        if(result < 1){
            throw new RuntimeException("코인이 부족합니다.");
        }

        map.put("testcase", testcaseDTO);

        try{
            quizDAO.buyTestcase(map);
        } catch (Exception e){
            throw new SQLIntegrityConstraintViolationException("힌트 구매 실패");
        }

        return (TestcaseDTO) map.get("testcase");
    }
}
