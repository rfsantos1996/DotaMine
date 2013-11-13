package com.jabyftw.dota;

import java.sql.*;
import java.util.logging.Level;

public class SQL {

    private final DotaMine pl;
    private final String user, pass, url;
    private Connection conn = null;

    public SQL(DotaMine plugin, String username, String password, String url) {
        this.pl = plugin;
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
            return conn;
        } catch (SQLException e) {
            pl.getLogger().log(Level.WARNING, "Couldn't connect to MySQL: " + e.getMessage());
            return null;
        }
    }

    public void closeConn() {
        if (conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (SQLException ex) {
                pl.getLogger().log(Level.WARNING, "Couldn't connect to MySQL: " + ex.getMessage());
            }
        }
    }
}
