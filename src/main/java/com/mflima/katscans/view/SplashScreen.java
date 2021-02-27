/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mflima.katscans.view;

import com.mflima.katscans.view.component.ProgressBar;
import com.mflima.katscans.view.component.image.SplashImage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/** @author mflim_000 */
public class SplashScreen extends javax.swing.JDialog {

  /** Creates new form SplashScreen */
  public SplashScreen(JFrame owner, GraphicsConfiguration gc) {
    super(owner, "KATscans", true, gc);

    setResizable(false);
    setUndecorated(true);

    initComponents();

    setSize(pnlBackground.getWidth(), pnlBackground.getHeight());
    setLocationRelativeTo(null);

    new Thread(
            () -> {
              double value = 0d;
              while (value <= 1d) {
                value += (Math.random() / 8d);
                pgrProgress.setValue(value);

                try {
                  Thread.sleep(50);
                } catch (InterruptedException ex) {
                  Logger.getLogger(SplashScreen.class.getName()).log(Level.SEVERE, null, ex);
                }
              }

              float mix = 0f;
              while (mix < 1f) {
                mix += 0.1f;
                pnlBackground.setMix(mix);

                try {
                  Thread.sleep(50);
                } catch (InterruptedException ex) {
                  Logger.getLogger(SplashScreen.class.getName()).log(Level.SEVERE, null, ex);
                }
              }

              try {
                Thread.sleep(500);
              } catch (InterruptedException ex) {
                Logger.getLogger(SplashScreen.class.getName()).log(Level.SEVERE, null, ex);
              }

              SplashScreen.this.setVisible(false);
              SplashScreen.this.dispose();
            })
        .start();
  }

  private void initComponents() {
    JPanel pnlTitle = new JPanel();
    JButton btnClose = new JButton();

    pnlBackground = new SplashImage();
    pgrProgress = new com.mflima.katscans.view.component.ProgressBar();

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout(5, 5));

    pnlBackground.setLayout(new BorderLayout());

    pnlTitle.setOpaque(false);
    pnlTitle.setLayout(new BorderLayout());

    btnClose.setIcon(new ImageIcon(getClass().getResource("/icons/exit_bw.png")));
    btnClose.setBorder(null);
    btnClose.setBorderPainted(false);
    btnClose.setContentAreaFilled(false);
    btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnClose.setMaximumSize(new Dimension(26, 26));
    btnClose.setMinimumSize(new Dimension(26, 26));
    btnClose.setPreferredSize(new Dimension(26, 26));
    btnClose.setRolloverIcon(new ImageIcon(getClass().getResource("/icons/exit_over.png")));
    btnClose.addActionListener(this::btnCloseActionPerformed);
    pnlTitle.add(btnClose, BorderLayout.EAST);

    pnlBackground.add(pnlTitle, BorderLayout.PAGE_START);

    pgrProgress.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    pgrProgress.setForeground(new Color(180, 180, 180));
    pgrProgress.setHorizontalAlignment(SwingConstants.CENTER);
    pgrProgress.setGapThickness(2);
    pgrProgress.setPreferredSize(new Dimension(32, 26));
    pgrProgress.setProgressBorderColor(new Color(85, 127, 85));
    pgrProgress.setProgressColor(new Color(85, 127, 85));
    pgrProgress.setValue(0.7);
    pnlBackground.add(pgrProgress, BorderLayout.SOUTH);

    getContentPane().add(pnlBackground, BorderLayout.CENTER);

    pack();
  }

  private void btnCloseActionPerformed(ActionEvent evt) {
    System.exit(0);
  }

  private ProgressBar pgrProgress;
  private SplashImage pnlBackground;
}
