package org.cyberA;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class UserInterface {
    private Window mainWindow;
    private WindowBasedTextGUI textGUI;

    private GridLayout layoutRoot;
    private GridLayout topPanelLayout;
    private GridLayout bottomPanelLayout;

    private Label responseLabel;
    private TextBox userInput;

    public boolean initalize() {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Screen screen = null;
        try {
            screen = terminalFactory.createScreen();
            screen.startScreen();
        } catch (IOException e) {
            return false;
        }
        textGUI = new MultiWindowTextGUI(screen);
        mainWindow = new BasicWindow("CyberAdvance");

        Window.Hint[] hints = new Window.Hint[]{Window.Hint.CENTERED, Window.Hint.EXPANDED, Window.Hint.FULL_SCREEN, Window.Hint.FIXED_SIZE, Window.Hint.FIT_TERMINAL_WINDOW, Window.Hint.NO_DECORATIONS};

        mainWindow.setHints(Arrays.asList(hints));

        layoutRoot = new GridLayout(1);

        Panel rootPanel = new Panel(layoutRoot);
        rootPanel.setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.FILL,
                GridLayout.Alignment.FILL,
                false,
                false,
                1,
                1
        ));

        topPanelLayout = new GridLayout(1);
        bottomPanelLayout = new GridLayout(1);

        Panel topPanel = new Panel(topPanelLayout);
        Panel bottomPanel = new Panel(bottomPanelLayout);

        userInput = new TextBox();
        userInput.setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.FILL,
                GridLayout.Alignment.FILL,
                false,
                false,
                1,
                1
        ));

        responseLabel = new Label("model response");
        responseLabel.setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.FILL,
                GridLayout.Alignment.FILL,
                false,
                false,
                1,
                1
        ));

        topPanel.setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.FILL,
                GridLayout.Alignment.BEGINNING,
                true,
                false,
                1,
                4
        ));

        bottomPanel.setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.FILL,
                GridLayout.Alignment.END,
                true,
                false,
                1,
                1
        ));


        rootPanel.addComponent(topPanel);
        rootPanel.addComponent(bottomPanel);
        topPanel.addComponent(responseLabel);
        bottomPanel.addComponent(userInput);

        mainWindow.setComponent(rootPanel);
        textGUI.addWindowAndWait(mainWindow);


        return true;
    }
}
