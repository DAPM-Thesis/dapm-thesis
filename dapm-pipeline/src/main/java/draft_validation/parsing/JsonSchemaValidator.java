package draft_validation.parsing;

import com.networknt.schema.*;
import java.util.Set;

// based on the NetworkNT readme: https://github.com/networknt/json-schema-validator
public class JsonSchemaValidator {

    private static final JsonSchemaFactory factory =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012,
                    builder -> builder.schemaMappers(
                            schemaMappers -> schemaMappers.mapPrefix(
                                    "https://www.dapm.org/", "classpath:jsonschemas/")));
    private static final SchemaValidatorsConfig.Builder builder = SchemaValidatorsConfig.builder();
    private static final SchemaValidatorsConfig config = builder.build();

    /** Throws a RuntimeException if the provided JSON String does not adhere to the pipeline draft JSON schema.
     *  Succeeds silently otherwise. */
    public static void validatePipelineDraft(String inputJson) {
        String pipelineDraftSchemaPath = "https://www.dapm.org/pipeline_draft_schema.json";

        JsonSchema schema = factory.getSchema(SchemaLocation.of(pipelineDraftSchemaPath), config);
        Set<ValidationMessage> assertions = schema.validate(inputJson, InputFormat.JSON, executionContext -> {
            executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
        });

        if (!assertions.isEmpty()) { // the input json does not follow the schema
            StringBuilder sb = new StringBuilder("The provided JSON does not conform to schema:\n");
            for (ValidationMessage error : assertions) {
                sb.append(" - ").append(error.getMessage()).append("\n");
            }
            throw new RuntimeException(sb.toString());
        }

    }
}
