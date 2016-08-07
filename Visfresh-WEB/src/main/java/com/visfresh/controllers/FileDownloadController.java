/**
 *
 */
package com.visfresh.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import javax.activation.FileTypeMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.services.AuthenticationException;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("FileDownload")
@RequestMapping("/rest")
public class FileDownloadController extends AbstractController {
    /**
     * 
     */
    private static final String URL_ENCODING = "UTF-8";
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FileDownloadController.class);
    private static final long ALIVE_TIME_OUT = 5 * 60 * 1000l;
    private File tmpRoot;
    private Timer timer = new Timer(true);
    private static final String DOWNLOAD_FILE = "downloadFile";

    /**
     * Default constructor.
     */
    public FileDownloadController() {
        super();
    }

    @PostConstruct
    public void initialize() {
        this.tmpRoot = new File(System.getProperty("java.io.tmpdir"), "smarttrace-tmps");
        if (!tmpRoot.exists()) {
            tmpRoot.mkdir();
        }

        //start cleaning service
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                cleanTmpDir();
            }
        }, 60 * 1000l);
    }
    /**
     *
     */
    protected void cleanTmpDir() {
        final long time = System.currentTimeMillis();
        //clear temporary directory
        final File[] files = tmpRoot.listFiles();
        for (final File file : files) {
            if (file.lastModified() - time > ALIVE_TIME_OUT) {
                file.delete();
            }
        }
    }

    @PreDestroy
    public void destroy() {
        timer.cancel();
    }
    /**
     * @param authToken authentication token.
     * @param device device.
     * @return ID of saved device.
     * @throws AuthenticationException
     * @throws RestServiceException
     * @throws IOException
     */
    @RequestMapping(value = "/" + DOWNLOAD_FILE + "/{authToken}/{fileNamePart}", method = RequestMethod.GET)
    public ResponseEntity<?> downloadFile(@PathVariable final String authToken,
            @PathVariable final String fileNamePart,
            final HttpServletRequest req)
            throws AuthenticationException, RestServiceException, IOException {

        //check logged in.
        final User user = getLoggedInUser(authToken);
        checkAccess(user, Role.BasicUser);

        //extract file name
        String fileName = req.getRequestURI().substring(req.getRequestURI().indexOf(authToken)
                + authToken.length() + 1);
        fileName = URLDecoder.decode(fileName, URL_ENCODING);

        final File file = getFile(fileName);
        if (!file.exists()) {
            log.error("File not found " + fileName);
            throw new FileNotFoundException("File not found " + fileName);
        }
        log.debug("downloading file " + fileName);

        final InputStream in = new TmpFileInputStream(file);
        return ResponseEntity
                .ok()
                .contentType(getContentType(file))
                .contentLength(file.length())
                .body(new InputStreamResource(in, file.getName()));
    }
    /**
     * @param baseRestUrl base URL.
     * @param accessToken access token.
     * @param fileName file name to download.
     * @return URL for download given file.
     * @throws UnsupportedEncodingException
     */
    public static String createDownloadUrl(final String baseRestUrl, final String accessToken, final String fileName)
            throws UnsupportedEncodingException {
        final StringBuilder sb = new StringBuilder(baseRestUrl);
        if (!baseRestUrl.endsWith("/")) {
            sb.append('/');
        }

        sb.append(DOWNLOAD_FILE);
        sb.append('/');
        sb.append(accessToken);
        sb.append('/');
        sb.append(URLEncoder.encode(fileName, URL_ENCODING));

        return sb.toString();
    }
    /**
     * @param suffix file suffix.
     * @return new file by unique name.
     * @throws IOException
     */
    public synchronized File createTmpFile(final String suffix) throws IOException {
        final char separator = '-';

        long id = 0;
        while (true) {
            final File file = new File(tmpRoot, id + separator + suffix);
            if (!file.exists()) {
                file.createNewFile();
                return file;
            }
            id++;
        }
    }
    /**
     * @param fileName file name.
     * @return file from file name.
     */
    private File getFile(final String fileName) {
        return new File(tmpRoot, fileName);
    }
    /**
     * @param file file.
     * @return media type for given file.
     */
    protected static MediaType getContentType(final File file) {
        try {
            return MediaType.parseMediaType(FileTypeMap.getDefaultFileTypeMap().getContentType(file));
        } catch (final Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
