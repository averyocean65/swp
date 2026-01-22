package org.averyocean65.swp.windows;

import javax.swing.*;
import java.awt.*;

public class InfoWindow extends WindowWrapper {
    public InfoWindow(String title, int width, int height) {
        super(title, width, height);
        rootFrame.setResizable(false);
    }

    @Override
    void createWindowContent() {
        rootFrame.setLayout(new BoxLayout(rootFrame.getContentPane(), BoxLayout.Y_AXIS));

        JLabel header = new JLabel("About SWP");
        Font headerFont = header.getFont()
                .deriveFont(Font.BOLD)
                .deriveFont(16.0f);
        header.setFont(headerFont);

        JLabel versionInfo = new JLabel("Version 1.0.1");
        JLabel openSourceInfo = new JLabel("GitHub: https://github.com/averyocean65/SWP");

        rootFrame.add(header, BorderLayout.CENTER);
        rootFrame.add(versionInfo, BorderLayout.CENTER);
        rootFrame.add(openSourceInfo, BorderLayout.CENTER);
    }
}
