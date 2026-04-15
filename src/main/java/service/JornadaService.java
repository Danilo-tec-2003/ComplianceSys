package service;

import model.Ponto;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JornadaService {

    private static final Duration JORNADA_MAXIMA = Duration.ofHours(8);
    private static final Duration LIMITE_DIRECAO_CONTINUA = Duration.ofHours(5).plusMinutes(30);
    private static final Duration PAUSA_MINIMA_OBRIGATORIA = Duration.ofMinutes(30);
    private static final Duration DESCANSO_DIARIO_MINIMO = Duration.ofHours(11);
    private static final Duration JORNADA_COM_EXTRAS = Duration.ofHours(10);
    private static final Duration INTERVALO_ALMOÇO_MINIMO = Duration.ofHours(1);

    public String calcularEValidar(List<Ponto> pontos) {

        // 1) Ordena os pontos do dia
        pontos.sort(Comparator.comparing(Ponto::getRegistro));

        Duration tempoTotalTrabalhado = Duration.ZERO;
        LocalDateTime inicioBloco = null;
        List<String> alertasCriticos = new ArrayList<>();
        LocalDateTime inicioDirecaoContinua = null;
        LocalDateTime ultimoFimDirecao = null;
        LocalDateTime ultimoFimJornada = null;
        Duration maiorIntervaloJornada = Duration.ZERO;
        LocalDateTime inicioIntervalo = null;

        for (Ponto p : pontos) {

            if (p.getTipo().startsWith("INICIO")) {

                inicioBloco = p.getRegistro();

                if (p.getTipo().equals("INICIO_DIRECAO") || p.getTipo().equals("INICIO_JORNADA")) {
                    inicioDirecaoContinua = p.getRegistro();
                }

                if ((p.getTipo().equals("INICIO_DIRECAO") || p.getTipo().equals("INICIO_JORNADA")) && ultimoFimDirecao != null) {
                    Duration duracaoPausa = Duration.between(ultimoFimDirecao, p.getRegistro());
                    if (duracaoPausa.compareTo(PAUSA_MINIMA_OBRIGATORIA) < 0) {
                        long faltou = PAUSA_MINIMA_OBRIGATORIA.minus(duracaoPausa).toMinutes();
                        alertasCriticos.add("Pausa Obrigatória insuficiente. Faltaram " + faltou + "min antes de retomar a direção.");
                    }
                    ultimoFimDirecao = null;
                }

                if (p.getTipo().equals("INICIO_JORNADA") && ultimoFimJornada != null) {
                    Duration descanso = Duration.between(ultimoFimJornada, p.getRegistro());
                    if (descanso.compareTo(DESCANSO_DIARIO_MINIMO) < 0) {
                        long faltou = DESCANSO_DIARIO_MINIMO.minus(descanso).toMinutes();
                        alertasCriticos.add("Descanso Diário (Interjornada) insuficiente. Faltaram " + faltou + "min de descanso.");
                    }
                }
            }

            else if (p.getTipo().startsWith("FIM") && inicioBloco != null) {

                if (p.getTipo().equals("FIM_JORNADA")) {
                    ultimoFimJornada = p.getRegistro();
                }

                Duration duracaoBloco = Duration.between(inicioBloco, p.getRegistro());

                if (p.getTipo().equals("FIM_JORNADA") || p.getTipo().equals("FIM_DIRECAO")) {
                    tempoTotalTrabalhado = tempoTotalTrabalhado.plus(duracaoBloco);
                }

                if (inicioDirecaoContinua != null) {
                    Duration tempoDirigido = Duration.between(inicioDirecaoContinua, p.getRegistro());
                    if (tempoDirigido.compareTo(LIMITE_DIRECAO_CONTINUA) > 0) {
                        long excedido = tempoDirigido.minus(LIMITE_DIRECAO_CONTINUA).toMinutes();
                        alertasCriticos.add("Limite de Direção Contínua excedido em " + excedido + "min.");
                    }
                    inicioDirecaoContinua = null;
                }

                if (p.getTipo().equals("FIM_DIRECAO") || p.getTipo().equals("FIM_JORNADA")) {
                    ultimoFimDirecao = p.getRegistro();
                }

                inicioBloco = null;
            }


            if (p.getTipo().startsWith("INICIO")) {
                if (inicioIntervalo != null) {
                    Duration thisInterval = Duration.between(inicioIntervalo, p.getRegistro());
                    if (thisInterval.compareTo(maiorIntervaloJornada) > 0) {
                        maiorIntervaloJornada = thisInterval;
                    }
                    inicioIntervalo = null;
                }
            } else if (p.getTipo().startsWith("FIM")) {
                inicioIntervalo = p.getRegistro();
            }
        }

        if (inicioIntervalo != null) {
            LocalDateTime ultimoRegistro = pontos.get(pontos.size() - 1).getRegistro();
            Duration thisInterval = Duration.between(inicioIntervalo, ultimoRegistro);
            if (thisInterval.compareTo(maiorIntervaloJornada) > 0) {
                maiorIntervaloJornada = thisInterval;
            }
        }


        if (maiorIntervaloJornada.compareTo(INTERVALO_ALMOÇO_MINIMO) < 0) {
            long faltou = INTERVALO_ALMOÇO_MINIMO.minus(maiorIntervaloJornada).toMinutes();
            alertasCriticos.add("Intervalo Intrajornada insuficiente. Mínimo de 1h não cumprido. Faltaram " + faltou + "min.");
        }

        if (!alertasCriticos.isEmpty()) {
            return "ALERTA CRÍTICO: " + String.join(" | ", alertasCriticos);
        }

        if (tempoTotalTrabalhado.compareTo(JORNADA_COM_EXTRAS) > 0) {
            Duration excedido = tempoTotalTrabalhado.minus(JORNADA_COM_EXTRAS);
            long h = excedido.toHours();
            long m = excedido.toMinutes() % 60;
            return "ALERTA CRÍTICO: Limite legal de Horas Extras excedido! Ultrapassou 10h em " + h + "h e " + m + "min.";
        } else if (tempoTotalTrabalhado.compareTo(JORNADA_MAXIMA) > 0) {
            Duration extra = tempoTotalTrabalhado.minus(JORNADA_MAXIMA);
            long h = extra.toHours();
            long m = extra.toMinutes() % 60;
            return "ALERTA: Jornada de 8h excedida (dentro do limite de 10h). Extra: " + h + "h " + m + "min.";
        }

        long h = tempoTotalTrabalhado.toHours();
        long m = tempoTotalTrabalhado.toMinutes() % 60;
        return "OK: Jornada em conformidade. Total: " + h + "h " + m + "min.";
    }
}
