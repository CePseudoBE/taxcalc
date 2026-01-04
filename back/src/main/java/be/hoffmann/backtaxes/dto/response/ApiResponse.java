package be.hoffmann.backtaxes.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Format de reponse uniforme pour toute l'API.
 * Le succes/echec est determine par le code HTTP (2xx = succes, 4xx/5xx = erreur).
 *
 * Succes (HTTP 2xx):
 * {
 *   "data": { ... },
 *   "message": "Operation reussie"
 * }
 *
 * Erreur (HTTP 4xx/5xx):
 * {
 *   "message": "Description de l'erreur",
 *   "error": {
 *     "code": "VALIDATION_ERROR",
 *     "details": ["field1: message", "field2: message"]
 *   }
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private T data;
    private String message;
    private ErrorInfo error;

    // ==================== CONSTRUCTORS ====================

    private ApiResponse() {
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Cree une reponse de succes avec des donnees.
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.data = data;
        return response;
    }

    /**
     * Cree une reponse de succes avec des donnees et un message.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.data = data;
        response.message = message;
        return response;
    }

    /**
     * Cree une reponse de succes sans donnees (ex: DELETE).
     */
    public static <T> ApiResponse<T> success(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.message = message;
        return response;
    }

    /**
     * Cree une reponse d'erreur.
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.message = message;
        return response;
    }

    /**
     * Cree une reponse d'erreur avec code et details.
     */
    public static <T> ApiResponse<T> error(String message, String errorCode, List<String> details) {
        ApiResponse<T> response = new ApiResponse<>();
        response.message = message;
        response.error = new ErrorInfo(errorCode, details);
        return response;
    }

    /**
     * Cree une reponse d'erreur avec code.
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.message = message;
        response.error = new ErrorInfo(errorCode, null);
        return response;
    }

    // ==================== ERROR INFO ====================

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorInfo {
        private String code;
        private List<String> details;

        public ErrorInfo() {}

        public ErrorInfo(String code, List<String> details) {
            this.code = code;
            this.details = details;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public List<String> getDetails() { return details; }
        public void setDetails(List<String> details) { this.details = details; }
    }

    // ==================== GETTERS ====================

    public T getData() { return data; }
    public String getMessage() { return message; }
    public ErrorInfo getError() { return error; }
}
