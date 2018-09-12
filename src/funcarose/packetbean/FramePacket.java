package funcarose.packetbean;

import funcarose.controller.DeviceAccess;
import jpcap.packet.Packet;

public class FramePacket extends Packet {
    public static int MAC_ADDRESS_LENGTH = 6;
    public static int MAC_TYPE_LENGTH = 2;

    private String dst_mac = "";
    private String src_mac = "";
    private String type = "";

    public FramePacket(Packet source) {
        this.header = source.header;
        this.data = source.data;
        this.datalink = source.datalink;
        this.sec = source.sec;
        this.usec = source.usec;
        this.caplen = source.caplen;
        this.len = source.len;
        splitHeader();
    }

    private void splitHeader() {

        byte[] dst  = new byte[MAC_ADDRESS_LENGTH];
        byte[] src  = new byte[MAC_ADDRESS_LENGTH];
        byte[] type = new byte[MAC_TYPE_LENGTH];


        for (int i = 0; i < this.header.length && i < MAC_ADDRESS_LENGTH * 2 + MAC_TYPE_LENGTH; i++) {
            if (i < 6) {
                //目标MAC
                dst[i]                           = this.header[i];
            } else if (i < 12) {
                //来源MAC
                src[i - MAC_ADDRESS_LENGTH]      = this.header[i];
            } else {
                //上层类型
                type[i - MAC_ADDRESS_LENGTH * 2] = this.header[i];
            }
        }

        this.dst_mac = DeviceAccess.macToString(dst);
        this.src_mac = DeviceAccess.macToString(src);

        this.type +=
                (
                        (type[0] < 16 && type[0] >= 0)
                        ? ("0" + Integer.toHexString(type[0]) )
                        : Integer.toHexString(type[0])
                )
                + (
                        (type[1] < 16 && type[1] >= 0)
                        ? ("0" + Integer.toHexString(type[1]) )
                        : Integer.toHexString(type[1])
                );
    }

    public String getDst_mac() {
        return dst_mac;
    }

    public String getSrc_mac() {
        return src_mac;
    }

    public String getType() {
        return type;
    }
}
