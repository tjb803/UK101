/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view;

/**
 * Unit test for the Control panel.
 */
public class TestMachinelView extends BaseViewTest {

    protected void setupTest() throws Exception {
        // Create the store display component and add to the frame.
        machineView = new MachineView(testComputer, null);
        testView.add(machineView);
    }

    private MachineView machineView;

    public void testRandom() throws Exception {
        while (true) {
            Thread.yield();
        }
    }
}
