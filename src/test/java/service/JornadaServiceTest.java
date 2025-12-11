package service;

import model.Ponto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JornadaServiceTest {

    private JornadaService service;

    private Ponto p(LocalDateTime t, String tipo) {
        return new Ponto(t, tipo);
    }

    @BeforeEach
    void setup() {
        service = new JornadaService();
    }


    @Nested
    @DisplayName("1. Testes de Conformidade (OK)")
    class Conformidade {

        @Test
        @DisplayName("Jornada exatamente 8h, pausa de 1h — deve retornar OK")
        void jornada8h_ok() {

            List<Ponto> pontos = Arrays.asList(
                    p(LocalDateTime.of(2025, 12, 10, 8, 0), "INICIO_JORNADA"),
                    p(LocalDateTime.of(2025, 12, 10, 12, 0), "FIM_DIRECAO"),
                    p(LocalDateTime.of(2025, 12, 10, 13, 0), "INICIO_DIRECAO"),
                    p(LocalDateTime.of(2025, 12, 10, 17, 0), "FIM_JORNADA")
            );

            String r = service.calcularEValidar(pontos);

            assertTrue(r.startsWith("OK"), "A jornada cumpre todas as regras e deve retornar OK");
        }

        @Test
        @DisplayName("Jornada com extras legais (9h30) — deve retornar ALERTA moderado")
        void jornadaComExtrasValidas() {

            List<Ponto> pontos = Arrays.asList(
                    p(LocalDateTime.of(2025, 12, 10, 8, 0), "INICIO_JORNADA"),
                    p(LocalDateTime.of(2025, 12, 10, 12, 0), "FIM_DIRECAO"),
                    p(LocalDateTime.of(2025, 12, 10, 13, 0), "INICIO_DIRECAO"),
                    p(LocalDateTime.of(2025, 12, 10, 18, 30), "FIM_JORNADA")
            );

            String r = service.calcularEValidar(pontos);

            assertTrue(r.contains("Jornada de 8h excedida"),
                    "Extras dentro do limite devem gerar alerta moderado, não crítico.");
        }

        @Test
        @DisplayName("Direção contínua exatamente 5h30m — permitido")
        void direcaoContinuaLimitePermitido() {

            List<Ponto> pontos = Arrays.asList(
                    p(LocalDateTime.of(2025, 12, 10, 8, 0), "INICIO_DIRECAO"),
                    p(LocalDateTime.of(2025, 12, 10, 13, 30), "FIM_DIRECAO")
            );

            String r = service.calcularEValidar(pontos);

            assertFalse(r.contains("Direção Contínua"),
                    "Exatos 5h30m são permitidos.");
        }

        @Test
        @DisplayName("Pausa de almoço maior que 1h — permitido")
        void almocoMaiorQueUmaHora() {

            List<Ponto> pontos = Arrays.asList(
                    p(LocalDateTime.of(2025, 1, 1, 8, 0), "INICIO_JORNADA"),
                    p(LocalDateTime.of(2025, 1, 1, 12, 0), "FIM_DIRECAO"),
                    p(LocalDateTime.of(2025, 1, 1, 13, 30), "INICIO_DIRECAO"),
                    p(LocalDateTime.of(2025, 1, 1, 17, 0), "FIM_JORNADA")
            );

            String r = service.calcularEValidar(pontos);

            assertTrue(r.startsWith("OK"),
                    "Pausa intrajornada maior que 1h deve ser válida.");
        }

        @Test
        @DisplayName("Descanso diário exatamente 11 horas — permitido")
        void descansoDiario11hExatas() {

            List<Ponto> pontos = Arrays.asList(
                    p(LocalDateTime.of(2025, 1, 1, 18, 0), "FIM_JORNADA"),
                    p(LocalDateTime.of(2025, 1, 2, 5, 0), "INICIO_JORNADA") // 11h depois
            );

            String r = service.calcularEValidar(pontos);

            assertFalse(r.contains("Descanso Diário"),
                    "Descanso de 11h é exatamente o mínimo permitido.");
        }


        @Nested
        @DisplayName("2. Testes de Infrações (ALERTA CRÍTICO)")
        class Infracoes {

            @Test
            @DisplayName("Direção contínua excedida (5h31m)")
            void direcaoContinuaExcedida() {

                List<Ponto> pontos = Arrays.asList(
                        p(LocalDateTime.of(2025, 12, 10, 8, 0), "INICIO_DIRECAO"),
                        p(LocalDateTime.of(2025, 12, 10, 13, 31), "FIM_DIRECAO")
                );

                String r = service.calcularEValidar(pontos);

                assertTrue(r.contains("Direção Contínua"),
                        "5h31m deve gerar alerta crítico.");
            }

            @Test
            @DisplayName("Pausa mínima insuficiente — faltou 1 minuto")
            void pausaMinimaInsuficiente() {

                List<Ponto> pontos = Arrays.asList(
                        p(LocalDateTime.of(2025, 12, 10, 8, 0), "INICIO_JORNADA"),
                        p(LocalDateTime.of(2025, 12, 10, 9, 0), "FIM_DIRECAO"),
                        p(LocalDateTime.of(2025, 12, 10, 9, 29), "INICIO_DIRECAO")
                );

                String r = service.calcularEValidar(pontos);

                assertTrue(r.contains("Pausa Obrigatória insuficiente"),
                        "29 minutos devem gerar alerta crítico.");
            }

            @Test
            @DisplayName("Intervalo intrajornada insuficiente (<1h)")
            void almocoInsuficiente() {

                List<Ponto> pontos = Arrays.asList(
                        p(LocalDateTime.of(2025, 12, 10, 8, 0), "INICIO_JORNADA"),
                        p(LocalDateTime.of(2025, 12, 10, 12, 0), "FIM_DIRECAO"),
                        p(LocalDateTime.of(2025, 12, 10, 12, 30), "INICIO_DIRECAO"),
                        p(LocalDateTime.of(2025, 12, 10, 15, 30), "FIM_JORNADA")
                );

                String r = service.calcularEValidar(pontos);

                assertTrue(r.contains("Intervalo Intrajornada insuficiente"),
                        "Intervalo menor que 1h deve gerar alerta crítico.");
            }

            @Test
            @DisplayName("Horas extras ilegais (>10h)")
            void jornadaAcimaDe10h() {

                List<Ponto> pontos = Arrays.asList(
                        p(LocalDateTime.of(2025, 12, 10, 8, 0), "INICIO_JORNADA"),
                        p(LocalDateTime.of(2025, 12, 10, 12, 0), "FIM_DIRECAO"),
                        p(LocalDateTime.of(2025, 12, 10, 13, 0), "INICIO_DIRECAO"),
                        p(LocalDateTime.of(2025, 12, 10, 17, 0), "FIM_DIRECAO"),
                        p(LocalDateTime.of(2025, 12, 10, 17, 30), "INICIO_DIRECAO"),
                        p(LocalDateTime.of(2025, 12, 10, 19, 31), "FIM_JORNADA") // total 10h01m
                );

                String r = service.calcularEValidar(pontos);

                assertTrue(r.contains("Horas Extras excedido"),
                        "10h01m deve gerar alerta crítico.");
            }

            @Test
            @DisplayName("Descanso diário insuficiente (<11h)")
            void deveRetornarErroQuandoDescansoDiarioForInferiorA11Horas() {

                JornadaService service = new JornadaService();

                // JORNADA DO PRIMEIRO DIA
                LocalDateTime dia1_inicio = LocalDateTime.of(2025, 1, 1, 8, 0);
                LocalDateTime dia1_fim = LocalDateTime.of(2025, 1, 1, 17, 0);

                // JORNADA DO SEGUNDO DIA (menos de 11h de descanso) ---
                LocalDateTime dia2_inicio = LocalDateTime.of(2025, 1, 2, 2, 0);

                List<Ponto> pontos = Arrays.asList(
                        new Ponto(dia1_inicio, "INICIO_JORNADA"),
                        new Ponto(dia1_fim, "FIM_JORNADA"),
                        new Ponto(dia2_inicio, "INICIO_JORNADA")
                );

                String resultado = service.calcularEValidar(pontos);

                assertTrue(
                        resultado.contains("Descanso Diário (Interjornada) insuficiente"),
                        "O teste deveria detectar descanso menor que 11 horas"
                );
            }

            @Test
            @DisplayName("Intervalo intrajornada 59 min — deve falhar")
            void almoco59min() {

                List<Ponto> pontos = Arrays.asList(
                        p(LocalDateTime.of(2025, 1, 1, 8, 0), "INICIO_JORNADA"),
                        p(LocalDateTime.of(2025, 1, 1, 12, 0), "FIM_DIRECAO"),
                        p(LocalDateTime.of(2025, 1, 1, 12, 59), "INICIO_DIRECAO"),
                        p(LocalDateTime.of(2025, 1, 1, 16, 0), "FIM_JORNADA")
                );

                String r = service.calcularEValidar(pontos);

                assertTrue(r.contains("Intervalo Intrajornada insuficiente"),
                        "Faltando 1 minuto para completar 1h deve falhar.");
            }
        }


    }
}
