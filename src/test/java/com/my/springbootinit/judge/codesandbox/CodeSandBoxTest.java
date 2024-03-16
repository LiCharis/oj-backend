package com.my.springbootinit.judge.codesandbox;

import com.my.springbootinit.judge.codesandbox.model.ExecuteCodeRequest;
import com.my.springbootinit.judge.codesandbox.model.ExecuteCodeResponse;
import com.my.springbootinit.model.enums.QuestionSubmitLanguageEnum;
import com.my.springbootinit.model.enums.QuestionSubmitStateEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * @author 黎海旭
 **/
@SpringBootTest
public class CodeSandBoxTest {

    @Value("${codesandbox.type:example}")
    private String type;

    @Test
    public void executeCodeByProxy() {
        CodeSandBox codeSandBox = CodeSandBoxFactory.newInstance(type);
        CodeSandBoxProxy codeSandBoxProxy = new CodeSandBoxProxy(codeSandBox);

        String code = "public class Main {\n" +
                "    public static void main(String[] args) throws InterruptedException{\n" +
                "       int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        System.out.println(\"结果为 :\" + (a + b));\n" +
                "    }\n" +
                "}\n";
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder().code(code).
                language(language).
                inputList(inputList)
                .build();

        ExecuteCodeResponse executeCodeResponse = codeSandBoxProxy.codeExecute(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }
}
