package manager;

/**
 * Наша обёртка над IOException, чтобы не менять сигнатуры интерфейса.
 */
public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}