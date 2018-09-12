package funcarose;

import funcarose.controller.DeviceAccess;

public class main {
    public static void main(String args[]){
        DeviceAccess deviceAccess = DeviceAccess.getInstance();

        deviceAccess.accessAllDevices();

        deviceAccess.pickRecvDevice(2);

        deviceAccess.openCurrentDevice();

        while (true){
            deviceAccess.start();
        }

    }
}
