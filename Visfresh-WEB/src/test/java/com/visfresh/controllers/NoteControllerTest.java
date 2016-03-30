/**
 *
 */
package com.visfresh.controllers;

import org.junit.Test;

import com.visfresh.controllers.restclient.NoteRestClient;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NoteControllerTest extends AbstractRestServiceTest {
    private NoteRestClient client = new NoteRestClient(UTC);

    /**
     * Default constructor.
     */
    public NoteControllerTest() {
        super();
    }

    @Test
    public void testSaveNote() {

    }
}
