package DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class MainDB {
    public static void main(String[] args) {
        try (Connection connection = ConnectionFactory.getConnection()) {

            if (connection != null) {
                System.out.println("Conectado ao banco de dados PostgreSQL.");
            } else {
                System.out.println("Falha ao conectar: conexão retornou null.");
            }

        } catch (SQLException e) {
            System.err.println("Erro de SQL: " + e.getMessage());
        }
    }
}
