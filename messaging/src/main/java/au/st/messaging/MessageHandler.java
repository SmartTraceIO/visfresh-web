/**
 *
 */
package au.st.messaging;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface MessageHandler<M> {
    String getMessageType();
    Class<M> getMessageClass();
    void handle(M m) throws SystemMessageException;
}
