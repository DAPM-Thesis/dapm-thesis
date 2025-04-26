package candidate_validation.parsing.schema_provider;

import java.io.InputStream;

public interface SchemaProvider {
    InputStream getSchema(String schemaPath);
}
