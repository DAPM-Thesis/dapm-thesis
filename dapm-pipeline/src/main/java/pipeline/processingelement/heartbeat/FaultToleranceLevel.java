package pipeline.processingelement.heartbeat;

public enum FaultToleranceLevel {
    LEVEL_0_IGNORE_FAULTS,                          // Only logs received HBs, no verification or reaction
    LEVEL_1_NOTIFY_ONLY,                            // Verifies, and notifies on fault, PE continues
    LEVEL_2_STOP_PIPELINE_FOR_DEBUG,                // Notifies, stops data flow in entire pipeline
    LEVEL_3_TERMINATE_ENTIRE_PIPELINE_FULL_CLEANUP, // Notifies, fully terminates pipeline
    LEVEL_4_KEEP_RUNNING_PARTIAL_PIPELINE,          // -- TO BE DEFINED
    LEVEL_5_RESTART_FAILED_INSTANCE;                 // -- TO BE DEFINED

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