package DAO;

import DatabaseConfig.ConnectionFactory;
import model.Ponto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PontoDAO {

    public void registrarPonto (Ponto ponto) {
        String sql = " INSERT INTO ponto (motorista_id, registro, tipo, data_criacao, mensagem_conformidade) " +
                "VALUES (?, ?, ?, ?, ?)" ;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setLong(1, ponto.getMotoristaId());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(ponto.getRegistro()));
            stmt.setString(3, ponto.getTipo());
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(5, ponto.getMensagemConformidade());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao registrar Ponto: " + e.getMessage());
        }
    }


    public List<Ponto> buscarPontosPorMotoristaId(Long motoristaId) {
        String sql = "SELECT id, motorista_id, registro, tipo, data_criacao, mensagem_conformidade " +
                "FROM ponto " +
                "WHERE motorista_id = ?";

        List<Ponto> pontos = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, motoristaId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    Ponto ponto = new Ponto();

                    ponto.setId(rs.getLong("id"));
                    ponto.setMotoristaId(rs.getLong("motorista_id"));
                    ponto.setRegistro(rs.getTimestamp("registro").toLocalDateTime());
                    ponto.setTipo(rs.getString("tipo"));
                    ponto.setDataCriacao(rs.getTimestamp("data_criacao").toLocalDateTime());
                    ponto.setMensagemConformidade(rs.getString("mensagem_conformidade"));

                    pontos.add(ponto);
                }
            }

            return pontos;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pontos do motorista: " + motoristaId, e);
        }

    }

}
