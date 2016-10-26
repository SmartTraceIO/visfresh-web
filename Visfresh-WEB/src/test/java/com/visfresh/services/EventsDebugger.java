/**
 *
 */
package com.visfresh.services;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.visfresh.entities.Location;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.reports.ShortTrackerEventsImporter;
import com.visfresh.reports.geomap.AbstractGeoMapBuiler;
import com.visfresh.reports.geomap.GoogleGeoMapBuiler;
import com.visfresh.reports.geomap.OpenStreetMapBuilder;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@SuppressWarnings("serial")
public class EventsDebugger extends JFrame {
    private final List<ShortTrackerEvent> readings;

    private AbstractGeoMapBuiler builder = new GoogleGeoMapBuiler();
    private final int zoom;
    private Point mapLocation;
    private BufferedImage map;
    private int position;
    private volatile int sleepTimeOut = 51;
    private volatile Thread debugThread;
    private GeneralPath path = new GeneralPath();

    private final JPanel viewPanel;
    /**
     * Default constructor.
     */
    public EventsDebugger(final List<ShortTrackerEvent> readings) {
        super("Event Optimizer debugger");
        this.readings = readings;

        final Dimension viewArea = new Dimension(250, 250);

        final List<Location> coords = getCoordinates(readings);
        zoom = builder.calculateZoom(coords, viewArea, 10);

        final Rectangle r = builder.getMapBounds(coords, zoom);
        this.mapLocation = new Point(
                (int) (r.getX() - (viewArea.getWidth() - r.getWidth()) / 2.),
                (int) (r.getY() - (viewArea.getHeight() - r.getHeight()) / 2.));

        final int width = (int) Math.floor(viewArea.getWidth());
        final int height = (int) Math.floor(viewArea.getHeight());

        //create picture.
        this.map = createMap(width, height);

        //create UI.
        final JPanel cp = new JPanel(new BorderLayout(5, 5));
        setContentPane(cp);

        //create tool bar.
        final JToolBar tb = new JToolBar();
        tb.add(createResetAction());
        tb.add(createRunAction());
        tb.add(createDebugAction());

        cp.add(tb, BorderLayout.NORTH);

        //view panel
        viewPanel = new JPanel() {
            /* (non-Javadoc)
             * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
             */
            @Override
            protected void paintComponent(final Graphics g) {
                paintMap((Graphics2D) g);
            }
        };
        viewPanel.setBackground(Color.WHITE);
        cp.setPreferredSize(new Dimension(map.getWidth(), map.getHeight()));
        cp.add(viewPanel, BorderLayout.CENTER);
    }

    /**
     * @return
     */
    private Action createDebugAction() {
        return new AbstractAction("Debug") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                doDebug();
            }
        };
    }
    /**
     * @return
     */
    private Action createRunAction() {
        return new AbstractAction("Run") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (debugThread == null) {
                    debugThread = new Thread() {
                        @Override
                        public void run() {
                            while (debugThread != null) {
                                if (position < readings.size() - 1) {
                                    try {
                                        Thread.sleep(sleepTimeOut);
                                    } catch (final InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    SwingUtilities.invokeLater(new Runnable() {
                                        /* (non-Javadoc)
                                         * @see java.lang.Runnable#run()
                                         */
                                        @Override
                                        public void run() {
                                            doDebug();
                                        }
                                    });
                                } else {
                                    break;
                                }
                            }

                            debugThread = null;
                        }
                    };
                    debugThread.start();
                } else {
                    debugThread = null;
                }
            }
        };
    }

    /**
     *
     */
    protected void doDebug() {
        if (setPosition(position + 1)) {
            viewPanel.repaint();
        }
    }

    /**
     * @param pos
     * @return
     */
    private boolean setPosition(final int pos) {
        if (pos < readings.size() && pos != position) {
            //create path
            if (pos == -1) {
                path = null;
            } else {
                int i= 0;
                final GeneralPath p = new GeneralPath();

                //create path shape
                for (final ShortTrackerEvent e : readings) {
                    if (i >= pos) {
                        break;
                    }
                    if (e.getLatitude() != null && e.getLongitude() != null) {
                        final int x = Math.round(OpenStreetMapBuilder.lon2position(
                                e.getLongitude(), zoom) - mapLocation.x);
                        final int y = Math.round(OpenStreetMapBuilder.lat2position(
                                e.getLatitude(), zoom) - mapLocation.y);
                        final Point2D cp = p.getCurrentPoint();
                        if (cp == null) {
                            p.moveTo(x, y);
                        } else if (Math.round(cp.getX()) != x || Math.round(cp.getY()) != y) {
                            p.lineTo(x, y);
                        }
                    }

                    i++;
                }
                this.path = p;

                final ShortTrackerEvent e = readings.get(pos);
                if (e.getLatitude() == null || e.getLongitude() == null) {
                    System.out.println("Null coordinates will not read. Size to paint: " + position);
                }
            }

            position = pos;
            return true;
        }
        return false;
    }

    /**
     * @return
     */
    private Action createResetAction() {
        return new AbstractAction("Reset") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                setPosition(-1);
                viewPanel.repaint();
            }
        };
    }

    /**
     * @param g
     */
    protected void paintMap(final Graphics2D g) {
        g.drawImage(map, 0, 0, null);

        final GeneralPath p = path;
        if (p != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(2.f));
            g.setColor(Color.RED);
            g.draw(p);
        }
    }

    /**
     * @return
     */
    private BufferedImage createMap(final int width, final int height) {
        //use image buffer for avoid of problems with alpha chanel.
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = image.createGraphics();

        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

            //set transparency before draw map
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.8f));

            builder.paint(g, mapLocation, zoom, width, height);
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            g.dispose();
        }
        return image;
    }

    /**
     * @param readings
     * @return
     */
    private List<Location> getCoordinates(final List<ShortTrackerEvent> readings) {
        final List<Location> coords = new LinkedList<>();
        for (final ShortTrackerEvent e : readings) {
            if (e.getLatitude() != null && e.getLongitude() != null) {
                coords.add(new Location(e.getLatitude(), e.getLongitude()));
            }
        }
        return coords;
    }

    public static void main(final String[] args) throws Exception {
        final JFrame f = new EventsDebugger(importReadings(new File(args[0])));
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();

        f.setLocationByPlatform(true);
        f.setVisible(true);
    }

    /**
     * @param file
     * @return
     * @throws ParseException
     * @throws IOException
     */
    private static List<ShortTrackerEvent> importReadings(final File file) throws IOException, ParseException {
        final Reader r = new FileReader(file);
        try {
            return new ShortTrackerEventsImporter(7l).importEvents(r);
        } finally {
            r.close();
        }
    }
}
