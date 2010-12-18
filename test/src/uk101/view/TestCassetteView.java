/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view;

import uk101.machine.TapeRecorder;


/**
 * Unit test for the Cassette player view
 *
 * @author Baldwin
 */
public class TestCassetteView extends BaseViewTest {

    protected void setupTest() {
        // Create the cassette component and add to the frame.
        cView = new CassetteView(new TapeRecorder());
        testView.add(cView);
    }

    private CassetteView cView;

    public void testWait() throws Exception {
        while (true) {
            Thread.yield();
        }
    }
}
