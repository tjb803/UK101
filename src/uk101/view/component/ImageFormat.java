/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
 */
package uk101.view.component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * Machine image selection dialog accessory to select image format.
 */
public class ImageFormat extends JPanel {
    private static final long serialVersionUID = 1L;

    private JCheckBox imgMem, imgSnp, imgCfg, imgPos;

    public ImageFormat(boolean save) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Image options"));

        String action = (save) ? "Save " : "Load ";
        imgMem = new JCheckBox(action + "RAM");
        imgMem.setSelected(true);
        imgMem.setEnabled(false);
        imgSnp = new JCheckBox(action + "snapshot");
        imgSnp.setSelected(true);
        imgSnp.setEnabled(save);
        imgCfg = new JCheckBox(action + "properties");
        imgCfg.setSelected(true);
        imgCfg.setEnabled(save);
        imgPos = new JCheckBox(action + "positions");
        imgPos.setSelected(false);
        imgPos.setEnabled(save);

        add(imgMem);
        add(Box.createVerticalStrut(5));
        add(imgSnp);
        add(Box.createVerticalStrut(5));
        add(imgCfg);
        add(Box.createVerticalStrut(5));
        add(imgPos);
    }

    public boolean saveProperties() {
        return imgCfg.isSelected();
    }

    public boolean savePostions() {
        return imgPos.isSelected();
    }

    public boolean saveSnapshot() {
        return imgSnp.isSelected();
    }
}
