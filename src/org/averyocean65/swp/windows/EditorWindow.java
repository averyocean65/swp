package org.averyocean65.swp.windows;

import org.averyocean65.swp.IO;
import org.averyocean65.swp.Result;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Hashtable;

public final class EditorWindow extends WindowWrapper implements ActionListener, KeyListener {
    private JTabbedPane tabs;

    private Hashtable<String, Runnable> menuActionMap = new Hashtable<>();

    private JMenuItem newFile;
    private JMenuItem openFile;
    private JMenuItem saveFile;
    private JMenuItem closeFile;

    private JMenuItem aboutSwp;

    public EditorWindow(String title, int width, int height) {
        super(title, width, height);
        menuActionMap = new Hashtable<>();
        createMenu();
        rootFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JMenuItem createMenuItem(String text, Runnable callback) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(this);

        if(callback != null && menuActionMap != null) {
            menuActionMap.put(text, callback);
        }

        return item;
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        // FILES
        JMenu fileCategory = new JMenu("File");

        newFile = createMenuItem("New File", this::createBlankTab);
        openFile = createMenuItem("Open File", this::loadNewFile);
        saveFile = createMenuItem("Save File", this::saveCurrentFile);
        closeFile = createMenuItem("Close File", this::closeCurrentFile);

        fileCategory.add(newFile);
        fileCategory.add(openFile);
        fileCategory.add(saveFile);
        fileCategory.add(closeFile);
        menuBar.add(fileCategory);

        // HELP
        JMenu helpCategory = new JMenu("Help");

        aboutSwp = createMenuItem("About", this::aboutSwp);

        helpCategory.add(aboutSwp);
        menuBar.add(helpCategory);

        // FINAL
        rootFrame.setJMenuBar(menuBar);
    }

    @Override
    void createWindowContent() {
        tabs = new JTabbedPane();
        rootFrame.add(tabs);

        createBlankTab();
    }

    private void createBlankTab() {
        createFileTab("");
    }

    private void loadNewFile() {
        FileDialog dialog = new FileDialog(rootFrame, "Choose a file", FileDialog.LOAD);
        dialog.setFile("*");
        dialog.setMultipleMode(true);
        dialog.setVisible(true);

        File[] files = dialog.getFiles();
        for(int i = 0; i < files.length; i++) {
            File openingFile = files[i];
            if(!IO.doesFileExist(openingFile)) {
                continue;
            }

            createFileTab(openingFile);
        }
    }

    private void closeCurrentFile() {
        closeCurrentFile(false, false);
    }

    private void closeCurrentFile(boolean forceKeepOpen, boolean noSavePrompt) {
        if(!noSavePrompt) {
            int saveProject = JOptionPane.showConfirmDialog(rootFrame, "Would you like to save your file?", "Warning", JOptionPane.YES_NO_OPTION);
            if (saveProject == JOptionPane.YES_OPTION) {
                saveCurrentFile();
            } else if(saveProject == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }

        int currentIndex = tabs.getSelectedIndex();
        tabs.remove(tabs.getSelectedIndex());

        boolean shouldCloseWindow = tabs.getTabCount() < 1;
        if(shouldCloseWindow && !forceKeepOpen) {
            rootFrame.dispatchEvent(new WindowEvent(rootFrame, WindowEvent.WINDOW_CLOSING));
            return;
        }

        // special case for when forceKeepOpen = true
        if(!shouldCloseWindow) {
            // essentially a clamp operation
            currentIndex = Math.max(0, Math.min(tabs.getTabCount() - 1, currentIndex));
            tabs.setSelectedIndex(currentIndex);
        }
    }

    private void saveCurrentFile() {
        int tabIndex = tabs.getSelectedIndex();
        String filePath = tabs.getTitleAt(tabIndex);

        // get file content
        JTextArea area = (JTextArea) tabs.getSelectedComponent();
        String fileContent = area.getText();

        boolean reloadTab = filePath == null || filePath.isBlank() || filePath.equals("New");
        if(reloadTab) {
            FileDialog dialog = new FileDialog(rootFrame, "", FileDialog.SAVE);
            dialog.setFile("*");
            dialog.setVisible(true);

            filePath = dialog.getFile();
        }

        boolean writeSuccess = IO.writeFile(filePath, fileContent);
        if (!writeSuccess) {
            JOptionPane.showMessageDialog(rootFrame, "Please select a file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if(reloadTab) {
            closeCurrentFile(true, true);
            createFileTab(filePath);
        }
    }

    private void aboutSwp() {
        InfoWindow window = new InfoWindow("About SWP", 300, 150);
        window.setShowing(true);
    }

    private void createFileTab(File file) {
        String tabTitle = "";
        String tabContent = "";

        if (file == null || !file.exists()) {
            tabTitle = "New";
        } else {
            tabTitle = file.getAbsolutePath();
            Result<String> read = IO.readFile(file);

            if(read.success) {
                tabContent = read.value;
            }
        }

        JTextArea textArea = new JTextArea(tabContent);
        textArea.setLineWrap(true);

        tabs.add(textArea, tabTitle);
        tabs.setSelectedIndex(tabs.getTabCount() - 1);

        Component currentComponent = tabs.getSelectedComponent();
        currentComponent.addKeyListener(this);
    }

    private void createFileTab(String path) {
        if(path == null) {
            System.err.println("createFileTab() path == null!");
            return;
        }

        Result<File> file = IO.findFile(path);
        createFileTab(file.value);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if(!menuActionMap.containsKey(command)) {
            System.out.printf("TODO: Implement %s%n", command);
            return;
        }

        Runnable function = menuActionMap.get(command);
        try {
            function.run();
        } catch (Exception ex) {
            System.out.println("An exception occurred.");
            ex.printStackTrace();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(!e.isControlDown()) {
            return;
        }

        switch(e.getKeyCode()) {
            case KeyEvent.VK_S: saveCurrentFile(); break;
            case KeyEvent.VK_O: loadNewFile(); break;
            case KeyEvent.VK_W: closeCurrentFile(); break;
            case KeyEvent.VK_T: createBlankTab(); break;
            case KeyEvent.VK_SPACE: cycleTabs(e.isShiftDown()); break;
            default: return;
        }
    }

    private void cycleTabs(boolean reverse) {
        int selectedIndex = tabs.getSelectedIndex();
        int maxIndex = tabs.getTabCount() - 1;

        selectedIndex = reverse ? selectedIndex - 1 : selectedIndex + 1;

        selectedIndex = selectedIndex > maxIndex ? 0 : selectedIndex;
        selectedIndex = selectedIndex < 0 ? maxIndex : selectedIndex;

        tabs.setSelectedIndex(selectedIndex);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
