/**
 *
 */
package au.smarttrace.eel.service;

import org.springframework.stereotype.Component;

import au.smarttrace.eel.Utils;
import au.smarttrace.eel.rawdata.DefaultPackageResponseBody;
import au.smarttrace.eel.rawdata.EelMessage;
import au.smarttrace.eel.rawdata.EelPackage;
import au.smarttrace.eel.rawdata.HeartbeatPackageBody;
import au.smarttrace.eel.rawdata.InstructionPackageResponseBody;
import au.smarttrace.eel.rawdata.LocationPackageBody;
import au.smarttrace.eel.rawdata.LoginPackageBody;
import au.smarttrace.eel.rawdata.LoginPackageResponse;
import au.smarttrace.eel.rawdata.MessagePackageBody;
import au.smarttrace.eel.rawdata.MessagePackageResponse;
import au.smarttrace.eel.rawdata.PackageBody;
import au.smarttrace.eel.rawdata.PackageHeader;
import au.smarttrace.eel.rawdata.ParamSetPackageBody;
import au.smarttrace.eel.rawdata.ParamSetPackageResponse;
import au.smarttrace.eel.rawdata.WarningPackageBody;
import au.smarttrace.eel.rawdata.WarningPackageResponse;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ImmediatelyResponderImpl implements ImmediatelyResponder {

    /**
     * Default constructor.
     */
    public ImmediatelyResponderImpl() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * au.smarttrace.eel.service.ImmediatelyResponder#respond(au.smarttrace.eel.
     * rawdata.EelMessage)
     */
    @Override
    public EelMessage respond(final EelMessage msg) {
        final EelMessage respMessage = new EelMessage();
        respMessage.setMark(msg.getMark());
        respMessage.setImei(msg.getImei());

        for (final EelPackage p : msg.getPackages()) {
            final PackageBody respBody = respondeTo(p.getBody());
            if (respBody != null) {
                final PackageHeader h = new PackageHeader();
                h.setMark(p.getHeader().getMark());
                h.setPid(p.getHeader().getPid());
                h.setSequence(p.getHeader().getSequence());

                final EelPackage resp = new EelPackage();
                resp.setBody(respBody);
                resp.setHeader(h);
                respMessage.getPackages().add(resp);
            }
        }

        return respMessage;
    }

    /**
     * @param msgPackage
     * @return
     */
    private PackageBody respondeTo(final PackageBody p) {
        final PackageBody resp;

        if (p instanceof LoginPackageBody) {
            resp = respondToLoginPackage((LoginPackageBody) p);
        } else if (p instanceof HeartbeatPackageBody) {
            resp = respondToHeartbeatPackage((HeartbeatPackageBody) p);
        } else if (p instanceof LocationPackageBody) {
            resp = respondToLocationPackage((LocationPackageBody) p);
        } else if (p instanceof WarningPackageBody) {
            resp = respondToWarningPackage((WarningPackageBody) p);
        } else if (p instanceof MessagePackageBody) {
            resp = respondToMessagePackage((MessagePackageBody) p);
        } else if (p instanceof ParamSetPackageBody) {
            resp = respondToParamSetPackage((ParamSetPackageBody) p);
        } else if (p instanceof InstructionPackageResponseBody) {
            resp = respondToInstructionPackageResponse((InstructionPackageResponseBody) p);
        } else if (p instanceof DefaultPackageResponseBody) {
            resp = respondToBroadcastPackageResponse((DefaultPackageResponseBody) p);
        } else {
            throw new RuntimeException("Unhandled package " + p.getClass().getName());
        }

        return resp;
    }
    /**
     * @param p
     * @return
     */
    private ParamSetPackageResponse respondToParamSetPackage(final ParamSetPackageBody p) {
        final ParamSetPackageResponse r = new ParamSetPackageResponse();
        r.setShouldSendNext(false); //not yet can handle the param set
        return r;
    }
    /**
     * @param p
     * @return
     */
    private PackageBody respondToBroadcastPackageResponse(final DefaultPackageResponseBody p) {
        return null;
    }
    /**
     * @param p
     * @return
     */
    private PackageBody respondToInstructionPackageResponse(final InstructionPackageResponseBody p) {
        // not any response.
        return null;
    }
    /**
     * @param p
     * @return
     */
    protected MessagePackageResponse respondToMessagePackage(final MessagePackageBody p) {
        final MessagePackageResponse r = new MessagePackageResponse();
        r.setPhoneNumber(p.getPhoneNumber());
        //r.setContent(content); nothing for now
        return r;
    }
    /**
     * @param p
     * @return
     */
    private WarningPackageResponse respondToWarningPackage(final WarningPackageBody p) {
        final WarningPackageResponse r = new WarningPackageResponse();
        r.setContent("Warning received: " + p.getWarningType());
        return r;
    }

    /**
     * @param p
     * @return
     */
    private PackageBody respondToLocationPackage(final LocationPackageBody p) {
        final DefaultPackageResponseBody r = new DefaultPackageResponseBody();
        return r;
    }
    /**
     * @param p
     * @return
     */
    private DefaultPackageResponseBody respondToHeartbeatPackage(final HeartbeatPackageBody p) {
        final DefaultPackageResponseBody r = new DefaultPackageResponseBody();
        return r;
    }
    /**
     * @param p
     * @return
     */
    private LoginPackageResponse respondToLoginPackage(final LoginPackageBody p) {
        final LoginPackageResponse r = new LoginPackageResponse();
        r.setProtocolVersion(1);
        r.setCurrentTimeUtc(Utils.timeToUtc(System.currentTimeMillis() / 1000L));
        r.setShouldUploadParamSetImmediately(false);
        r.setShouldUploadParamSetInFuture(false);
        return r;
    }
}
