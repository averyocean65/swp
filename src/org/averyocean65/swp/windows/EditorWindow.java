package org.averyocean65.swp.windows;

import org.averyocean65.swp.IO;
import org.averyocean65.swp.Result;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Hashtable;

public final class EditorWindow extends WindowWrapper implements ActionListener, KeyListener, UndoableEditListener {
    private JTabbedPane tabs;
    private UndoManager undoManager;

    private Hashtable<String, Runnable> menuActionMap = new Hashtable<>();

    private JMenuItem newFile;
    private JMenuItem openFile;
    private JMenuItem saveFile;
    private JMenuItem saveAsFile;
    private JMenuItem closeFile;

    private JMenuItem undoAction;
    private JMenuItem redoAction;

    private JMenuItem aboutSwp;

    public EditorWindow(String title, int width, int height) {
        super(title, width, height);
        undoManager = new UndoManager();
        menuActionMap = new Hashtable<>();
        createMenu();
        rootFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JMenuItem createMenuItem(String text, String keybindTooltip, Runnable callback) {
        JMenuItem item = new JMenuItem(text);

        if(!keybindTooltip.isEmpty()) {
            item.setToolTipText("Keybind: " + keybindTooltip);
        }

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

        newFile = createMenuItem("New", "Ctrl + T", this::createBlankFileProcedure);
        openFile = createMenuItem("Open", "Ctrl + O", this::loadFileProcedure);
        saveFile = createMenuItem("Save", "Ctrl + S", this::saveFileProcedure);
        saveAsFile = createMenuItem("Save As", "Ctrl + Shift + S", this::saveAsProcedure);
        closeFile = createMenuItem("Close File", "Ctrl + W", this::closeCurrentFileProcedure);

        fileCategory.add(newFile);
        fileCategory.add(openFile);
        fileCategory.add(saveFile);
        fileCategory.add(saveAsFile);
        fileCategory.add(closeFile);
        menuBar.add(fileCategory);

        // EDIT
        JMenu editCategory = new JMenu("Edit");

        undoAction = createMenuItem("Undo", "Ctrl + Z", this::undoProcedure);
        redoAction = createMenuItem("Redo", "Ctrl + Shift + Z", this::redoProcedure);

        editCategory.add(undoAction);
        editCategory.add(redoAction);
        menuBar.add(editCategory);

        // HELP
        JMenu helpCategory = new JMenu("Help");

        aboutSwp = createMenuItem("About", "", this::aboutSwp);

        helpCategory.add(aboutSwp);
        menuBar.add(helpCategory);

        // FINAL
        rootFrame.setJMenuBar(menuBar);
    }

    @Override
    void createWindowContent() {
        tabs = new JTabbedPane();
        rootFrame.add(tabs);

        createBlankFileProcedure();
    }

    private void createBlankFileProcedure() {
        createFileTab("");
    }

    private void loadFileProcedure() {
        FileDialog dialog = new FileDialog(rootFrame, "Choose a file", FileDialog.LOAD);
        dialog.setFile("*");
        dialog.setMultipleMode(true);
        dialog.setVisible(true);

        File[] files = dialog.getFiles();
        for(int i = 0; i < files.length; i++) {
            File openingFile = files[i];
            if(!IO.doesFileExist(openingFile)) {
                JOptionPane.showMessageDialog(rootFrame, "Selected file (" + openingFile + ") doesn't exist!", "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            createFileTab(openingFile);
        }
    }

    private void closeCurrentFileProcedure() {
        closeCurrentFileProcedure(false, false);
    }

    private void closeCurrentFileProcedure(boolean forceKeepOpen, boolean noSavePrompt) {
        if(!noSavePrompt) {
            int saveProject = JOptionPane.showConfirmDialog(rootFrame, "Would you like to save your file?", "Warning", JOptionPane.YES_NO_OPTION);
            if (saveProject == JOptionPane.YES_OPTION) {
                saveFileProcedure();
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

    private void saveFileProcedure() {
        saveFile(false);
    }

    private void saveAsProcedure() {
        saveFile(true);
    }

    private void saveFile(boolean saveAs) {
        int tabIndex = tabs.getSelectedIndex();
        String filePath = tabs.getTitleAt(tabIndex);

        // get file content
        JTextArea area = (JTextArea) tabs.getSelectedComponent();
        String fileContent = area.getText();

        boolean reloadTab = saveAs || filePath == null || filePath.isBlank() || filePath.equals("New");
        if(reloadTab) {
            FileDialog dialog = new FileDialog(rootFrame, "", FileDialog.SAVE);
            dialog.setFile("*");
            dialog.setVisible(true);

            filePath = dialog.getDirectory() + "/" + dialog.getFile();
            System.out.println("File path: " + filePath);
        }

        boolean writeSuccess = IO.writeFile(filePath, fileContent);
        if (!writeSuccess) {
            JOptionPane.showMessageDialog(rootFrame, "Please select a destination to save your file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if(reloadTab) {
            if(!saveAs) closeCurrentFileProcedure(true, true);
            createFileTab(filePath);
        }
    }

    private void undoProcedure() {
        if(undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    private void redoProcedure() {
        if(undoManager.canRedo()) {
            undoManager.redo();
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
        textArea.getDocument().addUndoableEditListener(this);

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
            case KeyEvent.VK_S: saveFile(e.isShiftDown()); break;
            case KeyEvent.VK_O: loadFileProcedure(); break;
            case KeyEvent.VK_W: closeCurrentFileProcedure(); break;
            case KeyEvent.VK_T: createBlankFileProcedure(); break;
            case KeyEvent.VK_SPACE: cycleTabs(e.isShiftDown()); break;
            case KeyEvent.VK_Z: {
                if(e.isShiftDown()) {
                    redoProcedure();
                    break;
                }

                undoProcedure();
                break;
            }
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

    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        undoManager.addEdit(e.getEdit());
    }
}
