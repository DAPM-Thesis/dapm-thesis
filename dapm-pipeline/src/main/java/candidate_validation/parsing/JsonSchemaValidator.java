package candidate_validation.parsing;

import com.networknt.schema.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Set;
// TODO: look into schemaProvider so the default can be the resources - but its also possible to inject a provider,
// TODO: e.g. one for testing (so the resources folder does not become bloated with test file config schemas).
// TODO: Draw inspiration from src/main/java/candidat_validation/TEMP_DELETE.java
// based on the NetworkNT readme: https://github.com/networknt/json-schema-validator
public class JsonSchemaValidator {
    private static final JsonSchemaFactory factory =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012,
                    builder -> builder.schemaMappers(
                            schemaMappers -> schemaMappers.mapPrefix(
                                    "https://www.dapm.org/", "classpath:jsonschemas/")));
    private static final SchemaValidatorsConfig.Builder builder = SchemaValidatorsConfig.builder();
    private static final SchemaValidatorsConfig config = builder.build();

    /** Throws a RuntimeException if the provided JSON String does not adhere to the pipeline candidate JSON schema.
     *  Succeeds silently otherwise. */
    public static void validatePipelineCandidate(String inputJson) {
        String pipelineCandidateSchemaPath = "https://www.dapm.org/pipeline_candidate_schema.json";

        JsonSchema schema = factory.getSchema(SchemaLocation.of(pipelineCandidateSchemaPath), config);
        Set<ValidationMessage> assertions = schema.validate(inputJson, InputFormat.JSON,
                executionContext -> executionContext.getExecutionConfig().setFormatAssertionsEnabled(true));

        if (!assertions.isEmpty()) {
            StringBuilder sb = new StringBuilder("The provided JSON does not conform to schema:\n");
            for (ValidationMessage error : assertions) {
                sb.append(" - ").append(error.getMessage()).append("\n");
            }
            throw new RuntimeException(sb.toString());
        }

    }
    // TODO: try to combine this and the above method into 1 (where default is pipeline_candidate_schema).
    public static void validatePath(String jsonInput, String jsonPath) {
        String pipelineCandidateSchemaPath = "https://www.dapm.org/" + jsonPath;

        JsonSchema schema = factory.getSchema(SchemaLocation.of(pipelineCandidateSchemaPath), config);
        Set<ValidationMessage> assertions = schema.validate(jsonInput, InputFormat.JSON,
                executionContext -> executionContext.getExecutionConfig().setFormatAssertionsEnabled(true));

        if (!assertions.isEmpty()) {
            StringBuilder sb = new StringBuilder("The provided JSON does not conform to schema:\n");
            for (ValidationMessage error : assertions) {
                sb.append(" - ").append(error.getMessage()).append("\n");
            }
            throw new RuntimeException(sb.toString());
        }

    }
    // TODO: try deleting
    private static String readResourceFile(String pathFromResources) {
        try (InputStream in = JsonSchemaValidator.class.getClassLoader().getResourceAsStream(pathFromResources)) {
            if (in == null) throw new IllegalArgumentException("Resource not found: " + pathFromResources);
            try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8)) {
                scanner.useDelimiter("\\A");
                if (!scanner.hasNext()) {
                    throw new IllegalArgumentException("Resource is empty: " + pathFromResources);
                }
                return scanner.next();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource file: " + pathFromResources, e);
        }
    }
}
