/*******************************************************************************
 * Copyright (c) 2008, 2012 Stepan Rutz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stepan Rutz - initial implementation
 *******************************************************************************/

package com.visfresh.reports.geomap;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.StringReader;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.visfresh.entities.AlertType;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.reports.shipment.ReadingsHandler;
import com.visfresh.reports.shipment.ReadingsParser;
import com.visfresh.utils.StringUtils;


/**
 * MapPanel display tiles from openstreetmap as is. This simple minimal viewer supports zoom around mouse-click center and has a simple api.
 * A number of tiles are cached. See {@link #CACHE_SIZE} constant. If you use this it will create traffic on the tileserver you are
 * using. Please be conscious about this.
 *
 * This class is a JPanel which can be integrated into any swing app just by creating an instance and adding like a JLabel.
 *
 * The map has the size <code>256*1<<zoomlevel</code>. This measure is referred to as map-coordinates. Geometric locations
 * like longitude and latitude can be obtained by helper methods. Note that a point in map-coordinates corresponds to a given
 * geometric position but also depending on the current zoom level.
 *
 * You can zoomIn around current mouse position by left double click. Left right click zooms out.
 *
 * <p>
 * Methods of interest are
 * <ul>
 * <li>{@link #setZoom(int)} which sets the map's zoom level. Values between 1 and 18 are allowed.</li>
 * <li>{@link #setMapPosition(Point)} which sets the map's top left corner. (In map coordinates)</li>
 * <li>{@link #setCenterPosition(Point)} which sets the map's center position. (In map coordinates)</li>
 * <li>{@link #computePosition(java.awt.geom.Point2D.Double)} returns the position in the map panels coordinate system
 * for the given longitude and latitude. If you want to center the map around this geometric location you need
 * to pass the result to the method</li>
 * </ul>
 * </p>
 *
 * <p>As mentioned above Longitude/Latitude functionality is available via the method {@link #computePosition(java.awt.geom.Point2D.Double)}.
 * If you have a GIS database you can get this info out of it for a given town/location, invoke {@link #computePosition(java.awt.geom.Point2D.Double)} to
 * translate to a position for the given zoom level and center the view around this position using {@link #setCenterPosition(Point)}.
 * </p>
 *
 * <p>The properties <code>zoom</code> and <code>mapPosition</code> are bound and can be tracked via
 * regular {@link PropertyChangeListener}s.</p>
 *
 * <p>License is EPL (Eclipse Public License).  Contact at stepan.rutz@gmx.de</p>
 *
 * @author stepan.rutz
 * @version $Revision$
 */
@SuppressWarnings("all")
public class MapPanel extends JPanel {

    private static final Logger log = Logger.getLogger(MapPanel.class.getName());


    private static final int PREFERRED_WIDTH = 320;
    private static final int PREFERRED_HEIGHT = 200;


    private static final int ANIMATION_FPS = 15, ANIMATION_DURARTION_MS = 500;



    /* basically not be changed */
    private static final int TILE_SIZE = 256;

    //-------------------------------------------------------------------------
    // map impl.

    private Dimension mapSize = new Dimension(0, 0);
    private Point mapPosition = new Point(0, 0);
    private int zoom;

    private DragListener mouseListener = new DragListener();
    private OverlayPanel overlayPanel = new OverlayPanel();
    private ControlPanel controlPanel = new ControlPanel();

    private final AbstractGeoMapBuiler mapBuilder = new GoogleGeoMapBuiler();

    protected final List<ShortTrackerEvent> readings = new LinkedList<>();

    public MapPanel() {
        this(new Point(8282, 5179), 6);
    }

    public MapPanel(final Point mapPosition, final int zoom) {
        setLayout(new MapLayout());
        setOpaque(true);
        setBackground(new Color(0xc0, 0xc0, 0xc0));
        add(overlayPanel);
        add(controlPanel);
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        addMouseWheelListener(mouseListener);
        //add(slider);
        setZoom(Math.min(mapBuilder.getMaxZoom(), zoom));
        setMapPosition(mapPosition);
    }

    public OverlayPanel getOverlayPanel() {
        return overlayPanel;
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public Point getMapPosition() {
        return new Point(mapPosition.x, mapPosition.y);
    }

    public void setMapPosition(final Point mapPosition) {
        setMapPosition(mapPosition.x, mapPosition.y);
    }

    public void setMapPosition(final int x, final int y) {
        if (mapPosition.x == x && mapPosition.y == y)
            return;
        final Point oldMapPosition = getMapPosition();
        mapPosition.x = x;
        mapPosition.y = y;
        firePropertyChange("mapPosition", oldMapPosition, getMapPosition());
    }

    public void translateMapPosition(final int tx, final int ty) {
        setMapPosition(mapPosition.x + tx, mapPosition.y + ty);
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(final int zoom) {
        if (zoom == this.zoom)
            return;
        final int oldZoom = this.zoom;
        this.zoom = Math.min(mapBuilder.getMaxZoom(), zoom);
        mapSize.width = getXMax();
        mapSize.height = getYMax();
        firePropertyChange("zoom", oldZoom, zoom);
    }

    public void zoomIn(final Point pivot) {
        if (getZoom() >= mapBuilder.getMaxZoom())
            return;
        final Point mapPosition = getMapPosition();
        final int dx = pivot.x;
        final int dy = pivot.y;
        setZoom(getZoom() + 1);
        setMapPosition(mapPosition.x * 2 + dx, mapPosition.y * 2 + dy);
        repaint();
    }

    public void zoomOut(final Point pivot) {
        if (getZoom() <= 1)
            return;
        final Point mapPosition = getMapPosition();
        final int dx = pivot.x;
        final int dy = pivot.y;
        setZoom(getZoom() - 1);
        setMapPosition((mapPosition.x - dx) / 2, (mapPosition.y - dy) / 2);
        repaint();
    }

    public int getXTileCount() {
        return (1 << zoom);
    }

    public int getYTileCount() {
        return (1 << zoom);
    }

    public int getXMax() {
        return TILE_SIZE * getXTileCount();
    }

    public int getYMax() {
        return TILE_SIZE * getYTileCount();
    }

    public Point getCursorPosition() {
        return new Point(mapPosition.x + mouseListener.mouseCoords.x, mapPosition.y + mouseListener.mouseCoords.y);
    }

    public Point getTile(final Point position) {
        return new Point((int) Math.floor(((double) position.x) / TILE_SIZE),(int) Math.floor(((double) position.y) / TILE_SIZE));
    }

    public Point getCenterPosition() {
        return new Point(mapPosition.x + getWidth() / 2, mapPosition.y + getHeight() / 2);
    }

    public void setCenterPosition(final Point p) {
        setMapPosition(p.x - getWidth() / 2, p.y - getHeight() / 2);
    }

    public Point.Double getLongitudeLatitude(final Point position) {
        return new Point.Double(
                AbstractGeoMapBuiler.position2lon(position.x, getZoom()),
                AbstractGeoMapBuiler.position2lat(position.y, getZoom()));
    }

    public Point computePosition(final Point.Double coords) {
        final int x = AbstractGeoMapBuiler.lon2position(coords.x, getZoom());
        final int y = AbstractGeoMapBuiler.lat2position(coords.y, getZoom());
        return new Point(x, y);
    }

    @Override
    protected void paintComponent(final Graphics gOrig) {
        super.paintComponent(gOrig);
        final Graphics2D g = (Graphics2D) gOrig.create();
        try {
            paintInternal(g);
        } finally {
            g.dispose();
        }
    }

    private void paintInternal(final Graphics2D g) {
        final Point position = getMapPosition();

        try {
            mapBuilder.paint(g, position, getZoom(), getWidth(), getHeight());
        } catch (final IOException e) {
            e.printStackTrace();
        }
        paintReadings(g, position, getZoom(), this.readings);
    }

    /**
     * @param rim
     * @param bean
     * @param size
     * @return
     */
    private void paintReadings(final Graphics2D g, final Point loc, final int zoom,
            final List<ShortTrackerEvent> readings) {
        //create path shape
        final GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

        Double lat = null;
        Double lon = null;
        for (final ShortTrackerEvent p : readings) {
            if (p.getLatitude() != null && p.getLongitude() != null) {
                lat = p.getLatitude();
                lon = p.getLongitude();
            } else {
                System.out.println("Null location, previous coordinates are used");
            }

            if (lat != null && lon != null) {
                final int x = Math.round(OpenStreetMapBuilder.lon2position(lon, zoom) - loc.x);
                final int y = Math.round(OpenStreetMapBuilder.lat2position(lat, zoom) - loc.y);
                final Point2D cp = path.getCurrentPoint();
                if (cp == null) {
                    path.moveTo(x, y);
                } else if (Math.round(cp.getX()) != x || Math.round(cp.getY()) != y) {
                    path.lineTo(x, y);
                }
            } else {
                System.out.println("Have not available coorinages will not painted");
            }
        }

        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setStroke(new BasicStroke(4.f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.RED);
        g.draw(path);
    }

   //-------------------------------------------------------------------------
    // utils
    public static String format(final double d) {
        return String.format("%.5f", d);
    }

    public static double getN(final int y, final int z) {
        final double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return n;
    }

    public static double tile2lon(final int x, final int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    public static double tile2lat(final int y, final int z) {
        return Math.toDegrees(Math.atan(Math.sinh(Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z))));
    }

    private static void drawBackground(final Graphics2D g, final int width, final int height) {
        Color color1 = Color.black;
        Color color2 = new Color(0x30, 0x30, 0x30);
        color1 = new Color(0xc0, 0xc0, 0xc0);
        color2 = new Color(0xe0, 0xe0, 0xe0);
        final Composite oldComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.75f));
        g.setPaint(new GradientPaint(0, 0, color1, 0, height, color2));
        g.fillRoundRect(0, 0, width, height, 4, 4);
        g.setComposite(oldComposite);
    }

    private static void drawRollover(final Graphics2D g, final int width, final int height) {
        final Color color1 = Color.white;
        final Color color2 = new Color(0xc0, 0xc0, 0xc0);
        final Composite oldComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.25f));
        g.setPaint(new GradientPaint(0, 0, color1, width, height, color2));
        g.fillRoundRect(0, 0, width, height, 4, 4);
        g.setComposite(oldComposite);
    }

    private static BufferedImage flip(final BufferedImage image, final boolean horizontal, final boolean vertical) {
        final int width = image.getWidth(), height = image.getHeight();
        if (horizontal) {
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width / 2; ++x) {
                    final int tmp = image.getRGB(x, y);
                    image.setRGB(x, y, image.getRGB(width - 1 - x, y));
                    image.setRGB(width - 1 - x, y, tmp);
                }
            }
        }
        if (vertical) {
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height / 2; ++y) {
                    final int tmp = image.getRGB(x, y);
                    image.setRGB(x, y, image.getRGB(x, height - 1 - y));
                    image.setRGB(x, height - 1 - y, tmp);
                }
            }
        }
        return image;
    }

    private static BufferedImage makeIcon(final Color background) {
        final int WIDTH = 16, HEIGHT = 16;
        final BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < HEIGHT; ++y)
            for (int x = 0; x < WIDTH; ++x)
                image.setRGB(x, y, 0);
        final Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(background);
        g2d.fillOval(0, 0, WIDTH - 1, HEIGHT - 1);

        final double hx = 4;
        final double hy = 4;
        for (int y = 0; y < HEIGHT; ++y) {
            for (int x = 0; x < WIDTH; ++x) {
              final double dx = x - hx;
              final double dy = y - hy;
              double dist = Math.sqrt(dx * dx + dy * dy);
              if (dist > WIDTH) {
                 dist = WIDTH;
              }
              final int color = image.getRGB(x, y);
              final int a = (color >>> 24) & 0xff;
              final int r = (color >>> 16) & 0xff;
              final int g = (color >>> 8) & 0xff;
              final int b = (color >>> 0) & 0xff;
              final double coef = 0.7 - 0.7 * dist / WIDTH;
              image.setRGB(x, y, (a << 24) | ((int) (r + coef * (255 - r)) << 16) | ((int) (g + coef * (255 - g)) << 8) | (int) (b + coef * (255 - b)));
           }
        }
        g2d.setColor(Color.gray);
        g2d.drawOval(0, 0, WIDTH - 1, HEIGHT - 1);
        return image;
    }

    private static BufferedImage makeXArrow(final Color background) {
        final BufferedImage image = makeIcon(background);
        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillPolygon(new int[] { 10, 4, 10} , new int[] { 5, 8, 11 }, 3);
        image.flush();
        return image;

    }
    private static BufferedImage makeYArrow(final Color background) {
        final BufferedImage image = makeIcon(background);
        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillPolygon(new int[] { 5, 8, 11} , new int[] { 10, 4, 10 }, 3);
        image.flush();
        return image;
    }
    private static BufferedImage makePlus(final Color background) {
        final BufferedImage image = makeIcon(background);
        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRect(4, 7, 8, 2);
        g.fillRect(7, 4, 2, 8);
        image.flush();
        return image;
    }
    private static BufferedImage makeMinus(final Color background) {
        final BufferedImage image = makeIcon(background);
        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRect(4, 7, 8, 2);
        image.flush();
        return image;
    }

    private class DragListener extends MouseAdapter implements MouseMotionListener, MouseWheelListener {
        private Point mouseCoords;
        private Point downCoords;
        private Point downPosition;

        public DragListener() {
            mouseCoords = new Point();
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
                zoomIn(new Point(mouseCoords.x, mouseCoords.y));
            } else if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() >= 2) {
                zoomOut(new Point(mouseCoords.x, mouseCoords.y));
            } else if (e.getButton() == MouseEvent.BUTTON2) {
                setCenterPosition(getCursorPosition());
                repaint();
            }
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                downCoords = e.getPoint();
                downPosition = getMapPosition();
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                final int cx = getCursorPosition().x;
                final int cy = getCursorPosition().y;
                repaint();
            }
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            //setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            handleDrag(e);
            downCoords = null;
            downPosition = null;
        }

        @Override
        public void mouseMoved(final MouseEvent e) {
            handlePosition(e);
        }

        @Override
        public void mouseDragged(final MouseEvent e) {
            //setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            handlePosition(e);
            handleDrag(e);
        }

        @Override
        public void mouseExited(final MouseEvent e) {
            //setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        @Override
        public void mouseEntered(final MouseEvent me) {
            super.mouseEntered(me);
        }

        private void handlePosition(final MouseEvent e) {
            mouseCoords = e.getPoint();
            if (overlayPanel.isVisible())
                MapPanel.this.repaint();
        }

        private void handleDrag(final MouseEvent e) {
            if (downCoords != null) {
                final int tx = downCoords.x - e.getX();
                final int ty = downCoords.y - e.getY();
                setMapPosition(downPosition.x + tx, downPosition.y + ty);
                repaint();
            }
        }

        @Override
        public void mouseWheelMoved(final MouseWheelEvent e) {
            final int rotation = e.getWheelRotation();
            if (rotation < 0)
                zoomIn(new Point(mouseCoords.x, mouseCoords.y));
            else
                zoomOut(new Point(mouseCoords.x, mouseCoords.y));
        }
    }

    public final class OverlayPanel extends JPanel {

        private OverlayPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(370, 12 * 16 + 12));
        }

        @Override
        protected void paintComponent(final Graphics gOrig) {
            super.paintComponent(gOrig);
            final Graphics2D g = (Graphics2D) gOrig.create();
            try {
                paintOverlay(g);
            } finally {
                g.dispose();
            }
        }

        private void paintOverlay(final Graphics2D g) {
            drawBackground(g, getWidth(), getHeight());
            g.setColor(Color.black);
            drawString(g, 0, "Zoom", Integer.toString(getZoom()));
            drawString(g, 1, "MapSize", mapSize.width + ", " + mapSize.height);
            drawString(g, 2, "MapPosition", mapPosition.x + ", " + mapPosition.y);
            drawString(g, 3, "CursorPosition", (mapPosition.x + getCursorPosition().x) + ", " + (mapPosition.y + getCursorPosition().y));
            drawString(g, 4, "CenterPosition", (mapPosition.x + getWidth() / 2) + ", " + (mapPosition.y + getHeight() / 2));
            drawString(g, 5, "Tilescount", getXTileCount() + ", " + getYTileCount() + " (" + (NumberFormat.getIntegerInstance().format((long)getXTileCount() * getYTileCount())) + " total)");
            drawString(g, 6, "Active Tile", getTile(getCursorPosition()).x + ", " + getTile(getCursorPosition()).y);
            drawString(g, 7, "Tile Box Lon/Lat", format(tile2lon(getTile(getCursorPosition()).x, getZoom())) + ", " + format(tile2lat(getTile(getCursorPosition()).y, getZoom())));
            drawString(g, 8, "Cursor Lon/Lat", format(
                    AbstractGeoMapBuiler.position2lon(getCursorPosition().x, getZoom())) + ", " + format(
                            AbstractGeoMapBuiler.position2lat(getCursorPosition().y, getZoom())));
        }

        private void drawString(final Graphics2D g, final int row, final String key, final String value) {
            final int y = 16 + row * 16;
            g.drawString(key, 20, y);
            g.drawString(value, 150, y);
        }
    }

    public final class ControlPanel extends JPanel {

        protected static final int MOVE_STEP = 32;

        private JButton makeButton(final Action action) {
            final JButton b = new JButton(action);
            b.setFocusable(false);
            b.setText(null);
            b.setContentAreaFilled(false);
            b.setBorder(BorderFactory.createEmptyBorder());
            final BufferedImage image = (BufferedImage) ((ImageIcon)b.getIcon()).getImage();
            final BufferedImage hl = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = (Graphics2D) hl.getGraphics();
            g.drawImage(image, 0, 0, null);
            drawRollover(g, hl.getWidth(), hl.getHeight());
            hl.flush();
            b.setRolloverIcon(new ImageIcon(hl));
            return b;
        }

        public ControlPanel() {
            setOpaque(false);
            setForeground(Color.white);
            setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            setLayout(new BorderLayout());

            final Action zoomInAction = new AbstractAction() {
                {
                    final String text = "Zoom In";
                    putValue(Action.NAME, text);
                    putValue(Action.SHORT_DESCRIPTION, text);
                    putValue(Action.SMALL_ICON, new ImageIcon(flip(makePlus(new Color(0xc0, 0xc0, 0xc0)), false, false)));
                }

                @Override
                public void actionPerformed(final ActionEvent e) {
                    zoomIn(new Point(MapPanel.this.getWidth() / 2, MapPanel.this.getHeight() / 2));
                }
            };
            final Action zoomOutAction = new AbstractAction() {
                {
                    final String text = "Zoom Out";
                    putValue(Action.NAME, text);
                    putValue(Action.SHORT_DESCRIPTION, text);
                    putValue(Action.SMALL_ICON, new ImageIcon(flip(makeMinus(new Color(0xc0, 0xc0, 0xc0)), false, false)));
                }

                @Override
                public void actionPerformed(final ActionEvent e) {
                    zoomOut(new Point(MapPanel.this.getWidth() / 2, MapPanel.this.getHeight() / 2));
                }
            };

            final Action upAction = new AbstractAction() {
                {
                    final String text = "Up";
                    putValue(Action.NAME, text);
                    putValue(Action.SHORT_DESCRIPTION, text);
                    putValue(Action.SMALL_ICON, new ImageIcon(flip(makeYArrow(new Color(0xc0, 0xc0, 0xc0)), false, false)));
                }

                @Override
                public void actionPerformed(final ActionEvent e) {
                    translateMapPosition(0, -MOVE_STEP);
                    MapPanel.this.repaint();
                }
            };
            final Action downAction = new AbstractAction() {
                {
                    final String text = "Down";
                    putValue(Action.NAME, text);
                    putValue(Action.SHORT_DESCRIPTION, text);
                    putValue(Action.SMALL_ICON, new ImageIcon(flip(makeYArrow(new Color(0xc0, 0xc0, 0xc0)), false, true)));
                }

                @Override
                public void actionPerformed(final ActionEvent e) {
                    translateMapPosition(0, +MOVE_STEP);
                    MapPanel.this.repaint();
                }
            };
            final Action leftAction = new AbstractAction() {
                {
                    final String text = "Left";
                    putValue(Action.NAME, text);
                    putValue(Action.SHORT_DESCRIPTION, text);
                    putValue(Action.SMALL_ICON, new ImageIcon(flip(makeXArrow(new Color(0xc0, 0xc0, 0xc0)), false, false)));
                }

                @Override
                public void actionPerformed(final ActionEvent e) {
                    translateMapPosition(-MOVE_STEP, 0);
                    MapPanel.this.repaint();
                }
            };
            final Action rightAction = new AbstractAction() {
                {
                    final String text = "Right";
                    putValue(Action.NAME, text);
                    putValue(Action.SHORT_DESCRIPTION, text);
                    putValue(Action.SMALL_ICON, new ImageIcon(flip(makeXArrow(new Color(0xc0, 0xc0, 0xc0)), true, false)));
                }

                @Override
                public void actionPerformed(final ActionEvent e) {
                    translateMapPosition(+MOVE_STEP, 0);
                    MapPanel.this.repaint();
                }
            };
            final JPanel moves = new JPanel(new BorderLayout());
            moves.setOpaque(false);
            final JPanel zooms = new JPanel(new BorderLayout(0, 0));
            zooms.setOpaque(false);
            zooms.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
            moves.add(makeButton(upAction), BorderLayout.NORTH);
            moves.add(makeButton(leftAction), BorderLayout.WEST);
            moves.add(makeButton(downAction), BorderLayout.SOUTH);
            moves.add(makeButton(rightAction), BorderLayout.EAST);
            zooms.add(makeButton(zoomInAction), BorderLayout.NORTH);
            zooms.add(makeButton(zoomOutAction), BorderLayout.SOUTH);
            add(moves, BorderLayout.NORTH);
            add(zooms, BorderLayout.SOUTH);
        }
    }


    private final class MapLayout implements LayoutManager {

        @Override
        public void addLayoutComponent(final String name, final Component comp) {
        }
        @Override
        public void removeLayoutComponent(final Component comp) {
        }
        @Override
        public Dimension minimumLayoutSize(final Container parent) {
            return new Dimension(1, 1);
        }
        @Override
        public Dimension preferredLayoutSize(final Container parent) {
            return new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        }
        @Override
        public void layoutContainer(final Container parent) {
            final int width = parent.getWidth();
            {
                final Dimension psize = overlayPanel.getPreferredSize();
                overlayPanel.setBounds(width - psize.width - 20, 20, psize.width, psize.height);
            }
            {
                final Dimension psize = controlPanel.getPreferredSize();
                controlPanel.setBounds(20, 20, psize.width, psize.height);
            }
        }
    }

    public static final class Gui extends JPanel {

        private final MapPanel mapPanel;

        public Gui() {
            this(new MapPanel());
        }

        public Gui(final MapPanel mapPanel) {
            super(new BorderLayout());
            this.mapPanel = mapPanel;
            mapPanel.getOverlayPanel().setVisible(false);
            mapPanel.setMinimumSize(new Dimension(1, 1));
            add(mapPanel, BorderLayout.CENTER);
        }

        public MapPanel getMapPanel() {
            return mapPanel;
        }

        public JMenuBar createMenuBar() {
            JFrame frame = null;
            if (SwingUtilities.getWindowAncestor(mapPanel) instanceof JFrame)
                frame = (JFrame) SwingUtilities.getWindowAncestor(mapPanel);
            final JFrame frame_ = frame;
            final JMenuBar menuBar = new JMenuBar();
            {
                final JMenu fileMenu = new JMenu("File");
                fileMenu.setMnemonic(KeyEvent.VK_F);
                fileMenu.add(new AbstractAction() {
                    {
                        putValue(Action.NAME, "Exit");
                        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
                        setEnabled(frame_ != null);
                    }
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        if (frame_ != null)
                            frame_.dispose();
                    }
                });
                menuBar.add(fileMenu);
            }
            {
                final JMenu viewMenu = new JMenu("View");
                viewMenu.setMnemonic(KeyEvent.VK_V);
                viewMenu.add(new JCheckBoxMenuItem(new AbstractAction() {
                    JFrame floatFrame;
                    Container oldParent;
                    {
                        putValue(Action.NAME, "Float In a Frame");
                        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
                        setEnabled(frame_ == null);
                    }
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        if (floatFrame == null) {
                            floatFrame = new JFrame("Floating MapPanel");
                            floatFrame.setBounds(100, 100, 800, 600);
                            floatFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                            floatFrame.addWindowListener(new WindowAdapter() {
                                @Override
                                public void windowClosing(final WindowEvent e) {
                                    unfloat();
                                }
                            });
                        }
                        if (!floatFrame.isVisible()) {
                            oldParent = Gui.this.getParent();
                            floatFrame.getContentPane().add(Gui.this);
                            oldParent.validate();
                            oldParent.repaint();
                            floatFrame.validate();
                            floatFrame.setVisible(true);
                        } else if (floatFrame.isVisible()) {
                            unfloat();
                        }
                    }
                    private void unfloat() {
                        floatFrame.setVisible(false);
                        oldParent.add(Gui.this);
                        oldParent.validate();
                        oldParent.repaint();
                    }
                }));
                viewMenu.add(new JCheckBoxMenuItem(new AbstractAction() {
                    {
                        putValue(Action.NAME, "Show Infopanel");
                        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
                    }
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        mapPanel.getOverlayPanel().setVisible(!mapPanel.getOverlayPanel().isVisible());
                    }

                }));
                final JCheckBoxMenuItem controlPanelMenuItem = new JCheckBoxMenuItem(new AbstractAction() {
                    {
                        putValue(Action.NAME, "Show Controlpanel");
                        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
                    }
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        mapPanel.getControlPanel().setVisible(!mapPanel.getControlPanel().isVisible());
                    }
                });
                controlPanelMenuItem.setSelected(true);
                viewMenu.add(controlPanelMenuItem);
                menuBar.add(viewMenu);
            }
            {
                final JMenu readings = new JMenu("Readings");
                menuBar.add(readings);

                //add load readings menu item
                final JMenuItem load = new JMenuItem("Load", KeyEvent.VK_L);
                load.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        loadReadings();
                    }
                });
                readings.add(load);

                //add load readings menu item
                final JMenuItem clear = new JMenuItem("Clear");
                clear.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        getMapPanel().readings.clear();
                    }
                });
                readings.add(clear);
            }
            return menuBar;
        }

        /**
         *
         */
        protected void loadReadings() {
            final JFileChooser chooser = new JFileChooser();
            final int result = chooser.showOpenDialog(getMapPanel());

            if (result == JFileChooser.APPROVE_OPTION) {
                final List<ShortTrackerEvent> events = new LinkedList<>();

                try {
                    final String readings = StringUtils.getContent(chooser.getSelectedFile().toURI().toURL(), "UTF-8");
                    final ReadingsParser p = new ReadingsParser();
                    p.setHandler(new ReadingsHandler() {
                        @Override
                        public void handleEvent(final ShortTrackerEvent e, final AlertType[] alerts) {
                            e.setDeviceImei("91028437098273");
                            events.add(e);
                        }

                        @Override
                        public Long getShipmentId(final String sn, final int tripCount) {
                            return 1l;
                        }
                    });

                    p.parse(new StringReader(readings));
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                getMapPanel().readings.clear();
                getMapPanel().readings.addAll(events);
            }

        }

        private boolean isWebstart() {
            return System.getProperty("javawebstart.version") != null && System.getProperty("javawebstart.version").length() > 0;
        }

    }

    public static MapPanel createMapPanel(final Point mapPosition, final int zoom) {
        final MapPanel mapPanel = new MapPanel(mapPosition, zoom);
        mapPanel.getOverlayPanel().setVisible(false);
        ((JComponent)mapPanel.getControlPanel()).setVisible(false);
        return mapPanel;
    }

    public static Gui createGui(final Point mapPosition, final int zoom) {
        final MapPanel mapPanel = createMapPanel(mapPosition, zoom);
        return new MapPanel.Gui(mapPanel);
    }

    public static void launchUI() {

        final JFrame frame = new JFrame();
        frame.setTitle("Map Panel");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        final Dimension sz = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(800, 600);
        frame.setLocation((sz.width - frame.getWidth()) / 2, (sz.height - frame.getHeight())/2);

        final Gui gui = new Gui();
        frame.getContentPane().add(gui, BorderLayout.CENTER);

        final JMenuBar menuBar = gui.createMenuBar();
        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (final Exception e) {
                    // ignore
                }
                launchUI();
            }
        });
    }
}
