package auto.bangumi.message.factory.annotation;

import auto.bangumi.message.enums.MessagePushType;

import java.lang.annotation.*;

/**
 * Message push strategy annotation.
 *
 * @author sakura
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessagePushMethod {
    MessagePushType type();
}
