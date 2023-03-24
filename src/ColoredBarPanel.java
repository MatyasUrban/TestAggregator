// Copyright (c) 2023 Matyas Urban. Licensed under the MIT license.

import javax.swing.*;
import java.awt.*;

class ColoredBarPanel extends JPanel {
    private final double passRate;

    /**
     * Class constructor
     * @param passRate Pass rate (ratio of successful vs failed tests)
     */
    public ColoredBarPanel(double passRate) {
        this.passRate = passRate;
    }

    /**
     * Fills the bar with correct proportion of green and red according to the pass rate.
     * @param g the <code>Graphics</code> object to protect
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        int greenWidth = (int) (width * passRate);
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, greenWidth, height);
        g.setColor(Color.RED);
        g.fillRect(greenWidth, 0, width - greenWidth, height);
    }
}