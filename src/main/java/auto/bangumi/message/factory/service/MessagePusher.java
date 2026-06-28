package auto.bangumi.message.factory.service;

/**
 * Message push strategy interface.
 *
 * @author sakura
 */
public interface MessagePusher {

    boolean push(String message);
}
