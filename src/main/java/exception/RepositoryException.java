package exception;

import java.sql.SQLException;

public class RepositoryException extends RuntimeException {

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public boolean hasSqlState(String sqlState) {
        return getCause() instanceof SQLException sqlException
                && sqlState.equals(sqlException.getSQLState());
    }
}
