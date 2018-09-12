import funcarose.controller.DeviceAccess;
import UI.UI;
import funcarose.controller.PacketDispatch;

import java.awt.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    DeviceAccess deviceAccess = DeviceAccess.getInstance();

                    deviceAccess.accessAllDevices();

                    PacketDispatch dispatch = PacketDispatch.getInstance();

                    UI window = UI.getInstance();

                    window.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
