/**
 *
 */
package au.smarttrace.ctrl.req;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Order {
    private String field;
    private boolean ascent = true;

    /**
     * Default constructor.
     */
    public Order() {
        super();
    }

    /**
     * @param field
     * @param asc
     */
    public Order(final String field, final boolean asc) {
        this.field = field;
        this.ascent = asc;
    }

    /**
     * @return the field
     */
    public String getField() {
        return field;
    }
    /**
     * @param field the field to set
     */
    public void setField(final String field) {
        this.field = field;
    }
    /**
     * @return the ascent
     */
    public boolean isAscent() {
        return ascent;
    }
    /**
     * @param ascent the ascent to set
     */
    public void setAscent(final boolean ascent) {
        this.ascent = ascent;
    }
}
