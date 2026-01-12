package com.example.planning_service.exception;

/**
 * Exception thrown when route optimization fails.
 * HTTP 500 Internal Server Error (or 422 Unprocessable Entity for constraint
 * violations)
 */
public class OptimizationFailedException extends PlanningException {

    private final String solutionScore;
    private final String reason;

    public OptimizationFailedException(String message) {
        super(message);
        this.solutionScore = null;
        this.reason = null;
    }

    public OptimizationFailedException(String message, Throwable cause) {
        super(message, cause);
        this.solutionScore = null;
        this.reason = null;
    }

    public OptimizationFailedException(String message, String solutionScore, String reason) {
        super(message);
        this.solutionScore = solutionScore;
        this.reason = reason;
    }

    public static OptimizationFailedException timeout(long timeoutSeconds) {
        return new OptimizationFailedException(
                String.format("Optimization did not complete within %d seconds", timeoutSeconds),
                "TIMEOUT",
                "Solver exceeded time limit");
    }

    public static OptimizationFailedException noFeasibleSolution(String score) {
        return new OptimizationFailedException(
                "No feasible solution found - constraints cannot be satisfied",
                score,
                "Hard constraints violated (capacity, time windows, etc.)");
    }

    public static OptimizationFailedException emptyInput() {
        return new OptimizationFailedException(
                "Cannot optimize empty order list",
                null,
                "No orders provided for optimization");
    }

    public String getSolutionScore() {
        return solutionScore;
    }

    public String getReason() {
        return reason;
    }
}
