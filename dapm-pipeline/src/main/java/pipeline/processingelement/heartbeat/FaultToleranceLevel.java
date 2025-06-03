package pipeline.processingelement.heartbeat;

public enum FaultToleranceLevel {
    LEVEL_0_IGNORE_FAULTS,                          // Only logs received HBs, no verification or reaction
    LEVEL_1_NOTIFY_ONLY,                            // Verifies, and notifies on fault, PE continues processing
    LEVEL_2_TERMINATE_ENTIRE_PIPELINE,              // Notifies, fully terminates pipeline
    LEVEL_3_KEEP_RUNNING_PARTIAL_PIPELINE,          // -- TO BE DEFINED
    LEVEL_4_RESTART_FAILED_INSTANCE;                 // -- TO BE DEFINED

    public static FaultToleranceLevel fromString(String levelString, FaultToleranceLevel defaultLevel) {
        if (levelString == null || levelString.trim().isEmpty()) {
            return defaultLevel;
        }
        try {
            return FaultToleranceLevel.valueOf(levelString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultLevel;
        }
    }
}