package cn.xeblog.plugin.action;

import cn.xeblog.plugin.entity.TextRender;
import cn.xeblog.plugin.enums.Command;
import cn.xeblog.plugin.enums.Style;
import cn.xeblog.plugin.mode.ModeContext;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.actions.OpenFileAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author anlingyi
 * @date 2020/6/1
 */
public class ConsoleAction {

    private static JTextPane console;

    private static JPanel panel;

    private static JScrollPane consoleScroll;

    private static boolean isNewLine;

    public static void renderText(List<TextRender> list) {
        for (TextRender textRender : list) {
            renderText(textRender.getText(), textRender.getStyle());
        }
    }

    public static void renderText(String text) {
        renderText(text, Style.DEFAULT);
    }

    public static void renderText(String text, Style style) {
        if (isNewLine) {
            ModeContext.getMode().renderTextBefore(text);
        }

        render(text, style.get());

        isNewLine = text.endsWith("\n");
    }

    public static void showSimpleMsg(String msg) {
        renderText(msg + "\n", Style.DEFAULT);
    }

    public static void render(String content, AttributeSet attributeSet) {
        Document document = console.getDocument();
        try {
            document.insertString(document.getLength(), content, attributeSet);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        gotoConsoleLow();
    }

    public static void renderImage(String filePath) {
        JLabel imgLabel = new JLabel("查看图片");
        imgLabel.setAlignmentY(0.85f);
        imgLabel.setToolTipText("点击查看图片");
        imgLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        imgLabel.setForeground(StyleConstants.getForeground(Style.DEFAULT.get()));
        imgLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    Project[] projects = ProjectManager.getInstance().getOpenProjects();
                    OpenFileAction.openFile(filePath, projects[projects.length - 1]);
                });
            }
        });

        synchronized (console) {
            renderText("[");
            renderComponent(imgLabel);
            renderText("]\n");
        }
    }

    public static void renderUrl(String title, String url) {
        JLabel label = new JLabel(title);
        label.setAlignmentY(0.85f);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setForeground(StyleConstants.getForeground(Style.DEFAULT.get()));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.browse(url);
            }
        });
        renderComponent(label);
    }

    public static void renderComponent(Component component) {
        JScrollBar verticalScrollBar = consoleScroll.getVerticalScrollBar();
        int beforeScrollVal = verticalScrollBar.getValue();
        updateCaretPosition(-1);
        console.insertComponent(component);
        SwingUtilities.invokeLater(() -> verticalScrollBar.setValue(beforeScrollVal));
    }

    public static void clean() {
        console.setText("");
    }

    public static void setConsoleTitle(String title) {
        synchronized (panel) {
            ((TitledBorder) panel.getBorder()).setTitle(title);
            panel.updateUI();
        }
    }

    public static void gotoConsoleLow() {
        synchronized (consoleScroll) {
            JScrollBar verticalScrollBar = consoleScroll.getVerticalScrollBar();
            if (verticalScrollBar.getValue() + 20 < verticalScrollBar.getMaximum() - verticalScrollBar.getHeight()) {
                return;
            }

            updateCaretPosition(-1);
        }
    }

    public static void showErrorMsg() {
        ConsoleAction.showSimpleMsg("输入的命令有误！帮助命令：" + Command.HELP.getCommand());
    }

    public static void showLoginMsg() {
        ConsoleAction.showSimpleMsg("请先登录！登录命令：" + Command.LOGIN.getCommand() + "，帮助命令：" + Command.HELP.getCommand());
    }

    public static void setConsole(JTextPane console) {
        ConsoleAction.console = console;
    }

    public static void setPanel(JPanel panel) {
        ConsoleAction.panel = panel;
    }

    public static void setConsoleScroll(JScrollPane consoleScroll) {
        ConsoleAction.consoleScroll = consoleScroll;
    }

    public static void showSystemMsg(String time, String msg) {
        ConsoleAction.renderText(String.format("[%s] 系统消息：%s\n", time, msg), Style.SYSTEM_MSG);
    }

    private static void updateCaretPosition(int position) {
        if (position == -1) {
            position = console.getDocument().getLength();
        }
        console.setCaretPosition(position);
    }

}
