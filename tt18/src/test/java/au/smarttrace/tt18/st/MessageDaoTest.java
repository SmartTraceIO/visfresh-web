/**
 *
 */
package au.smarttrace.tt18.st;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import au.smarttrace.tt18.junit.DaoTest;
import au.smarttrace.tt18.junit.db.DaoTestRunner;
import au.smarttrace.tt18.junit.db.DbSupport;
import au.smarttrace.tt18.st.db.MessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
@Category(DaoTest.class)
public class MessageDaoTest {
    @Autowired
    private DbSupport support;
    @Autowired
    private MessageDao dao;

    private Long companyId;

    @Before
    public void setUp() {
        companyId = support.createSimpleCompany("JUnit");
    }

    @Test
    public void testCheckDevice() {
        final String imei = "23948703298470983247";
        assertFalse(dao.checkDevice(imei));

        support.createSimpleDevice(companyId, imei);
        assertTrue(dao.checkDevice(imei));
    }

    @Test
    public void testCheckDisabledDevice() {
        final String imei = "23948703298470983247";
        support.createSimpleDevice(companyId, imei);
        assertTrue(dao.checkDevice(imei));

        support.getJdbc().update("update devices set active = false", new HashMap<>());
        assertFalse(dao.checkDevice(imei));
    }

    @After
    public void tearDown() {
        support.deleteMessages();
        support.deleteDevices();
        support.deleteCompanies();
    }
}
