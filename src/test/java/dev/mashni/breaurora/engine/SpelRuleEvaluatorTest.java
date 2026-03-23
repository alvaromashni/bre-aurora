package dev.mashni.breaurora.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SpelRuleEvaluatorTest {

    private SpelRuleEvaluator evaluator;

    @BeforeEach
    void setUp(){
        evaluator = new SpelRuleEvaluator();
    }

    // condição simples verdadeira
    @Test
    void deveRetornarTrueQuandoCondicaoForSatisfeita(){
        Map<String, Object> event = Map.of("valor", 1500);

        boolean result = evaluator.evaluate("['valor'] > 1000", event);

        assertThat(result).isTrue();
    }

    // condição simples falsa
    @Test
    void deveRetornarFalseQuandoCondicaoNaoForSatisfeita(){
        Map<String, Object> event = Map.of("valor", 500);

        boolean result = evaluator.evaluate("['valor'] > 1000", event);

        assertThat(result).isFalse();
    }

    // condição composta com AND
    @Test
    void deveAvaliarCondicaoComAnd() {
        Map<String, Object> event = Map.of("valor", 800, "pais", "US");

        boolean result = evaluator.evaluate("['valor'] > 500 and ['pais'] != 'BR'", event);

        assertThat(result).isTrue();
    }

    // condição composta com OR
    @Test
    void deveAvaliarCondicaoComOr() {
        Map<String, Object> event = Map.of("valor", 200, "clienteNovo", true);

        boolean result = evaluator.evaluate("['valor'] > 1000 or ['clienteNovo'] == true", event);

        assertThat(result).isTrue();
    }

    // condição inválida — não deve lançar exceção
    @Test
    void deveRetornarFalseQuandoCondicaoForInvalida() {
        Map<String, Object> event = Map.of("valor", 1500);

        boolean result = evaluator.evaluate("essa não é uma expressão válida $$##", event);

        assertThat(result).isFalse();
    }

    // chave ausente no evento — não deve lançar exceção
    @Test
    void deveRetornarFalseQuandoChaveNaoExisteNoEvento() {
        Map<String, Object> event = Map.of("valor", 1500);

        boolean result = evaluator.evaluate("['categoria'] == 'fraud'", event);

        assertThat(result).isFalse();
    }

    // segurança — SpEL injection deve ser bloqueado
    @Test
    void deveBloquearSpelInjection() {
        Map<String, Object> event = Map.of("valor", 100);

        boolean result = evaluator.evaluate(
                "T(java.lang.Runtime).getRuntime().exec('echo hack')", event);

        assertThat(result).isFalse();
    }

}
