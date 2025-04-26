package candidate_validation.parsing.schema_provider.impl;

import candidate_validation.parsing.schema_provider.SchemaProvider;

public class ClasspathSchemaProvider implements SchemaProvider {
    @Override
    public java.io.InputStream getSchema(String schemaPath) {
        return getClass().getClassLoader().getResourceAsStream(schemaPath);
    }
}
