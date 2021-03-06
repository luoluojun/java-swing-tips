// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    String[] columnNames = {"Name", "Comment"};
    Object[][] data = {
      {"test1.jpg", "adfasd"},
      {"test1234.jpg", "  "},
      {"test15354.gif", "fasdf"},
      {"t.png", "comment"},
      {"tfasdfasd.jpg", "123"},
      {"afsdfasdfffffffffffasdfasdf.mpg", "test"},
      {"fffffffffffasdfasdf", ""},
      {"test1.jpg", ""}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }

      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    JTable table = new FileListTable(model);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }
  // private static int getStringWidth(JTable table, int row, int column) {
  //   FontMetrics fm = table.getFontMetrics(table.getFont());
  //   Object o = table.getValueAt(row, column);
  //   return fm.stringWidth(o.toString()) + ICON_SIZE + 2 + 2;
  // }
  // private static boolean isOnLabel(JTable table, Point pt, int row, int col) {
  //   Rectangle rect = table.getCellRect(row, col, true);
  //   rect.setSize(getStringWidth(table, row, col), rect.height);
  //   return(rect.contains(pt));
  // }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class SelectedImageFilter extends RGBImageFilter {
  // public SelectedImageFilter() {
  //   canFilterIndexColorModel = false;
  // }

  @Override public int filterRGB(int x, int y, int argb) {
    int r = (argb >> 16) & 0xFF;
    int g = (argb >> 8) & 0xFF;
    return (argb & 0xFF_00_00_FF) | ((r >> 1) << 16) | ((g >> 1) << 8);
    // return (argb & 0xFF_FF_FF_00) | ((argb & 0xFF) >> 1);
  }
}

class FileNameRenderer implements TableCellRenderer {
  protected final Dimension dim = new Dimension();
  private final JPanel renderer = new JPanel(new BorderLayout());
  private final JLabel textLabel = new JLabel(" ");
  private final JLabel iconLabel;
  private final Border focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
  private final Border noFocusBorder;
  private final ImageIcon nicon;
  private final ImageIcon sicon;

  protected FileNameRenderer(JTable table) {
    Border b = UIManager.getBorder("Table.noFocusBorder");
    if (Objects.isNull(b)) { // Nimbus???
      Insets i = focusBorder.getBorderInsets(textLabel);
      b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
    }
    noFocusBorder = b;

    JPanel p = new JPanel(new BorderLayout()) {
      @Override public Dimension getPreferredSize() {
        return dim;
      }
    };
    p.setOpaque(false);
    renderer.setOpaque(false);

    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    nicon = new ImageIcon(getClass().getResource("wi0063-16.png"));

    ImageProducer ip = new FilteredImageSource(nicon.getImage().getSource(), new SelectedImageFilter());
    sicon = new ImageIcon(p.createImage(ip));

    iconLabel = new JLabel(nicon);
    iconLabel.setBorder(BorderFactory.createEmptyBorder());

    p.add(iconLabel, BorderLayout.WEST);
    p.add(textLabel);
    renderer.add(p, BorderLayout.WEST);

    Dimension d = iconLabel.getPreferredSize();
    dim.setSize(d);
    table.setRowHeight(d.height);
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    textLabel.setFont(table.getFont());
    textLabel.setText(Objects.toString(value, ""));
    textLabel.setBorder(hasFocus ? focusBorder : noFocusBorder);

    FontMetrics fm = table.getFontMetrics(table.getFont());
    Insets i = textLabel.getInsets();
    int swidth = iconLabel.getPreferredSize().width + fm.stringWidth(textLabel.getText()) + i.left + i.right;
    int cwidth = table.getColumnModel().getColumn(column).getWidth();
    dim.width = Math.min(swidth, cwidth);

    if (isSelected) {
      textLabel.setOpaque(true);
      textLabel.setForeground(table.getSelectionForeground());
      textLabel.setBackground(table.getSelectionBackground());
      iconLabel.setIcon(sicon);
    } else {
      textLabel.setOpaque(false);
      textLabel.setForeground(table.getForeground());
      textLabel.setBackground(table.getBackground());
      iconLabel.setIcon(nicon);
    }
    return renderer;
  }
}

class FileListTable extends JTable {
  private static final AlphaComposite ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1f);
  private final Color rcolor = SystemColor.activeCaption;
  private final Color pcolor = makeColor(rcolor);
  private final Path2D rubberBand = new Path2D.Double();
  private transient RubberBandingListener rbl;

  protected FileListTable(TableModel model) {
    super(model);
  }

  @Override public void updateUI() {
    // [JDK-6788475] Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely - Java Bug System
    // https://bugs.openjdk.java.net/browse/JDK-6788475
    // XXX: set dummy ColorUIResource
    setSelectionForeground(new ColorUIResource(Color.RED));
    setSelectionBackground(new ColorUIResource(Color.RED));
    removeMouseMotionListener(rbl);
    removeMouseListener(rbl);
    super.updateUI();
    rbl = new RubberBandingListener();
    addMouseMotionListener(rbl);
    addMouseListener(rbl);

    putClientProperty("Table.isFileList", Boolean.TRUE);
    setCellSelectionEnabled(true);
    setIntercellSpacing(new Dimension());
    setShowGrid(false);
    setAutoCreateRowSorter(true);
    setFillsViewportHeight(true);

    setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return super.getTableCellRendererComponent(table, value, false, false, row, column);
      }
    });

    TableColumn col = getColumnModel().getColumn(0);
    col.setCellRenderer(new FileNameRenderer(this));
    col.setPreferredWidth(200);
    col = getColumnModel().getColumn(1);
    col.setPreferredWidth(300);
  }

  @Override public String getToolTipText(MouseEvent e) {
    Point pt = e.getPoint();
    int row = rowAtPoint(pt);
    int col = columnAtPoint(pt);
    if (convertColumnIndexToModel(col) != 0 || row < 0 || row > getRowCount()) {
      return null;
    }
    Rectangle rect = getCellRect2(this, row, col);
    if (rect.contains(pt)) {
      return getValueAt(row, col).toString();
    }
    return null;
  }

  @Override public void setColumnSelectionInterval(int index0, int index1) {
    int idx = convertColumnIndexToView(0);
    super.setColumnSelectionInterval(idx, idx);
  }

  protected Path2D getRubberBand() {
    return rubberBand;
  }

  private class RubberBandingListener extends MouseAdapter {
    private final Point srcPoint = new Point();

    @Override public void mouseDragged(MouseEvent e) {
      Point destPoint = e.getPoint();
      Path2D rb = getRubberBand();
      rb.reset();
      rb.moveTo(srcPoint.x, srcPoint.y);
      rb.lineTo(destPoint.x, srcPoint.y);
      rb.lineTo(destPoint.x, destPoint.y);
      rb.lineTo(srcPoint.x, destPoint.y);
      rb.closePath();
      clearSelection();
      int col = convertColumnIndexToView(0);
      int[] indeces = IntStream.range(0, getModel().getRowCount())
        .filter(i -> rb.intersects(getCellRect2(FileListTable.this, i, col)))
        .toArray();
      for (int i: indeces) {
        addRowSelectionInterval(i, i);
        changeSelection(i, col, true, true);
      }
      repaint();
    }

    @Override public void mouseReleased(MouseEvent e) {
      getRubberBand().reset();
      repaint();
    }

    @Override public void mousePressed(MouseEvent e) {
      srcPoint.setLocation(e.getPoint());
      if (rowAtPoint(e.getPoint()) < 0) {
        clearSelection();
        repaint();
      } else {
        int index = rowAtPoint(e.getPoint());
        Rectangle rect = getCellRect2(FileListTable.this, index, convertColumnIndexToView(0));
        if (!rect.contains(e.getPoint())) {
          clearSelection();
          repaint();
        }
      }
    }
  }

  // SwingUtilities2.pointOutsidePrefSize(...)
  protected static Rectangle getCellRect2(JTable table, int row, int col) {
    TableCellRenderer tcr = table.getCellRenderer(row, col);
    Object value = table.getValueAt(row, col);
    Component cell = tcr.getTableCellRendererComponent(table, value, false, false, row, col);
    Dimension itemSize = cell.getPreferredSize();
    Rectangle cellBounds = table.getCellRect(row, col, false);
    cellBounds.width = itemSize.width;
    return cellBounds;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(rcolor);
    g2.draw(rubberBand);
    g2.setComposite(ALPHA);
    g2.setPaint(pcolor);
    g2.fill(rubberBand);
    g2.dispose();
  }
  // private int[] getIntersectedIndices(Path2D path) {
  //   TableModel model = getModel();
  //   List<Integer> list = new ArrayList<>(model.getRowCount());
  //   for (int i = 0; i < getRowCount(); i++) {
  //     if (path.intersects(getCellRect2(FileListTable.this, i, convertColumnIndexToView(0)))) {
  //       list.add(i);
  //     }
  //   }
  //   int[] il = new int[list.size()];
  //   for (int i = 0; i < list.size(); i++) {
  //     il[i] = list.get(i);
  //   }
  //   return il;
  // }

  private static Color makeColor(Color c) {
    int r = c.getRed();
    int g = c.getGreen();
    int b = c.getBlue();
    return r > g ? r > b ? new Color(r, 0, 0) : new Color(0, 0, b)
           : g > b ? new Color(0, g, 0) : new Color(0, 0, b);
  }
}
