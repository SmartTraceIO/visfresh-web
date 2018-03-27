/**
 *
 */
package com.visfresh.web;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.visfresh.MessageParserUtils;
import com.visfresh.bt04.Bt04Message;
import com.visfresh.bt04.Bt04Service;
import com.visfresh.bt04.MessageParser;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Bt04Servlet extends HttpServlet {
    private static final long serialVersionUID = 824070442463613374L;

    private static final Logger log = LoggerFactory.getLogger(Bt04Servlet.class);

    private final MessageParser parser = new MessageParser();

    private Bt04Service service;

    /**
     * Default constructor.
     */
    public Bt04Servlet() {
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
        log.debug("BT04 message has received: " + rawData);

        Bt04Message msgs;
        try {
            msgs = parser.parse(rawData);
        } catch (final Exception e) {
            log.error("Failed to parse BT04 message: " + rawData);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        service.process(msgs);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getOutputStream().flush();
        resp.getOutputStream().close();
    }

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);

        final ConfigurableApplicationContext ctxt = ApplicationInitializer.getBeanContext(
                getServletContext());
        service = ctxt.getBean(Bt04Service.class);

        log.debug("BT04 servlet has initialized");
    }
}
