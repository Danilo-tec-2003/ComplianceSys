package web;

import DAO.PontoDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Ponto;
import service.JornadaService;
import util.LocalDateTimeAdapter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@WebServlet("/api/jornada")
public class JornadaServlet extends HttpServlet {

    private final JornadaService jornadaService = new JornadaService();
    private final PontoDAO pontoDAO = new PontoDAO();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String motoristaIdParam = request.getParameter("motoristaId");
        String dataParam = request.getParameter("data");

        List<Ponto> pontos = null;

        try {
            if (motoristaIdParam != null && !motoristaIdParam.isEmpty()) {
                Long motoristaId = Long.parseLong(motoristaIdParam);
                pontos = pontoDAO.buscarPontosPorMotoristaId(motoristaId);

            } else if (dataParam != null && !dataParam.isEmpty()) {
                LocalDate data = LocalDate.parse(dataParam);
                pontos = pontoDAO.buscarPontosPorDia(data);

            }

            if (pontos != null) {
                if (!pontos.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write(gson.toJson(pontos));
                    return;
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write(gson.toJson("Nenhum ponto Registrado para essa Data."));
                    return;
                }
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson("Parâmetro ID inválido."));
            return;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson("Erro interno do servidor: " + e.getMessage()));
            return;
        }

        Ponto pontoExemplo = new Ponto(
                null,
                100L,
                LocalDateTime.now(),
                "INICIO_JORNADA",
                null, LocalDateTime.now(),
                "OK: Teste de Serialização"
        );
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(pontoExemplo));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            String jsonInput = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

            Type listType = new TypeToken<List<Ponto>>(){}.getType();
            List<Ponto> pontosRecebidos = gson.fromJson(jsonInput, listType);

            if(pontosRecebidos == null || pontosRecebidos.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson("Erro: Lista de pontos vazia ou mal formatada."));
                return;
            }


            String resultadoValidacao = jornadaService.calcularEValidar(pontosRecebidos);

            for (Ponto ponto : pontosRecebidos) {
                ponto.setMensagemConformidade(resultadoValidacao);
            }

            for (Ponto ponto : pontosRecebidos) {
                pontoDAO.registrarPonto(ponto);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(resultadoValidacao));

        } catch (Exception e) {
            System.err.println("Erro no processamento da jornada: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson("Erro interno: " + e.getMessage()));
        }
    }
}