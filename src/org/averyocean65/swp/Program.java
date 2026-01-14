package org.averyocean65.swp;

import org.averyocean65.swp.windows.EditorWindow;

public final class Program {
    public static void main(String[] args) {
        EditorWindow window = new EditorWindow("Test", 800, 600);
        window.setShowing(true);
    }
}
