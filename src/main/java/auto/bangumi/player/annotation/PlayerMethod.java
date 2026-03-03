package auto.bangumi.player.annotation;

import auto.bangumi.common.enums.PlayerEnums;

import java.lang.annotation.*;

/**
 * PlayerMethod
 *
 * @author sakura
 * @version 1.0
 * @since 2026/03/03
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PlayerMethod {

    PlayerEnums method();
}
