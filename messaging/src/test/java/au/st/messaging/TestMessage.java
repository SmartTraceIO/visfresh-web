/**
 *
 */
package au.st.messaging;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TestMessage {
    private int intValue;
    private String stringVlue;

    /**
     * Default constructor.
     */
    public TestMessage() {
        super();
    }

    /**
     * @return the intValue
     */
    public int getIntValue() {
        return intValue;
    }
    /**
     * @param intValue the intValue to set
     */
    public void setIntValue(final int intValue) {
        this.intValue = intValue;
    }
    /**
     * @return the stringVlue
     */
    public String getStringVlue() {
        return stringVlue;
    }
    /**
     * @param stringVlue the stringVlue to set
     */
    public void setStringVlue(final String stringVlue) {
        this.stringVlue = stringVlue;
    }
}
