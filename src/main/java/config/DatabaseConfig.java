package config;

import exception.RepositoryException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConfig {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static final String ENV_FILE_NAME = ".env";
    private static final String ENV_FILE_PROPERTY = "license.registry.env";
    private static final String ENV_FILE_ENV_VARIABLE = "LICENSE_REGISTRY_ENV";
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
                LOGGER.info("Файл .env не найден. Укажите путь через -D" + ENV_FILE_PROPERTY
                        + "=/path/.env или переменную окружения " + ENV_FILE_ENV_VARIABLE
                        + ", либо положите .env рядом с WAR или в CATALINA_BASE/conf. "
                        + "Будут использованы переменные окружения системы");
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
            LOGGER.log(Level.WARNING, "Не удалось прочитать .env: " + e.getMessage());
        }
    }

    private static InputStream openEnvStream() throws IOException {
        Path envFile = findEnvFile();
        if (envFile != null) {
            LOGGER.info("Загружен .env: " + envFile.toAbsolutePath());
            return Files.newInputStream(envFile);
        }

        InputStream classpathStream = DatabaseConfig.class.getClassLoader().getResourceAsStream(ENV_FILE_NAME);
        if (classpathStream != null) {
            LOGGER.info("Загружен .env из classpath");
            return classpathStream;
        }

        File workingDirFile = new File(ENV_FILE_NAME);
        if (workingDirFile.exists()) {
            LOGGER.info("Загружен .env из рабочей директории: " + workingDirFile.getAbsolutePath());
            return new FileInputStream(workingDirFile);
        }

        return null;
    }

    /**
     * Порядок поиска:
     * 1. Явный путь: -Dlicense.registry.env=/path/.env или LICENSE_REGISTRY_ENV.
     * 2. CATALINA_BASE/conf/.env.
     * 3. Рядом с WAR: CATALINA_BASE/webapps/.env, корень распакованного webapp или его родитель.
     * 4. Корень проекта (для локального запуска из IDE/Gradle).
     */
    private static Path findEnvFile() {
        Path explicitlyConfigured = explicitlyConfiguredEnvFile();
        if (explicitlyConfigured != null) {
            return explicitlyConfigured;
        }

        Path catalinaConfEnv = catalinaConfEnvFile();
        if (catalinaConfEnv != null) {
            return catalinaConfEnv;
        }

        Path nextToWarEnv = nextToWarEnvFile();
        if (nextToWarEnv != null) {
            return nextToWarEnv;
        }

        Path projectRoot = findProjectRoot(Paths.get("").toAbsolutePath());
        if (projectRoot == null) {
            projectRoot = findProjectRoot(codeSourceDirectory());
        }
        if (projectRoot != null) {
            Path envFile = projectRoot.resolve(ENV_FILE_NAME);
            if (Files.isRegularFile(envFile)) {
                return envFile;
            }
        }

        return null;
    }

    private static Path explicitlyConfiguredEnvFile() {
        String configuredPath = System.getProperty(ENV_FILE_PROPERTY);
        if (configuredPath == null || configuredPath.isBlank()) {
            configuredPath = System.getenv(ENV_FILE_ENV_VARIABLE);
        }
        if (configuredPath == null || configuredPath.isBlank()) {
            return null;
        }

        Path path;
        try {
            path = Paths.get(configuredPath.trim());
        } catch (InvalidPathException e) {
            LOGGER.log(Level.WARNING, "Некорректный путь к .env: " + configuredPath + " (" + e.getMessage() + ")");
            return null;
        }
        if (Files.isRegularFile(path)) {
            return path;
        }

        LOGGER.log(Level.WARNING, "Указанный файл .env не найден: " + path.toAbsolutePath());
        return null;
    }

    private static Path catalinaConfEnvFile() {
        String catalinaBase = catalinaBase();
        if (catalinaBase == null) {
            return null;
        }

        Path envFile = Paths.get(catalinaBase, "conf", ENV_FILE_NAME);
        return Files.isRegularFile(envFile) ? envFile : null;
    }

    private static Path nextToWarEnvFile() {
        String catalinaBase = catalinaBase();
        if (catalinaBase != null) {
            Path envFile = Paths.get(catalinaBase, "webapps", ENV_FILE_NAME);
            if (Files.isRegularFile(envFile)) {
                return envFile;
            }
        }

        Path webappRoot = findWebappRoot(codeSourceDirectory());
        if (webappRoot == null) {
            return null;
        }

        Path envInWebapp = webappRoot.resolve(ENV_FILE_NAME);
        if (Files.isRegularFile(envInWebapp)) {
            return envInWebapp;
        }

        Path parent = webappRoot.getParent();
        if (parent != null) {
            Path envNextToWebapp = parent.resolve(ENV_FILE_NAME);
            if (Files.isRegularFile(envNextToWebapp)) {
                return envNextToWebapp;
            }
        }

        return null;
    }

    private static Path findWebappRoot(Path codeSource) {
        Path current = codeSource;
        while (current != null) {
            if (current.getFileName() != null && "WEB-INF".equals(current.getFileName().toString())) {
                return current.getParent();
            }
            current = current.getParent();
        }
        return null;
    }

    private static String catalinaBase() {
        String value = System.getProperty("catalina.base");
        if (value == null || value.isBlank()) {
            value = System.getenv("CATALINA_BASE");
        }
        return (value == null || value.isBlank()) ? null : value;
    }

    private static Path findProjectRoot(Path start) {
        Path current = start;
        while (current != null) {
            if (isProjectRoot(current)) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    private static boolean isProjectRoot(Path path) {
        return Files.isRegularFile(path.resolve("settings.gradle.kts"))
                || Files.isRegularFile(path.resolve("build.gradle.kts"))
                || Files.isRegularFile(path.resolve("settings.gradle"))
                || Files.isRegularFile(path.resolve("build.gradle"))
                || Files.isDirectory(path.resolve(".git"));
    }

    private static Path codeSourceDirectory() {
        try {
            return Paths.get(DatabaseConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException | RuntimeException e) {
            return null;
        }
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
        String url = getValue("DB_URL");
        String user = getValue("DB_USER");
        String password = getValue("DB_PASSWORD");
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
