/**
 *
 */
package au.smarttrace.svc;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.smarttrace.ApplicationException;
import au.smarttrace.Color;
import au.smarttrace.Company;
import au.smarttrace.Device;
import au.smarttrace.company.CompaniesDao;
import au.smarttrace.ctrl.res.ListResponse;
import au.smarttrace.device.DevicesDao;
import au.smarttrace.device.DevicesService;
import au.smarttrace.device.GetDevicesRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DevicesServiceImpl implements DevicesService {
    @Autowired
    private DevicesDao dao;
    @Autowired
    private CompaniesDao companyDao;

    /**
     * Default constructor.
     */
    public DevicesServiceImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see au.smarttrace.device.DevicesService#deleteDevice(java.lang.String)
     */
    @Override
    public void deleteDevice(final String imei) {
        dao.deleteDevice(imei);
    }
    /* (non-Javadoc)
     * @see au.smarttrace.device.DevicesService#getDevice(java.lang.String)
     */
    @Override
    public Device getDevice(final String imei) {
        return dao.getByImei(imei);
    }
    /* (non-Javadoc)
     * @see au.smarttrace.device.DevicesService#moveDevice(java.lang.String, java.lang.Long)
     */
    @Override
    public Device moveDevice(final String device, final Long company) throws ApplicationException {
        //get device
        final Device oldDevice = dao.getByImei(device);
        if (oldDevice == null) {
            throw new ApplicationException("Not found device " + device, HttpServletResponse.SC_NOT_FOUND);
        } else if (isVirtualDevice(oldDevice)) {
            throw new ApplicationException("Device " + device
                    + " is virtual and can't be moved", HttpServletResponse.SC_BAD_REQUEST);
        }

        //get company
        final Company c = companyDao.getById(company);
        if (c == null) {
            throw new ApplicationException("Not found company " + company, HttpServletResponse.SC_NOT_FOUND);
        }

        //create new device
        final Device backup = createVirtualDevice(oldDevice);

        //switch device to new company
        dao.moveToNewCompany(oldDevice, c, backup);
        return backup;
    }
    /* (non-Javadoc)
     * @see au.smarttrace.device.DevicesService#updateDevice(au.smarttrace.Device)
     */
    @Override
    public void updateDevice(final Device d) {
        dao.updateDevice(d);
    }
    /* (non-Javadoc)
     * @see au.smarttrace.device.DevicesService#createDevice(au.smarttrace.Device)
     */
    @Override
    public void createDevice(final Device d) {
        dao.updateDevice(d);
    }
    /* (non-Javadoc)
     * @see au.smarttrace.device.DevicesService#getDevices(au.smarttrace.device.GetDevicesRequest)
     */
    @Override
    public ListResponse<Device> getDevices(final GetDevicesRequest req) {
        return dao.getDevices(req);
    }
    /* (non-Javadoc)
     * @see au.smarttrace.user.UsersService#getAvailableColors()
     */
    @Override
    public List<Color> getAvailableColors() {
        final List<Color> colors = new LinkedList<>();
        for (final Color color : Color.values()) {
            colors.add(color);
        }
        return colors;
    }
    /**
     * @param device device.
     * @return true if the device is virtual.
     */
    private boolean isVirtualDevice(final Device device) {
        return device.getImei().startsWith(createVirtualPrefix(device));
    }
    /**
     * @param device device.
     * @return virtual prefix for given device.
     */
    private String createVirtualPrefix(final Device device) {
        return device.getCompany() + "_";
    }
    /**
     * @param device
     * @return
     */
    private Device createVirtualDevice(final Device device) {
        final Device d = new Device();
        d.setActive(device.isActive());
        d.setCompany(device.getCompany());
        d.setDescription(device.getDescription());
        d.setImei(createVirtualPrefix(device) + device.getImei());
        d.setName(device.getName());
        d.setColor(device.getColor());

        if (dao.getByImei(d.getImei()) != null) {
            dao.updateDevice(d);
        } else {
            dao.createDevice(d);
        }

        dao.setTripCount(d, dao.getTripCount(device));
        return d;
    }
}
