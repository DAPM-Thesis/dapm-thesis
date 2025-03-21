package algorithms;

import datatype.DataType;
import datatype.petrinet.PetriNet;
import datatype.serialization.DataTypeSerializer;
import datatype.serialization.deserialization.DataTypeFactory;
import utils.Pair;

import java.io.*;

public class HeuristicsMiner implements Algorithm<Pair<PetriNet, Boolean>>{

    private Process process;
    private final BufferedWriter jarInput;
    private final BufferedReader jarOutput;
    private AlgorithmConfiguration config;

    public HeuristicsMiner(AlgorithmConfiguration config) {
        try {
            this.config = config;
            ProcessBuilder processBuilder = new ProcessBuilder(
                    config.getExecutionCommand(),
                    config.getCommandLineArgument(),
                    config.getExecutablePath());
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();

            jarInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            jarOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Pair<PetriNet, Boolean> runAlgorithm(DataType item) {
        DataTypeSerializer dataTypeSerializer = new DataTypeSerializer();
        item.acceptVisitor(dataTypeSerializer);
        String JXES = dataTypeSerializer.getSerialization();
        String output = mine(JXES);
        assert output != null;
        PetriNet petrinet = (PetriNet) DataTypeFactory.deserialize(output);

        return new Pair<>(petrinet, true);
    }

    private String mine(String JXES) {
        try {
            jarInput.write(JXES);
            jarInput.newLine();
            jarInput.flush();

            return jarOutput.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
