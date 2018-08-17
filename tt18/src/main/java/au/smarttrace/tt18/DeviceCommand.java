/**
 *
 */
package au.smarttrace.tt18;



/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceCommand {
    /**
     * Command ID.
     */
    private Long id;
    /**
     * Command to device
     */
    private String command;

    /**
     * Default constructor.
     */
    public DeviceCommand() {
        super();
    }

    /**
     * @return the command
     */
    public String getCommand() {
        return command;
    }
    /**
     * @param command the command to set
     */
    public void setCommand(final String command) {
        this.command = command;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithId#getId()
     */
    public Long getId() {
        return id;
    }
}
