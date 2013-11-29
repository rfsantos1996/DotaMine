package com.jabyftw.dotamine;

import java.sql.*;
import java.util.logging.Level;

public class MySQL {

    private final DotaMine reporter;
    private final String user, pass, url;
    public Connection conn = null;

    public MySQL(DotaMine plugin, String username, String password, String url) {
        this.reporter = plugin;
        this.user = username;
        this.pass = password;
        this.url = url;
    }

    public Connection getConn() {
        if (conn != null) {
            return conn;
        }
        try {
            conn = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            reporter.getLogger().log(Level.WARNING, "Couldn''t connect to MySQL: {0}", e.getMessage());
        }
        return conn;
    }

    public void closeConn() {
        if (conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (SQLException ex) {
                reporter.getLogger().log(Level.WARNING, "Couldn''t connect to MySQL: {0}", ex.getMessage());
            }
        }
    }
}
