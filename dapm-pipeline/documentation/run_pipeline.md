# Running a pipeline
Run a pipeline starting from a pipeline assembly Json representation in your organization.

## 1. Select a pipeline assembly Json
Specify the path to your pipeline assembly JSON in your application. Update the file path in the main method.
```java
public static void main(String[] args) {
    String contents;
    try {
        contents = Files.readString(Paths.get("orgC/src/main/resources/simple_pipeline.json"));
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
```
## 2. Get the Json config schemas
Each processing element template requires a corresponding JSON config schema. Make sure all schemas needed by the pipeline are present in the given location.
```java
URI configURI = Paths.get("orgC/src/main/resources/config_schemas").toUri();
```

## 3. Create the pipeline candidate
Transform the Json contents into a `ValidatedPipeline` object. This will only happen if the Json was specified correctly.
```java
PipelineCandidate pipelineCandidate = new PipelineCandidate(contents, configURI);
```

## 4. Create the validated pipeline
Validate the PipelineCandidate. If successfull, a `ValidatedPipeline` object has been created.
```java
ValidatedPipeline validatedPipeline = new ValidatedPipeline(pipelineCandidate);
```

## 5. Build the pipeline
Validated pipelines can be built using the `PipelineBuilder`. It also takes a pipelineID as argument.
```java
    public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(OrgAApplication.class, args);
    PipelineBuilder pipelineBuilder = context.getBean(PipelineBuilder.class);

    String pipelineID = "PipelineExample"

    pipelineBuilder.buildPipeline(pipelineID, validatedPipeline);
}
```

## 6. Execute the pipeline
Use the `pipelineID` as argument to execute the pipeline.
```java
    public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(OrgAApplication.class, args);
    PipelineExecutionService executionService = context.getBean(PipelineExecutionService.class);

    String pipelineID = "PipelineExample"

    executionService.start(pipelineID);
}
```

## 7. Terminate the pipeline
Use the `pipelineID` as argument to terminate the pipeline.
```java
    public static void main(String[] args) {

    String pipelineID = "PipelineExample"

    executionService.terminate(pipelineID);
}
```