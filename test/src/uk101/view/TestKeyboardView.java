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
        // Create the store display component and add to the frame.
        kbView = new KeyboardView(testComputer, new Keyboard());
        testView.add(kbView);
    }

    private KeyboardView kbView;

    public void testKeys() throws Exception {
        while (true) {
            Thread.yield();
        }
    }
}
