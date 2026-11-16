package config;

import exception.RepositoryException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {

    private static final String DB_PROPERTIES_FILE = "db.properties";

    private static final String DRIVER_PROPERTY = "db.driver";
    private static final String URL_PROPERTY = "db.url";
    private static final String USER_PROPERTY = "db.user";
    private static final String PASSWORD_PROPERTY = "db.password";

    private static final String DEFAULT_DRIVER = "org.postgresql.Driver";

    private static final Properties PROPERTIES = loadProperties();

    static {
        loadDriver();
    }

    private DatabaseConfig() {
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream(DB_PROPERTIES_FILE)) {
            if (input == null) {
                throw new RepositoryException("Файл " + DB_PROPERTIES_FILE
                        + " не найден в classpath (src/main/resources)");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RepositoryException("Не удалось прочитать " + DB_PROPERTIES_FILE, e);
        }
        return properties;
    }

    private static void loadDriver() {
        String driver = property(DRIVER_PROPERTY, DEFAULT_DRIVER);
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RepositoryException("Драйвер базы данных не найден в classpath: " + driver, e);
        }
    }

    private static String property(String key, String defaultValue) {
        String value = PROPERTIES.getProperty(key);
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }

    private static String requiredProperty(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new RepositoryException("Не задан параметр " + key + " в " + DB_PROPERTIES_FILE);
        }
        return value.trim();
    }

    public static Connection getConnection() {
        return openConnection();
    }

    public static <T> T inTransaction(TransactionWork<T> work) {
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try {
                T result = work.execute(connection);
                connection.commit();
                return result;
            } catch (RuntimeException e) {
                rollback(connection, e);
                throw e;
            } catch (SQLException e) {
                rollback(connection, e);
                throw new RepositoryException("Не удалось выполнить транзакцию", e);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось выполнить транзакцию", e);
        }
    }

    private static void rollback(Connection connection, Throwable cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            cause.addSuppressed(rollbackException);
        }
    }

    private static Connection openConnection() {
        String url = requiredProperty(URL_PROPERTY);
        String user = requiredProperty(USER_PROPERTY);
        String password = requiredProperty(PASSWORD_PROPERTY);
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось подключиться к базе данных", e);
        }
    }

    @FunctionalInterface
    public interface TransactionWork<T> {
        T execute(Connection connection);
    }
}
