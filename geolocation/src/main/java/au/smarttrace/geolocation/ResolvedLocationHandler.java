/**
 *
 */
package au.smarttrace.geolocation;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@FunctionalInterface
public interface ResolvedLocationHandler<T> {
    /**
     * @param userData
     * @param loc
     * @param status request status.
     */
    void handle(T userData, Location loc, RequestStatus status);
}
