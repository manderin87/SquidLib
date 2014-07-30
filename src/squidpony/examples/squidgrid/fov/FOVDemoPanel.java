package squidpony.squidgrid.fov;

import squidpony.squidgrid.util.RadiusStrategy;
import java.util.TreeMap;
import squidpony.squidcolor.SColorFactory;
import squidpony.squidgrid.los.BresenhamLOS;
import squidpony.squidgrid.los.EliasConcurrentLOS;
import squidpony.squidgrid.los.LOSSolver;
import squidpony.squidgrid.los.RayCastingLOS;
import squidpony.squidgrid.util.BasicRadiusStrategy;

/**
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class FOVDemoPanel extends javax.swing.JPanel {

    private TreeMap<String, FOVSolver> fovs = new TreeMap<>();
    private TreeMap<String, LOSSolver> loss = new TreeMap<>();
    private TreeMap<String, RadiusStrategy> strats = new TreeMap<>();

    /**
     * Creates new form FOVDemoPanel
     */
    public FOVDemoPanel() {
        initComponents();
//        FOVTranslator merged = new FOVTranslator();
//        merged.add(new Pair<FOVSolver, RadiusStrategy>(new ShadowFOV(), BasicRadiusStrategy.DIAMOND), 1f);
//        merged.add(new Pair<FOVSolver, RadiusStrategy>(new ShadowFOV(), BasicRadiusStrategy.SQUARE), 0.2f);
//        fovs.put("Merged", merged);
        fovs.put("Elias", new EliasFOV());
        fovs.put("Ray Casting", new RayCastingFOV());
        fovs.put("Ripple", new RippleFOV());
        fovs.put("Shadow Casting", new ShadowFOV());
        fovs.put("Spread", new SpreadFOV());
        fovs.put("Translucence Wrapper", new TranslucenceWrapperFOV());
        fovs.put("Tight Shadow Casting", new TightShadowFOV());

        fovComboBox.removeAllItems();
        for (String s : fovs.keySet()) {
            fovComboBox.addItem(s);
        }
        fovComboBox.setSelectedItem("Tight Shadow Casting");

        loss.put("Bresenham", new BresenhamLOS());
        loss.put("Elias", new EliasConcurrentLOS());
        loss.put("Ray Casting", new RayCastingLOS());

        losComboBox.removeAllItems();
        for (String s : loss.keySet()) {
            losComboBox.addItem(s);
        }
        losComboBox.setSelectedItem("Elias");

        strats.put("Circle", BasicRadiusStrategy.CIRCLE);
        strats.put("Diamond", BasicRadiusStrategy.DIAMOND);
        strats.put("Square", BasicRadiusStrategy.SQUARE);

        stratComboBox.removeAllItems();
        for (String s : strats.keySet()) {
            stratComboBox.addItem(s);
        }
        stratComboBox.setSelectedItem("Circle");
    }

    public FOVSolver getFOVSolver() {
        return fovs.get((String) fovComboBox.getSelectedItem());
    }

    public LOSSolver getLOSSolver() {
        return loss.get((String) losComboBox.getSelectedItem());
    }

    public RadiusStrategy getStrategy() {
        return strats.get((String) stratComboBox.getSelectedItem());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lightFadeButton = new javax.swing.JButton();
        lightCastButton = new javax.swing.JButton();
        radiusSlider = new javax.swing.JSlider();
        castColorPanel = new javax.swing.JPanel();
        fadeColorPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        placeLightSourceBox = new javax.swing.JCheckBox();
        playerCastsLightBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        clearBox = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        losComboBox = new javax.swing.JComboBox();
        fovComboBox = new javax.swing.JComboBox();
        stratComboBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        tileValueField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();

        lightFadeButton.setText("Light Color Fade");
        lightFadeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lightFadeButtonActionPerformed(evt);
            }
        });

        lightCastButton.setText("Light Color Cast");
        lightCastButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lightCastButtonActionPerformed(evt);
            }
        });

        radiusSlider.setMajorTickSpacing(10);
        radiusSlider.setMaximum(150);
        radiusSlider.setPaintLabels(true);
        radiusSlider.setToolTipText("Sets the radius ligth will be cast to");
        radiusSlider.setValue(20);

        castColorPanel.setBackground(new java.awt.Color(255, 255, 245));
        castColorPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        castColorPanel.setMinimumSize(new java.awt.Dimension(24, 24));
        castColorPanel.setPreferredSize(new java.awt.Dimension(24, 24));

        javax.swing.GroupLayout castColorPanelLayout = new javax.swing.GroupLayout(castColorPanel);
        castColorPanel.setLayout(castColorPanelLayout);
        castColorPanelLayout.setHorizontalGroup(
            castColorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );
        castColorPanelLayout.setVerticalGroup(
            castColorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        fadeColorPanel.setBackground(new java.awt.Color(160, 79, 0));
        fadeColorPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        fadeColorPanel.setMinimumSize(new java.awt.Dimension(24, 24));
        fadeColorPanel.setPreferredSize(new java.awt.Dimension(24, 24));

        javax.swing.GroupLayout fadeColorPanelLayout = new javax.swing.GroupLayout(fadeColorPanel);
        fadeColorPanel.setLayout(fadeColorPanelLayout);
        fadeColorPanelLayout.setHorizontalGroup(
            fadeColorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );
        fadeColorPanelLayout.setVerticalGroup(
            fadeColorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        jLabel3.setText("Radius");

        placeLightSourceBox.setText("Place Light Source Mode");
        placeLightSourceBox.setToolTipText("Places lights rather than the player @");

        playerCastsLightBox.setSelected(true);
        playerCastsLightBox.setText("Player Casts Light");
        playerCastsLightBox.setToolTipText("If the player casts light, then other lights are not used.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lightCastButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(castColorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lightFadeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fadeColorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3)
                    .addComponent(radiusSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(placeLightSourceBox)
                    .addComponent(playerCastsLightBox)))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lightCastButton, lightFadeButton});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {castColorPanel, fadeColorPanel});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lightCastButton)
                    .addComponent(castColorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(placeLightSourceBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lightFadeButton)
                    .addComponent(fadeColorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(playerCastsLightBox)))
            .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(radiusSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        clearBox.setText("Clear Map");

        jLabel1.setText("FOV Solver");

        jLabel2.setText("LOS Solver");

        losComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        fovComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        stratComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel5.setText("Radius Strategy");

        jLabel4.setText("Tile's Value");

        tileValueField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tileValueField.setText("0");
        tileValueField.setEnabled(false);
        tileValueField.setFocusable(false);
        tileValueField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tileValueFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(fovComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(losComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(tileValueField, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(clearBox))
                    .addComponent(stratComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {fovComboBox, losComboBox, stratComboBox});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(fovComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(stratComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(losComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearBox)
                    .addComponent(jLabel4)
                    .addComponent(tileValueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jSeparator1)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void lightCastButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lightCastButtonActionPerformed
        castColorPanel.setBackground(SColorFactory.showSColorChooser(this));
    }//GEN-LAST:event_lightCastButtonActionPerformed

    private void lightFadeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lightFadeButtonActionPerformed
        fadeColorPanel.setBackground(SColorFactory.showSColorChooser(this));
    }//GEN-LAST:event_lightFadeButtonActionPerformed

    private void tileValueFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tileValueFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tileValueFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JPanel castColorPanel;
    public javax.swing.JButton clearBox;
    public javax.swing.JPanel fadeColorPanel;
    private javax.swing.JComboBox fovComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton lightCastButton;
    private javax.swing.JButton lightFadeButton;
    private javax.swing.JComboBox losComboBox;
    public javax.swing.JCheckBox placeLightSourceBox;
    public javax.swing.JCheckBox playerCastsLightBox;
    public javax.swing.JSlider radiusSlider;
    private javax.swing.JComboBox stratComboBox;
    public javax.swing.JTextField tileValueField;
    // End of variables declaration//GEN-END:variables
}