/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2017
 */
package uk101.view;

import uk101.machine.TapeRecorder;


/**
 * Unit test for the Cassette player view
 */
public class TestCassetteView extends BaseViewTest {

    protected void setupTest() {
        // Create the cassette component and add to the frame.
        cView = new CassetteView(new TapeRecorder(testComputer.acia), testCfg);
        testView.add(cView.display());
    }

    private CassetteView cView;

    public void testPlayRec() throws Exception { 
        while (true) {
            cView.setRead();
            Thread.sleep(2000);
            cView.setWrite();
            Thread.sleep(2000);
        }
    }
}
