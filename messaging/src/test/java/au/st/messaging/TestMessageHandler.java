/**
 *
 */
package au.st.messaging;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface TestMessageHandler extends MessageHandler<TestMessage> {
    String TYPE = "TestMessage";

    /* (non-Javadoc)
     * @see au.st.messaging.MessageHandler#getMessageClass()
     */
    @Override
    default Class<TestMessage> getMessageClass() {
        return TestMessage.class;
    }
    /* (non-Javadoc)
     * @see au.st.messaging.MessageHandler#getMessageType()
     */
    @Override
    default String getMessageType() {
        return TYPE;
    }
    /* (non-Javadoc)
     * @see au.st.messaging.MessageHandler#getMaxNumberOfRetry()
     */
    @Override
    default int getMaxNumberOfRetry() {
        return 10;
    }
}
