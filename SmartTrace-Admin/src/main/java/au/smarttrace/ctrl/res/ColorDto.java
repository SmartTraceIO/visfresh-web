/**
 *
 */
package au.smarttrace.ctrl.res;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import au.smarttrace.Color;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ColorDto {
    private String color;
    private String htmlValue;

    /**
     * Default constructor.
     */
    public ColorDto() {
        super();
    }

    public ColorDto(final Color c) {
        this.color = c.name();
        this.htmlValue = c.getHtmlValue();
    }

    /**
     * @return the color
     */
    @JsonGetter("color")
    public String getColor() {
        return color;
    }
    /**
     * @param color the color to set
     */
    @JsonSetter("color")
    public void setColor(final String color) {
        this.color = color;
    }
    /**
     * @return the htmlValue
     */
    @JsonGetter("htmlValue")
    public String getHtmlValue() {
        return htmlValue;
    }
    /**
     * @param htmlValue the htmlValue to set
     */
    @JsonSetter("htmlValue")
    public void setHtmlValue(final String htmlValue) {
        this.htmlValue = htmlValue;
    }
}
