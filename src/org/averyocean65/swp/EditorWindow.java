package org.averyocean65.swp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.Runnable;
import java.util.HashMap;
import java.util.Hashtable;

public final class EditorWindow extends WindowWrapper implements ActionListener {
    private JTabbedPane tabs;

    private Hashtable<String, Runnable> menuActionMap = new Hashtable<>();

    private JMenuItem newFile;
    private JMenuItem openFile;
    private JMenuItem saveFile;
    private JMenuItem closeFile;

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
        dialog.setVisible(true);

        String file = dialog.getFile();
        if(file == null) {
            return;
        }

        createFileTab(file);
    }

    private void closeCurrentFile() {
        closeCurrentFile(false);
    }

    private void closeCurrentFile(boolean forceKeepOpen) {
        tabs.remove(tabs.getSelectedIndex());

        boolean shouldCloseWindow = tabs.getTabCount() < 1;
        if(shouldCloseWindow && !forceKeepOpen) {
            rootFrame.dispatchEvent(new WindowEvent(rootFrame, WindowEvent.WINDOW_CLOSING));
            return;
        }

        // special case for when forceKeepOpen = true
        if(!shouldCloseWindow) {
            tabs.setSelectedIndex(0);
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
            closeCurrentFile(true);
            createFileTab(filePath);
        }
    }

    private void createFileTab(String path) {
        String tabTitle = path;
        String tabContent = "";

        Result<File> file = IO.findFile(path);

        if(path.isBlank() || !file.success) {
            tabTitle = "New";
        } else {
            tabTitle = file.value.getAbsolutePath();
            Result<String> read = IO.readFile(file.value);

            if(read.success) {
                tabContent = read.value;
            }
        }

        JTextArea textArea = new JTextArea(tabContent);
        tabs.add(textArea, tabTitle);
        tabs.setSelectedIndex(tabs.getTabCount() - 1);
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
}
