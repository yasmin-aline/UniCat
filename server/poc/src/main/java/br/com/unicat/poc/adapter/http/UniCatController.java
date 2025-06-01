package br.com.unicat.poc.adapter.http;

import br.com.unicat.poc.usecases.GenerateUnitTests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class UniCatController {
    private final GenerateUnitTests generateUnitTests;

    public UniCatController(GenerateUnitTests generateUnitTests) {
        this.generateUnitTests = generateUnitTests;
    }

    @GetMapping(path = "/unicat")
    String generation() {
        this.generateUnitTests.run("CalculatorTest" , "public class Calculator {\n" +
                "\n" +
                "    /**\n" +
                "     * Adiciona dois números inteiros.\n" +
                "     *\n" +
                "     * @param a O primeiro número.\n" +
                "     * @param b O segundo número.\n" +
                "     * @return A soma de a e b.\n" +
                "     */\n" +
                "    public int add(int a, int b) {\n" +
                "        return a + b;\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * Subtrai o segundo número do primeiro.\n" +
                "     *\n" +
                "     * @param a O número do qual subtrair.\n" +
                "     * @param b O número a ser subtraído.\n" +
                "     * @return A diferença entre a e b.\n" +
                "     */\n" +
                "    public int subtract(int a, int b) {\n" +
                "        return a - b;\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * Multiplica dois números inteiros.\n" +
                "     *\n" +
                "     * @param a O primeiro número.\n" +
                "     * @param b O segundo número.\n" +
                "     * @return O produto de a e b.\n" +
                "     */\n" +
                "    public int multiply(int a, int b) {\n" +
                "        return a * b;\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * Divide o primeiro número pelo segundo.\n" +
                "     *\n" +
                "     * @param a O dividendo.\n" +
                "     * @param b O divisor.\n" +
                "     * @return O resultado da divisão inteira de a por b.\n" +
                "     * @throws IllegalArgumentException se o divisor (b) for zero.\n" +
                "     */\n" +
                "    public int divide(int a, int b) {\n" +
                "        if (b == 0) {\n" +
                "            throw new IllegalArgumentException(\"Divisor cannot be zero\");\n" +
                "        }\n" +
                "        return a / b;\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * Verifica se um número é par.\n" +
                "     *\n" +
                "     * @param number O número a ser verificado.\n" +
                "     * @return true se o número for par, false caso contrário.\n" +
                "     */\n" +
                "    public boolean isEven(int number) {\n" +
                "        return number % 2 == 0;\n" +
                "    }\n" +
                "}\n", "com.example.math" );

        return null;
    }
}