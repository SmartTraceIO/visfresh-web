/**
 *
 */
package com.visfresh.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("dummy")
public class DummyController {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DummyController.class);

    /**
     * @param sn serial number.
     * @param im IMEI code.
     * @return ok string
     */
    @RequestMapping(value = "/shipment.do", method = RequestMethod.GET)
    public @ResponseBody String registerDevice(
            @RequestParam final String sn,
            @RequestParam final String im) {
        // /web/shipment.do?sn=000001&im=611234560000019
        log.debug("Device register request has accepted. SN: " + sn + ", IMEI: " + im);
        return "Ok";
    }
}
