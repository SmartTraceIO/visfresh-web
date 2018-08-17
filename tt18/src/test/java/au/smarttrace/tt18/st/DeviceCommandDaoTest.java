/**
 *
 */
package au.smarttrace.tt18.st;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import au.smarttrace.tt18.DeviceCommand;
import au.smarttrace.tt18.junit.DaoTest;
import au.smarttrace.tt18.junit.db.DaoTestRunner;
import au.smarttrace.tt18.junit.db.DbSupport;
import au.smarttrace.tt18.st.db.DeviceCommandDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
@Category(DaoTest.class)
public class DeviceCommandDaoTest{
    @Autowired
    private DbSupport support;
    @Autowired
    private DeviceCommandDao dao;

    private Long companyId;
    private final String device = "23948703298470983247";

    /**
     * Default constructor.
     */
    public DeviceCommandDaoTest() {
        super();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        companyId = support.createSimpleCompany("JUnit");
        support.createSimpleDevice(companyId, device);
    }

    @Test
    public void testReadAndDelete() {
        //create device command
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("command", "stop");
        params.put("date", new Date());
        params.put("device", device);

        support.getJdbc().update("INSERT INTO devicecommands(command, device, date)"
                + "VALUES(:command, :device, :date)" , params);

        final List<DeviceCommand> list = dao.getFoDevice(device);
        assertEquals(1, list.size());

        dao.delete(list.get(0));
        assertEquals(0, dao.getFoDevice(device).size());
    }
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        //clean up data base
        support.getJdbc().update("delete from devicecommands", new HashMap<String, Object>());
        support.deleteDevices();
        support.deleteCompanies();
    }
}
