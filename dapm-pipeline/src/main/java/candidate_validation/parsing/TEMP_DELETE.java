package candidate_validation.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.*;
import java.util.Set;

public class TEMP_DELETE {
}

class JsonSchemaValidatorTEMP {

    private final SchemaProvider schemaProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSchemaFactory factory =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    private final SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();

    public JsonSchemaValidatorTEMP(SchemaProvider schemaProvider) {
        this.schemaProvider = schemaProvider;
    }

    /*
    public void validatePipelineCandidate(String inputJson) {
        try {
            String schemaString = schemaProvider.getSchema("pipeline_candidate_schema.json");
            JsonNode schemaNode = objectMapper.readTree(schemaString);
            JsonSchema schema = factory.getSchema(schemaNode, config);

            Set<ValidationMessage> assertions = schema.validate(inputJson, InputFormat.JSON,
                    executionContext -> executionContext.getExecutionConfig().setFormatAssertionsEnabled(true));

            if (!assertions.isEmpty()) {
                StringBuilder sb = new StringBuilder("The provided JSON does not conform to schema:\n");
                for (ValidationMessage error : assertions) {
                    sb.append(" - ").append(error.getMessage()).append("\n");
                }
                throw new RuntimeException(sb.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Schema validation failed", e);
        }
    }

     */
}

interface SchemaProvider {
    String getSchema(String schemaName);
}

class ResourceSchemaProvider implements SchemaProvider {
    @Override
    public String getSchema(String schemaName) {
        return "https://www.dapm.org/" + schemaName;
    }
}

class TestSchemaProvider implements SchemaProvider {
    @Override
    public String getSchema(String schemaName) {
        return "classpath:jsonschemas/" + schemaName;
    }
}