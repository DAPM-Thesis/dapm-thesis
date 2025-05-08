package processors;

import com.google.auto.service.AutoService;
import communication.message.AutoRegisterMessage;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("communication.message.AutoRegisterMessage")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class MessageTypeRegistryProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(AutoRegisterMessage.class);
        StringBuilder factoryCode = new StringBuilder();

        factoryCode.append("package communication.message;\n\n");
        factoryCode.append("import java.util.HashMap;\n");
        factoryCode.append("import java.util.Map;\n\n");

        factoryCode.append("public class MessageTypeFactory {\n");
        factoryCode.append("    private static final Map<String, Class<? extends Message>> nameToClass = new HashMap<>();\n\n");

        factoryCode.append("    static {\n");
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) continue;
            String fullyQualifiedClassName = ((TypeElement) element).getQualifiedName().toString();
            String simpleClassName = element.getSimpleName().toString();
            factoryCode.append("        register(\"" + simpleClassName + "\", " + fullyQualifiedClassName + ".class);\n");
        }
        factoryCode.append("    }\n\n");

        factoryCode.append("    public static Class<? extends Message> getMessageType(String simpleClassName) {\n");
        factoryCode.append("        if (!nameToClass.containsKey(simpleClassName)) { throw new IllegalArgumentException(\"Unknown message type: \" + simpleClassName);}\n");
        factoryCode.append("        return nameToClass.get(simpleClassName);\n");
        factoryCode.append("    }\n\n");

        factoryCode.append("    private static void register(String simpleClassName, Class<? extends Message> messageClass) {\n");
        factoryCode.append("        nameToClass.put(simpleClassName, messageClass);\n");
        factoryCode.append("    }\n");
        factoryCode.append("}\n");

        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile("communication.message.MessageTypeFactory");
            try (java.io.Writer writer = file.openWriter()) {
                writer.write(factoryCode.toString());
            }
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write MessageTypeFactory: " + e.getMessage());
        }


        return true;
    }
}
