/**
 *
 */
package com.visfresh.web;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.visfresh.Device;
import com.visfresh.DeviceCommand;
import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageParser;
import com.visfresh.DeviceMessageType;
import com.visfresh.db.DeviceCommandDao;
import com.visfresh.db.DeviceDao;
import com.visfresh.db.MessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceCommunicationServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(DeviceCommunicationServlet.class);
    private static final long serialVersionUID = -2549581331796018692L;

    private DeviceMessageParser parser;
    private MessageDao messageDao;
    private DeviceDao deviceDao;
    private DeviceCommandDao deviceCommandDao;

    /**
     * Default constructor.
     */
    public DeviceCommunicationServlet() {
        super();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        processMessage(req, resp);
    }
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        processMessage(req, resp);
    }

    /**
     * @param req request.
     * @param resp response.
     * @throws IOException
     */
    private void processMessage(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final String rawData = DeviceMessageParser.getContent(new InputStreamReader(req.getInputStream()));
        log.debug("device message has received: " + rawData);

        List<DeviceMessage> msgs;
        try {
            msgs = getParser().parse(rawData);
        } catch (final Exception e) {
            log.error("Failed to parse device message: " + rawData);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        boolean haveShutdown = false;
        for (final DeviceMessage msg : msgs) {
            //attempt to load device
            final Device device = deviceDao.getByImei(msg.getImei());

            if (device != null) {
                final List<DeviceCommand> commands = deviceCommandDao.getFoDevice(device.getImei());
                DeviceCommand cmd = null;

                if (!commands.isEmpty()) {
                    cmd = commands.get(0);
                    if (cmd.getCommand().toLowerCase().contains("shutdown")) {
                        haveShutdown = true;
                    }
                }

                if (msg.getType() == DeviceMessageType.RSP) {
                    //process response to server command
                    log.debug("Device response has received for " + msg.getImei());
                } else if (!haveShutdown){
                    messageDao.create(msg);
                } else {
                    log.debug("Shutdown command found for device "
                            + device.getImei() + ", current(s) message "
                            + rawData + " will not pushed to system");
                }

                if (cmd != null) {
                    final String command = cmd.getCommand();
                    log.debug("Sending command " + command + " to device " + device.getImei());
                    resp.getOutputStream().write(command.getBytes());

                    deviceCommandDao.delete(cmd);

                    if (haveShutdown) {
                        break;
                    }
                }
            } else {
                log.warn("Not found registered device " + msg.getImei());
            }
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getOutputStream().flush();
        resp.getOutputStream().close();
    }

    /**
     * @return the parser
     */
    public DeviceMessageParser getParser() {
        return parser;
    }
    /**
     * @param parser the parser to set
     */
    public void setParser(final DeviceMessageParser parser) {
        this.parser = parser;
    }

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
        super.init();

        setParser(new DeviceMessageParser());

        final ConfigurableApplicationContext ctxt = ApplicationInitializer.getBeanContext(
                getServletContext());
        messageDao = ctxt.getBean(MessageDao.class);
        deviceDao = ctxt.getBean(DeviceDao.class);
        deviceCommandDao = ctxt.getBean(DeviceCommandDao.class);

        log.debug("Device communication servlet has initialized");
    }
}
