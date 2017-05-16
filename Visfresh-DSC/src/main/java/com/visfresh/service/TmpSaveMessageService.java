/**
 *
 */
package com.visfresh.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageType;
import com.visfresh.StationSignal;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class TmpSaveMessageService {
    /**
     *
     */
    private static final String TEMPERATURE = "temperature";
    //station properties
    private static final String MNC = "mnc";
    private static final String MCC = "mcc";
    private static final String LEVEL = "level";
    private static final String LAC = "lac";
    private static final String CI = "ci";

    //message properties
    private static final String STATIONS = "stations";
    private static final String TYPE = "type";
    private static final String TIME = "time";
    private static final String BATTERY = "battery";
    private static final String TYPE_STRING = "typeString";
    private static final String MESSAGE = "message";
    private static final String IMEI = "imei";

    private static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Storage folder.
     */
    private Path storage;

    /**
     * default constructor.
     */
    protected TmpSaveMessageService() {
        super();
    }
    /**
     * @param storage storage folder.
     */
    public TmpSaveMessageService(final Path storage) {
        super();
        setStorage(storage);
    }
    /**
     * @param env Spring environment.
     */
    @Autowired
    public TmpSaveMessageService(final Environment env) {
        this(getStorageFolder(env));
    }

    /**
     * @param storage
     * @return
     */
    public Path setStorage(final Path storage) {
        return this.storage = storage;
    }
    /**
     * @param env Spring environment.
     * @return message storage.
     */
    private static Path getStorageFolder(final Environment env) {
        final String prop = env.getProperty("tmp.save.message.file", System.getProperty("user.home")
                + File.separator + "smarttrace/recovery/rawreadings");
        return Paths.get(prop);
    }

    @PostConstruct
    protected void init() throws IOException {
        if (!Files.exists(storage)) {
            Files.createDirectories(storage);
        } else if (!Files.isDirectory(storage)) {
            throw new RuntimeException("File storage is not a dirrctory "
                    + storage.toAbsolutePath().toString());
        }
    }

    /**
     * @param msg saves device message to file.
     * @throws IOException
     */
    public synchronized void saveMessage(final DeviceMessage msg) throws IOException {
        //serialize message to JSON string
        final String ser = serialize(msg);

        //save message to file
        final OutputStream out = createOutFile();
        try {
            out.write(ser.getBytes(UTF8));
            out.flush();
        } finally {
            out.close();
        }
    }
    /**
     * @return first saved message.
     * @throws IOException
     */
    public synchronized DeviceMessage removeAndGetMessage() throws IOException {
        //find first message
        final List<Integer> ids = getSortedFileIds();
        final Path file = storage.resolve(Integer.toString(ids.get(0)));

        //read message
        final String msg = new String(Files.readAllBytes(file), "UTF-8");
        final DeviceMessage m = parseDeviceMessage(msg);

        //remove message from disk
        Files.delete(file);
        return m;
    }
    /**
     * @return output stream.
     * @throws IOException
     */
    private OutputStream createOutFile() throws IOException {
        //get file names as integers.
        final List<Integer> files = getSortedFileIds();

        //create highest value for file name.
        final int next = files.size() == 0 ? 0 : files.get(files.size() - 1) + 1;

        //create new file
        final Path f = Files.createFile(storage.resolve(Integer.toString(next)));
        return Files.newOutputStream(f);
    }
    /**
     * @return
     * @throws IOException
     */
    private List<Integer> getSortedFileIds() throws IOException {
        final List<Integer> files = new LinkedList<>();
        Files.list(storage).forEach(p -> files.add(Integer.parseInt(p.getFileName().toString())));

        //sort integers for find highest.
        Collections.sort(files);
        return files;
    }
    /**
     * @param msg device message to serialize.
     * @return device message as JSON string.
     */
    protected String serialize(final DeviceMessage msg) {
        final SimpleDateFormat sdf = createDateFormat();

        final JsonObject obj = new JsonObject();
        obj.addProperty(IMEI, msg.getImei());
        obj.addProperty(MESSAGE, msg.getMessage());
        obj.addProperty(TYPE_STRING, msg.getTypeString());
        obj.addProperty(BATTERY, msg.getBattery());
        obj.addProperty(TIME, sdf.format(msg.getTime()));
        obj.addProperty(TYPE, msg.getType().toString());
        obj.addProperty(TEMPERATURE, msg.getTemperature());

        final JsonArray stations = new JsonArray();
        obj.add(STATIONS, stations);
        for (final StationSignal s : msg.getStations()) {
            stations.add(toJson(s));
        }

        return obj.toString();
    }
    protected DeviceMessage parseDeviceMessage(final String str) {
        //parse string to JSON
        final Reader in = new StringReader(str);
        final JsonObject json = new JsonParser().parse(in).getAsJsonObject();

        //parse JSON to Device Message
        final DeviceMessage msg = new DeviceMessage();
        msg.setImei(json.get(IMEI).getAsString());
        msg.setMessage(json.get(MESSAGE).getAsString());
        msg.setTypeString(json.get(TYPE_STRING).getAsString());
        msg.setBattery(json.get(BATTERY).getAsInt());
        msg.setTime(parseDate(json.get(TIME).getAsString()));
        msg.setType(DeviceMessageType.valueOf(json.get(TYPE).getAsString()));
        msg.setTemperature(json.get(TEMPERATURE).getAsDouble());

        final JsonArray stations = json.get(STATIONS).getAsJsonArray();
        for (final JsonElement el : stations) {
            msg.getStations().add(parseStationSignal(el.getAsJsonObject()));
        }

        return msg;
    }

    /**
     * @param s station signal.
     * @return signal serialized to JSON object.
     */
    private JsonObject toJson(final StationSignal s) {
        final JsonObject json = new JsonObject();
        json.addProperty(CI, s.getCi());
        json.addProperty(LAC, s.getLac());
        json.addProperty(LEVEL, s.getLevel());
        json.addProperty(MCC, s.getMcc());
        json.addProperty(MNC, s.getMnc());
        return json;
    }
    /**
     * @param json
     * @return
     */
    private StationSignal parseStationSignal(final JsonObject json) {
        final StationSignal s = new StationSignal();
        s.setCi(json.get(CI).getAsInt());
        s.setLac(json.get(LAC).getAsInt());
        s.setLevel(json.get(LEVEL).getAsInt());
        s.setMcc(json.get(MCC).getAsInt());
        s.setMnc(json.get(MNC).getAsInt());
        return s;
    }
    /**
     * @param str
     * @return
     */
    private Date parseDate(final String str) {
        try {
            return createDateFormat().parse(str);
        } catch (final ParseException e) {
            throw new RuntimeException("Failed to parse date", e);
        }
    }
    /**
     * @return date format.
     */
    private SimpleDateFormat createDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    }
}
