package algorithms;

/** Stores the parameters and parameter values of algorithms. */
public class AlgorithmConfiguration {

    private String executionCommand;
    private String commandLineArgument;
    private String executablePath;

    public AlgorithmConfiguration(String executionCommand, String commandLineArgument, String executablePath) {
        this.executablePath = executablePath;
        this.commandLineArgument = commandLineArgument;
        this.executionCommand = executionCommand;
    }

    public String getExecutionCommand() {
        return executionCommand;
    }

    public String getCommandLineArgument() {
        return commandLineArgument;
    }

    public String getExecutablePath() {
        return executablePath;
    }
}
