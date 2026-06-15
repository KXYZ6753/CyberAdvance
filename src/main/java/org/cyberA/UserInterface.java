package org.cyberA;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.graphics.SimpleTheme;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
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
    private BorderLayout topPanelLayout;
    private GridLayout bottomPanelLayout;

    private TextBox responseLabel;
    private TextBox userInput;

    private Runnable onSubmitFunc = null;

    //https://patorjk.com/software/taag/
    private final String DEFAULT_TEXT = """
            
                        ▄▄                         ▄▄                              \s
                        ██                         ██                              \s
            ▄████ ██ ██ ████▄ ▄█▀█▄ ████▄  ▀▀█▄ ▄████ ██ ██  ▀▀█▄ ████▄ ▄████ ▄█▀█▄\s
            ██    ██▄██ ██ ██ ██▄█▀ ██ ▀▀ ▄█▀██ ██ ██ ██▄██ ▄█▀██ ██ ██ ██    ██▄█▀\s
            ▀████  ▀██▀ ████▀ ▀█▄▄▄ ██    ▀█▄██ ▀████  ▀█▀  ▀█▄██ ██ ██ ▀████ ▀█▄▄▄\s
                    ██                                                             \s
                  ▀▀▀                                                              \s
            """;

    public void onSubmit(Runnable listener) {
        onSubmitFunc = listener;
    }

    public String getUserText() {
        return userInput.getText();
    }

    public void setUserText(String txt) {
        userInput.setText(txt);
    }

    public void setChatText(String txt) {
        responseLabel.setText(txt);
    }

    public void concatModelText(String txt) {
        responseLabel.setText(responseLabel.getText().concat(txt));
    }

    public void onSubmitCallback() {
        if (onSubmitFunc != null) {
            onSubmitFunc.run();
        }
    }

    public boolean initalize() {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();

        Screen screen = null;
        try {
            screen = terminalFactory.createScreen();
            screen.startScreen();
        } catch (IOException e) {
            return false;
        }

        SimpleTheme cyberAdvanceTheme = SimpleTheme.makeTheme(
                false,
                TextColor.ANSI.RED,
                TextColor.ANSI.BLACK,
                TextColor.ANSI.RED,
                TextColor.ANSI.BLACK,
                TextColor.ANSI.BLACK,
                TextColor.ANSI.RED,
                TextColor.ANSI.BLACK
        );

        textGUI = new MultiWindowTextGUI(screen);
        textGUI.setTheme(cyberAdvanceTheme);
        mainWindow = new BasicWindow("CyberAdvance");

        Window.Hint[] hints = new Window.Hint[]{Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS};

        mainWindow.setHints(Arrays.asList(hints));

        layoutRoot = new GridLayout(1);

        Panel rootPanel = new Panel(layoutRoot);
        rootPanel.setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.FILL,
                GridLayout.Alignment.FILL,
                true,
                true,
                1,
                1
        ));

        topPanelLayout = new BorderLayout();
        bottomPanelLayout = new GridLayout(1);

        Panel topPanel = new Panel(topPanelLayout);
        Panel bottomPanel = new Panel(bottomPanelLayout);

        // lanterna docs
        userInput = new TextBox() {
            @Override
            public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
                if (keyStroke.getKeyType() == KeyType.Enter) {
                    String message = getText();
                    onSubmitCallback();
                    return Result.HANDLED;
                }
                return super.handleKeyStroke(keyStroke);
            }
        };

        userInput.setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.FILL,
                GridLayout.Alignment.FILL,
                true,
                true,
                1,
                1
        ));

        Component userInputBorder = userInput.withBorder(Borders.singleLine());
        userInputBorder.setTheme(cyberAdvanceTheme);
        userInput.setTheme(cyberAdvanceTheme);

        bottomPanel.addComponent(userInputBorder);

        responseLabel = new TextBox(DEFAULT_TEXT, TextBox.Style.MULTI_LINE);
        responseLabel.setReadOnly(true);

        responseLabel.setLayoutData(BorderLayout.Location.CENTER);


        topPanel.addComponent(responseLabel);

        topPanel.setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.FILL,
                GridLayout.Alignment.BEGINNING,
                true,
                true,
                1,
                1
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

        mainWindow.setComponent(rootPanel);
        textGUI.addWindowAndWait(mainWindow);


        return true;
    }
}
