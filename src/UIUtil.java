import javax.swing.*;
import java.awt.*;

public class UIUtil {
    public static void init() {
        try {
            // Try Nimbus for a modern look; fall back to system LAF
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ex) {}
        }

        // Global font scale
        Font base = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object k = keys.nextElement();
            Object v = UIManager.get(k);
            if (v instanceof Font) UIManager.put(k, base);
        }

        // Table header font and colors
        UIManager.put("Table.font", base);
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("TableHeader.font", base.deriveFont(Font.BOLD, 14f));

        // Button padding
        UIManager.put("Button.font", base.deriveFont(Font.BOLD, 13f));
        UIManager.put("Button.margin", new Insets(6,12,6,12));

        // Dialogs
        UIManager.put("OptionPane.messageFont", base);
        UIManager.put("OptionPane.buttonFont", base.deriveFont(Font.BOLD, 13f));

        // Tabbed pane
        UIManager.put("TabbedPane.tabInsets", new Insets(8,12,8,12));

        // Other niceties
        UIManager.put("Label.font", base);
        UIManager.put("TextField.font", base);
        UIManager.put("PasswordField.font", base);
        UIManager.put("TextArea.font", base);
        UIManager.put("ComboBox.font", base);
    }
}
