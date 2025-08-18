package exceptions;

/** sprint-9: сигнализирует об отсутствии сущности в менеджере. */
public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) {
    super(message);
  }
}
