package com.mflima.katscans.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public class HelpDiag extends JDialog {

  private static final class Action {
    private final String text;
    private final boolean code;

    private Action(String text, boolean code) {
      this.text = text;
      this.code = code;
    }

    public static Action plain(String text) {
      return new Action(text, false);
    }

    public static Action code(String text) {
      return new Action(text, true);
    }

    public JComponent toComponent() {
      if (code) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label =
            new JLabel(
                String.format("<html><code>%s</code></html>", text.toUpperCase(Locale.ROOT))) {
              @Override
              protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                var paint = g2d.getPaint();
                g2d.setPaint(new Color(0, 0, 0, 50));
                var roundness = Math.min(getWidth(), getHeight()) / 3;
                g2d.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, roundness, roundness);
                g2d.setPaint(paint);
                super.paintComponent(g);
              }
            };
        label.setBorder(new EmptyBorder(2, 8, 2, 8));
        panel.add(label, BorderLayout.WEST);
        return panel;
      } else {
        return new JLabel(text);
      }
    }
  }

  private static final class HelpEntry {
    public static final List<HelpEntry> ENTRIES =
        Arrays.stream(
                ("|Action                          |Component         |Description                        |\n"
                        + "|--------------------------------|------------------|-----------------------------------|\n"
                        + "|`CTRL` + `1`                    |General           |Open Datasets panel                |\n"
                        + "|`?`                             |General           |Show help dialog                   |\n"
                        + "|`Space`                         |Dataset tree      |Display selected node actions      |\n"
                        + "|`Right button`                  |Dataset tree      |Display selected node actions      |\n"
                        + "|Scroll                          |Camera            |Zoom                               |\n"
                        + "|`Middle button` + drag          |Camera            |Zoom                               |\n"
                        + "|`Left button` + drag            |Camera            |Rotate                             |\n"
                        + "|`Right button` + drag           |Camera            |Pan                                |\n"
                        + "|`ALT` + scroll                  |Camera            |Field of view                      |\n"
                        + "|`ALT` + drag                    |Camera            |Field of view                      |\n"
                        + "|`SHIFT` + drag                  |Camera            |Light position                     |\n"
                        + "|`SHIFT` + scroll                |Slice             |Slice through volume               |\n"
                        + "|`SHIFT` + `Middle button` + drag|Slice             |Slice through volume               |\n"
                        + "|`X` + `Left button` + drag      |Slice             |Upper slice cut on X axis          |\n"
                        + "|`X` + `Right button` + drag     |Slice             |Lower slice cut on X axis          |\n"
                        + "|`X` + `Middle button` + drag    |Slice             |Upper and lower slice cut on X axis|\n"
                        + "|`Y` + `Left button` + drag      |Slice             |Upper slice cut on Y axis          |\n"
                        + "|`Y` + `Right button` + drag     |Slice             |Lower slice cut on Y axis          |\n"
                        + "|`Y` + `Middle button` + drag    |Slice             |Upper and lower slice cut on Y axis|\n"
                        + "|`Z` + `Left button` + drag      |Slice             |Upper slice cut on Z axis          |\n"
                        + "|`Z` + `Right button` + drag     |Slice             |Lower slice cut on Z axis          |\n"
                        + "|`Z` + `Middle button` + drag    |Slice             |Upper and lower slice cut on Z axis|\n"
                        + "|`CTRL` + `Left button` + drag   |Surface renderer  |Upper cut-off threshold            |\n"
                        + "|`CTRL` + `Right button` + drag  |Surface renderer  |Lower cut-off threshold            |\n"
                        + "|`CTRL` + `Middle button` + drag |Surface renderer  |Upper and lower cut-off threshold  |\n"
                        + "|`ALT` + `Left button` + drag    |Composite renderer|Stride length                      |\n"
                        + "|`Left button` + drag right      |Transfer function |Zoom histogram                     |\n"
                        + "|`Left button` + drag left       |Transfer function |Reset histogram zoom               |\n"
                        + "|`Right button`                  |Transfer function |Transfer function node color picker|\n"
                        + "|`Middle button`                 |Transfer function |Delete transfer function node      |\n")
                    .split("\n"))
            .skip(2)
            .map(
                s -> {
                  var split = s.split("\\|");
                  return new HelpEntry(split[1].trim(), split[2].trim(), split[3].trim());
                })
            .collect(Collectors.toList());

    public final List<Action> action;
    public final String component;
    public final String description;

    private HelpEntry(String actions, String component, String description) {
      this(
          Arrays.stream(actions.split(" \\+ "))
              .map(String::trim)
              .map(
                  a -> {
                    if (a.startsWith("`")) {
                      return Action.code(a.substring(1, a.length() - 1));
                    } else {
                      return Action.plain(a);
                    }
                  })
              .collect(Collectors.toList()),
          component,
          description);
    }

    private HelpEntry(List<Action> action, String component, String description) {
      this.action = action;
      this.component = component;
      this.description = description;
    }
  }

  public HelpDiag(MainFrame frame) {
    super(frame);

    initComponents();
    setAlwaysOnTop(true);
    setLocationRelativeTo(frame);
  }

  private void initComponents() {
    JButton btnOk = new JButton();
    JPanel pnlHelp = new JPanel();
    JScrollPane scrHelp = new JScrollPane(pnlHelp);

    scrHelp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrHelp.setBorder(new EmptyBorder(0, 5, 0, 5));

    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

    btnOk.setMnemonic('O');
    btnOk.setText("Ok");
    btnOk.addActionListener(this::btnOkActionPerformed);

    pnlHelp.setLayout(new GridBagLayout());

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.LINE_START;

    constraints.weightx = 1;
    constraints.gridx = 0;
    constraints.gridwidth = 2;
    pnlHelp.add(new JLabel("<html><h2>Action</h2></html>"), constraints);
    constraints.gridx = 2;
    constraints.gridwidth = 1;
    pnlHelp.add(new JLabel("<html><h2>Component</h2></html>"), constraints);
    constraints.gridx = 3;
    pnlHelp.add(new JLabel("<html><h2>Description</h2></html>"), constraints);

    for (var entry : HelpEntry.ENTRIES) {
      JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      for (var iter = entry.action.iterator(); iter.hasNext(); ) {
        actionPanel.add(iter.next().toComponent());
        if (iter.hasNext()) {
          actionPanel.add(new JLabel("+"));
        }
      }
      constraints.insets = new Insets(0, 0, 0, 16);
      constraints.gridx = 0;
      constraints.gridwidth = 2;
      pnlHelp.add(actionPanel, constraints);
      constraints.gridx = 2;
      constraints.gridwidth = 1;
      pnlHelp.add(new JLabel(entry.component), constraints);
      constraints.insets = new Insets(0, 0, 0, 0);
      constraints.gridx = 3;
      pnlHelp.add(new JLabel(entry.description), constraints);
    }

    GroupLayout layout = new GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout
            .createSequentialGroup()
            .addContainerGap()
            .addGroup(
                layout
                    .createParallelGroup(Alignment.LEADING)
                    .addComponent(
                        scrHelp,
                        GroupLayout.DEFAULT_SIZE,
                        GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE)
                    .addGroup(
                        Alignment.TRAILING,
                        layout
                            .createSequentialGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(btnOk)))
            .addContainerGap());

    layout.setVerticalGroup(
        layout
            .createSequentialGroup()
            .addContainerGap()
            .addComponent(
                scrHelp, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btnOk)
            .addContainerGap());

    pack();
  }

  private void btnOkActionPerformed(ActionEvent actionEvent) {
    setVisible(false);
  }
}
