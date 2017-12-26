/**
 *
 */
package au.smarttrace.ctrl.client.resp;

import java.util.List;

import au.smarttrace.ctrl.ServiceResponse;
import au.smarttrace.ctrl.res.ColorDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ColorListResponse extends ServiceResponse<List<ColorDto>> {
    /**
     * Default constructor.
     */
    public ColorListResponse() {
        super();
    }
}
