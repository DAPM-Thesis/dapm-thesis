package communication.message;

import communication.message.serialization.deserialization.DeserializationStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS) // TODO: only used at compile time, not runtime - change to RetentionPolicy.RUNTIME when implementing processor to generate files.
@Target(ElementType.TYPE)
public @interface AutoRegisterMessage {
    Class<? extends DeserializationStrategy> deserialization();
}
