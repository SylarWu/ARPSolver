package funcarose.controller;

import UI.UI;
import funcarose.packetbean.ARPPacket;
import funcarose.packetbean.FramePacket;
import jpcap.packet.Packet;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PacketDispatch {

    private static ConcurrentLinkedQueue<FramePacket> recv_queue;

    private static ConcurrentLinkedQueue<Packet> send_queue;

    private static PacketDispatch instance = null;

    private static Thread process_send;

    private static Thread process_recv;

    private static DeviceAccess deviceAccess = DeviceAccess.getInstance();

    private static UI ui = UI.getInstance();

    private static boolean RUN = true;

    static{
        recv_queue = new ConcurrentLinkedQueue<>();
        send_queue = new ConcurrentLinkedQueue<>();
    }


    private PacketDispatch(){
        process_send = new Thread(new Runnable() {
            @Override
            public void run() {
                while(RUN){
                    processSendQueue();
                }
            }
        });

        process_recv = new Thread(new Runnable() {
            @Override
            public void run() {
                while(RUN){
                    processRecvQueue();
                }
            }
        });

        System.out.println();
        System.out.println();
        process_recv.start();
        process_send.start();
    }

    public static synchronized PacketDispatch getInstance() {
        if (instance == null){
            instance = new PacketDispatch();
        }
        return instance;
    }

    public void dispatch(Packet packet){
        this.addRecvQueue(packet);
    }

    public void addSendQueue(Packet packet){
        send_queue.add(packet);
    }

    public void addRecvQueue(Packet packet){
        recv_queue.add(new FramePacket(packet));
    }

    public void processSendQueue(){
        Packet packet = send_queue.poll();

        if (packet != null){
            deviceAccess.send(packet);
        }
    }
    public void processRecvQueue(){
        Packet packet = recv_queue.poll();

        if (packet != null){
            ui.renderingPacket(packet);
        }
    }

    public void close(){
        RUN = false;
    }

}
