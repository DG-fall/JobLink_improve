package com.joblink.dao;

import com.joblink.model.Portfolio;
import com.joblink.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PortfolioDAO implements IDAO<Portfolio> {

    private Connection connection;

    public PortfolioDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    @Override
    public Portfolio create(Portfolio portfolio) {
        String sql = "INSERT INTO Portfolio (titre, description, id_freelance) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, portfolio.getTitre());
            pstmt.setString(2, portfolio.getDescription());
            pstmt.setInt(3, portfolio.getIdFreelance());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        portfolio.setIdPortfolio(generatedKeys.getInt(1));
                        return portfolio;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Portfolio getById(int id) {
        String sql = "SELECT * FROM Portfolio WHERE id_portfolio = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractPortfolioFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Portfolio> getAll() {
        List<Portfolio> portfolios = new ArrayList<>();
        String sql = "SELECT * FROM Portfolio";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                portfolios.add(extractPortfolioFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return portfolios;
    }

    @Override
    public Portfolio update(Portfolio portfolio) {
        String sql = "UPDATE Portfolio SET titre = ?, description = ? WHERE id_portfolio = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, portfolio.getTitre());
            pstmt.setString(2, portfolio.getDescription());
            pstmt.setInt(3, portfolio.getIdPortfolio());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return portfolio;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Portfolio WHERE id_portfolio = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthodes spécifiques

    public List<Portfolio> getPortfoliosByFreelance(int idFreelance) {
        List<Portfolio> portfolios = new ArrayList<>();
        String sql = "SELECT * FROM Portfolio WHERE id_freelance = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idFreelance);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    portfolios.add(extractPortfolioFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return portfolios;
    }

    private Portfolio extractPortfolioFromResultSet(ResultSet rs) throws SQLException {
        Portfolio portfolio = new Portfolio();
        portfolio.setIdPortfolio(rs.getInt("id_portfolio"));
        portfolio.setTitre(rs.getString("titre"));
        portfolio.setDescription(rs.getString("description"));
        portfolio.setIdFreelance(rs.getInt("id_freelance"));
        return portfolio;
    }
}
