/**
 *
 */
package com.visfresh.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceCommunicationServlet extends HttpServlet {
    private static final long serialVersionUID = 426829433723539068L;
    private static final Logger log = LoggerFactory.getLogger(DeviceCommunicationServlet.class);

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
        final InputStream in = req.getInputStream();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        final byte[] buff = new byte[256];

        while ((len = in.read(buff)) > -1) {
            out.write(buff, 0, len);
        }

        log.debug("device message has received:\n " + new String(out.toByteArray()));
        resp.getOutputStream().close();
    }

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
        super.init();
//        setParser(new DeviceMessageParser());
//
//        final ConfigurableApplicationContext ctxt = ApplicationInitializer.getBeanContext(
//                getServletContext());
//        service = ctxt.getBean(DeviceMessageService.class);

        log.debug("Device communication servlet has initialized");
    }
}
