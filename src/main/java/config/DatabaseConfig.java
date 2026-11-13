package config;

import exception.RepositoryException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseConfig {

    private static final String ENV_FILE_NAME = ".env";
    private static final Map<String, String> ENV_VALUES = new HashMap<>();

    static {
        loadDriver();
        loadEnvFile();
    }

    private static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RepositoryException("Драйвер PostgreSQL не найден в classpath", e);
        }
    }

    private DatabaseConfig() {
    }

    private static void loadEnvFile() {
        try (InputStream input = openEnvStream()) {
            if (input == null) {
                System.out.println("Файл .env не найден ни в classpath, ни в рабочей директории, "
                        + "будут использованы переменные окружения системы");
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int separatorIndex = line.indexOf('=');
                    if (separatorIndex == -1) {
                        continue;
                    }
                    String key = line.substring(0, separatorIndex).trim();
                    String value = line.substring(separatorIndex + 1).trim();
                    ENV_VALUES.put(key, value);
                }
            }
        } catch (IOException e) {
            System.out.println("Не удалось прочитать .env: " + e.getMessage());
        }
    }

    private static InputStream openEnvStream() throws IOException {
        InputStream classpathStream = DatabaseConfig.class.getClassLoader().getResourceAsStream(ENV_FILE_NAME);
        if (classpathStream != null) {
            return classpathStream;
        }
        File workingDirFile = new File(ENV_FILE_NAME);
        if (workingDirFile.exists()) {
            return new FileInputStream(workingDirFile);
        }
        return null;
    }

    private static String getValue(String key) {
        String value = ENV_VALUES.get(key);
        if (value == null) {
            value = System.getenv(key);
        }
        if (value == null) {
            throw new RepositoryException("Не задана переменная окружения " + key);
        }
        return value;
    }

    public static Connection getConnection() {
        String url = getValue("DB_URL");
        String user = getValue("DB_USER");
        String password = getValue("DB_PASSWORD");
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось подключиться к базе данных", e);
        }
    }
}
