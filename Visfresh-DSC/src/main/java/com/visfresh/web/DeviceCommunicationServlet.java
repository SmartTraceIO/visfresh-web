/**
 *
 */
package com.visfresh.web;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageParser;
import com.visfresh.DeviceMessageType;
import com.visfresh.db.MessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceCommunicationServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(DeviceCommunicationServlet.class);
    private static final long serialVersionUID = -2549581331796018692L;

    private DeviceMessageParser parser;
    private MessageDao dao;

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
        final DeviceMessage msg = getParser().parse(new InputStreamReader(req.getInputStream()));

        //attempt to load device
        log.debug("device message has received: " + msg);

        if (msg.getType() == DeviceMessageType.RSP) {
            //process response to server command
            log.debug("Device response has received " + msg);
        } else {
            dao.create(msg);
        }

        resp.setStatus(HttpServletResponse.SC_OK);
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
        dao = ctxt.getBean(MessageDao.class);

        log.debug("Device communication servlet has initialized");
    }
}
