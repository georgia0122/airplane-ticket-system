import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Initialize global UI settings
            UIUtil.init();
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }

}
