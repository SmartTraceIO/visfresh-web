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

import com.visfresh.DeviceCommand;
import com.visfresh.DeviceMessageParser;
import com.visfresh.MessageParserUtils;
import com.visfresh.service.DeviceMessageService;

import au.smarttrace.geolocation.DataWithGsmInfo;
import au.smarttrace.geolocation.DeviceMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceCommunicationServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(DeviceCommunicationServlet.class);
    private static final long serialVersionUID = -2549581331796018692L;

    private DeviceMessageParser parser;
    private DeviceMessageService service;

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
        final String rawData = MessageParserUtils.getContent(new InputStreamReader(req.getInputStream()));
        log.debug("device message has received: " + rawData);

        List<DataWithGsmInfo<DeviceMessage>> msgs;
        try {
            msgs = getParser().parse(rawData);
        } catch (final Exception e) {
            log.error("Failed to parse device message: " + rawData);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        final DeviceCommand cmd = service.process(msgs, rawData);
        resp.setStatus(HttpServletResponse.SC_OK);

        if (cmd != null) {
            final String command = cmd.getCommand();
            log.debug("Sending command " + command + " to device "
                    + msgs.get(0).getUserData().getImei());
            resp.getOutputStream().write(command.getBytes());
        }

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
        service = ctxt.getBean(DeviceMessageService.class);

        log.debug("Device communication servlet has initialized");
    }
}
