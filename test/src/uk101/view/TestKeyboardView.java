/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view;

import uk101.hardware.Keyboard;

/**
 * Unit test for the keyboard view
 *
 * @author Baldwin
 */
public class TestKeyboardView extends BaseViewTest {

    protected void setupTest() {
    }

    private KeyboardView kbView;

    public void testKeysUK() throws Exception {
        kbView = new KeyboardView(testComputer, new Keyboard("uk"));
        testView.add(kbView);
        while (true) {
            Thread.yield();
        }
    }
    
    public void testKeysUS() throws Exception {
        kbView = new KeyboardView(testComputer, new Keyboard("us"));
        testView.add(kbView);
        while (true) {
            Thread.yield();
        }
    }
}
