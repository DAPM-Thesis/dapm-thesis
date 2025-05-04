package candidate_validation.parsing;

import com.networknt.schema.*;

import java.net.URI;
import java.util.Set;

// based on the NetworkNT readme: https://github.com/networknt/json-schema-validator
public class JsonSchemaValidator {
    private final String configSchemasFolderURI;
    private final JsonSchemaFactory factory;
    private static final SchemaValidatorsConfig.Builder builder = SchemaValidatorsConfig.builder();
    private static final SchemaValidatorsConfig config = builder.build();

    public JsonSchemaValidator(URI configSchemasFolderURI) {
        this.configSchemasFolderURI = configSchemasFolderURI.toString();
        this.factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012,
                builder -> builder.schemaMappers(schemaMappers -> schemaMappers
                        .mapPrefix("https://www.dapm.org/config/", this.configSchemasFolderURI)
                        .mapPrefix("https://www.dapm.org/", "classpath:jsonschemas/")));
    }

    /**
     * Throws a RuntimeException if the provided JSON String does not adhere to the pipeline candidate JSON schema.
     * Succeeds silently otherwise.
     */
    public void validatePipelineCandidate(String inputJson) {
        validateJsonAgainstSchema(inputJson,
                "https://www.dapm.org/pipeline_candidate_schema.json"
        );
    }

    public void validateConfiguration(String configJson, String configFilenameWithExtension) {
        validateJsonAgainstSchema(configJson,
                "https://www.dapm.org/config/" + configFilenameWithExtension
        );
    }

    private void validateJsonAgainstSchema(String jsonInput, String schemaPath) {
        JsonSchema schema = factory.getSchema(SchemaLocation.of(schemaPath), config);
        Set<ValidationMessage> assertions = schema.validate(jsonInput, InputFormat.JSON,
                executionContext -> executionContext.getExecutionConfig().setFormatAssertionsEnabled(true)
        );

        if (!assertions.isEmpty()) {
            StringBuilder sb = new StringBuilder("The provided JSON does not conform to schema:\n");
            for (ValidationMessage error : assertions) {
                sb.append(" - ").append(error.getMessage()).append("\n");
            }
            throw new RuntimeException(sb.toString());
        }
    }

}
