package http;

/** sprint-9: общие мелкие утилиты для HTTP-обработчиков. */
public final class HttpUtil {
  private HttpUtil() {}

  /** Новый объект, если id == null или 0. */
  public static boolean isNewId(Integer id) {
    return id == null || id == 0;
  }

  /** Парсер id без исключений. Возвращает null при ошибке. */
  public static Integer parseIdOrNull(String s) {
    try {
      return Integer.valueOf(s);
    } catch (Exception ignored) {
      return null;
    }
  }
}
