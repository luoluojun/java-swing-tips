// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public final class MainPanel extends JPanel {
  private static final String LINESEPARATOR = "\n";
  private static final int LIMIT = 1000;
  private final JTextPane jtp = new JTextPane();
  private final JButton startButton = new JButton("Start");
  private final JButton stopButton = new JButton("Stop");
  private final JButton clearButton = new JButton("Clear");
  private final Timer timer;

  private MainPanel() {
    super(new BorderLayout());
    jtp.setEditable(false);
    stopButton.setEnabled(false);

    timer = new Timer(200, e -> append(LocalDateTime.now(ZoneId.systemDefault()).toString()));
    startButton.addActionListener(e -> timerStart());
    stopButton.addActionListener(e -> timerStop());
    clearButton.addActionListener(e -> {
      jtp.setText("");
      if (!timer.isRunning()) {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
      }
    });
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(Box.createHorizontalGlue());
    box.add(startButton);
    box.add(stopButton);
    box.add(Box.createHorizontalStrut(5));
    box.add(clearButton);

    add(new JScrollPane(jtp));
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private void timerStop() {
    timer.stop();
    startButton.setEnabled(true);
    stopButton.setEnabled(false);
  }

  private void timerStart() {
    startButton.setEnabled(false);
    stopButton.setEnabled(true);
    timer.start();
  }

  private void append(String str) {
    Document doc = jtp.getDocument();
    String text;
    if (doc.getLength() > LIMIT) {
      timerStop();
      startButton.setEnabled(false);
      text = "doc.getLength()>1000";
    } else {
      text = str;
    }
    try {
      doc.insertString(doc.getLength(), text + LINESEPARATOR, null);
      jtp.setCaretPosition(doc.getLength());
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
  }

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
