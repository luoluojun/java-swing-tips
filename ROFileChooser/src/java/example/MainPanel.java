// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTextArea log = new JTextArea();

    JButton readOnlyButton = new JButton("readOnly");
    readOnlyButton.addActionListener(e -> {
      UIManager.put("FileChooser.readOnly", Boolean.TRUE);
      JFileChooser fileChooser = new JFileChooser();
      int retvalue = fileChooser.showOpenDialog(getRootPane());
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton defaultButton = new JButton("Default");
    defaultButton.addActionListener(e -> {
      UIManager.put("FileChooser.readOnly", Boolean.FALSE);
      JFileChooser fileChooser = new JFileChooser();
      int retvalue = fileChooser.showOpenDialog(getRootPane());
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    p.add(readOnlyButton);
    p.add(defaultButton);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
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
      // UIManager.put("FileChooser.readOnly", Boolean.TRUE);
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
