/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view.component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * Machine image selection dialog accessory to select image format.
 *
 * @author Baldwin
 */
public class ImageFormat extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private JCheckBox saveMem, saveCfg, savePos;
    
    public ImageFormat() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Image options"));
        
        saveMem = new JCheckBox("Save RAM");
        saveMem.setSelected(true);
        saveMem.setEnabled(false);
        saveCfg = new JCheckBox("Save properties");
        saveCfg.setSelected(true);
        savePos = new JCheckBox("Save positions");
        savePos.setSelected(true);
        
        add(saveMem);
        add(Box.createVerticalStrut(5));
        add(saveCfg);
        add(Box.createVerticalStrut(5));
        add(savePos);
    }
    
    public boolean saveProperties() {
        return saveCfg.isSelected();
    }
    
    public boolean savePostions() {
        return savePos.isSelected();
    }
}
