package org.averyocean65.swp;

import javax.swing.*;
import java.awt.*;

public final class Program {
    public static void main(String[] args) {
        JFrame frame = new JFrame("My window");

        frame.setSize(new Dimension(300, 400));
        frame.setVisible(true);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
