package funcarose.controller;


import funcarose.packetbean.ARPPacket;
import funcarose.packetbean.FramePacket;
import jpcap.PacketReceiver;
import jpcap.packet.Packet;


public class PacketProcess implements PacketReceiver {

    private static PacketDispatch dispatch = PacketDispatch.getInstance();

    @Override
    public void receivePacket(Packet packet) {
        dispatch.dispatch(packet);
    }


}
