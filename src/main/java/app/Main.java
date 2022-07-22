package app;

import root.User;
import root.database.passport.LogAccount;

import javax.swing.*;

public class Main {

    public static boolean bFirstFrameLoaded = false; //todo:: store window(per-frame) config options

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(() -> {
                JFrame pFrame = new JFrame("AdminTool");
                pFrame.setContentPane(new AdminTool().GetPanel());
                pFrame.setDefaultCloseOperation(!bFirstFrameLoaded ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
                pFrame.pack();
                pFrame.setVisible(true);
                bFirstFrameLoaded = true;
           });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
