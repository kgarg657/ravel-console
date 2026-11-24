package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final String CONFIG_PATH = "config/db.properties";

    public static Connection get() throws SQLException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            props.load(fis);
        } catch (IOException e) {
            throw new SQLException("Unable to load database configuration from " + CONFIG_PATH, e);
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        if (url == null || url.isBlank() || user == null || user.isBlank()) {
            throw new SQLException("Missing db.url or db.user in " + CONFIG_PATH);
        }

        return DriverManager.getConnection(url, user, password);
    }
}
