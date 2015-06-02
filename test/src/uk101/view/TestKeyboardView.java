/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view;

import uk101.hardware.Keyboard;
import uk101.machine.Configuration;

/**
 * Unit test for the keyboard view
 */
public class TestKeyboardView extends BaseViewTest {

    protected void setupTest() {
    }

    private KeyboardView kbView;

    public void testKeysUK() throws Exception {
        testCfg.setValue(Configuration.KBD_MODE, Configuration.NORMAL);
        kbView = new KeyboardView(testComputer, new Keyboard("uk"), testCfg);
        testView.add(kbView);
        while (true) {
            Thread.yield();
        }
    }
    
    public void testKeysUS() throws Exception {
        testCfg.setValue(Configuration.KBD_MODE, Configuration.GAME);
        kbView = new KeyboardView(testComputer, new Keyboard("us"), testCfg);
        testView.add(kbView);
        while (true) {
            Thread.yield();
        }
    }
}
