package org.ebndrnk.apigateway.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ErrorInfo
 * <p>
 * A data transfer object representing the structure of an error response
 * in the REST API. Used to provide clients with consistent and detailed
 * information when an error or exception occurs during request processing.
 * <p>
 * Fields:
 * <ul>
 *     <li>{@code timestamp} — the date and time when the error occurred</li>
 *     <li>{@code status} — the HTTP status code of the error (e.g. 404, 500)</li>
 *     <li>{@code error} — a short description of the error (e.g. "Not Found")</li>
 *     <li>{@code message} — a detailed error message for debugging or context</li>
 *     <li>{@code path} — the request URI where the error occurred</li>
 * </ul>
 * <p>
 * Example JSON response:
 * <pre>
 * {
 *   "timestamp": "2025-07-01T16:30:25.123",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "User with id 10 not found",
 *   "path": "/api/users/10"
 * }
 * </pre>
 */
@Getter
@Setter
@AllArgsConstructor
public class ErrorInfo {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
