package org.averyocean65.swp.windows;

import javax.swing.*;

public abstract class WindowWrapper {
    protected JFrame rootFrame;

    protected WindowWrapper(String title, int width, int height) {
        rootFrame = new JFrame(title);
        rootFrame.setSize(width, height);

        createWindowContent();
    }

    public boolean isShowing() {
        return rootFrame.isVisible();
    }

    public void setShowing(boolean showing) {
        rootFrame.setVisible(showing);
    }

    abstract void createWindowContent();
}
