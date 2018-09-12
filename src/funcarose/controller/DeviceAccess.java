package funcarose.controller;


import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;

import java.io.IOException;

public class DeviceAccess {
    //单例模式
    private static DeviceAccess instance;
    //获取包的最大字节数
    private static int MAX_BYTES_NUMBER = 65535;
    //是否混杂模式
    private static final boolean PROMISC = true;
    //超时,ms
    private static int TIMEOUT = 50;
    //过滤器
    private String FILTER = "";

    //所有网卡设备
    private NetworkInterface[] all_devices = null;
    //使用接收的当前设备
    private NetworkInterface current_device = null;
    //使用发送的当前设备
    private NetworkInterface sender_device = null;
    //JpcapCapture engine
    JpcapCaptor core = null;
    //发送core
    JpcapSender sender = null;

    private DeviceAccess(){

    }

    public String [] getDeviceInfo(){

        String [] info = new String[this.all_devices.length];

        for (int i = 0 ; i < this.all_devices.length ; i++){
            info[i] = this.all_devices[i].name + this.all_devices[i].description;
        }
        return info;
    }

    /**
     * 开始listen
     */
    public void start(){
        this.core.processPacket(-1,new PacketProcess());
    }
    public void stop(){
        this.core.breakLoop();
    }

    public void sendInitial(){
        try {
            if (this.sender != null){
                closeSenderEngine();
            }

            this.sender = JpcapSender.openDevice(this.sender_device);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ARPPacket sendARPPrepared(int operation ,String dst_mac,String dst_ip,String src_mac,String src_ip){
        ARPPacket arpPacket = new ARPPacket();

        arpPacket.hardtype = ARPPacket.HARDTYPE_ETHER;

        arpPacket.prototype = ARPPacket.PROTOTYPE_IP;

        switch (operation){
            case 1:

                arpPacket.operation = ARPPacket.ARP_REQUEST;

                break;
            case 2:

                arpPacket.operation = ARPPacket.ARP_REPLY;
                break;
            default:

                arpPacket.operation = ARPPacket.ARP_REQUEST;

                break;
        }

        arpPacket.hlen = 6;
        arpPacket.plen = 4;

        arpPacket.sender_hardaddr = DeviceAccess.stringToMac(src_mac);
        arpPacket.sender_protoaddr= funcarose.packetbean.ARPPacket.stringToIp(src_ip);
        arpPacket.target_hardaddr = DeviceAccess.stringToMac(dst_mac);
        arpPacket.target_protoaddr= funcarose.packetbean.ARPPacket.stringToIp(dst_ip);

        EthernetPacket temp = new EthernetPacket();

        temp.frametype = EthernetPacket.ETHERTYPE_ARP;
        temp.src_mac = arpPacket.sender_hardaddr;
        temp.dst_mac = arpPacket.target_hardaddr;

        arpPacket.datalink = temp;

        return arpPacket;
    }

    public synchronized void send(Packet packet){
        this.sender.sendPacket(packet);
    }

    /**
     * 单例模式返回实例
     * @return DeviceAccess 实例
     */
    public static DeviceAccess getInstance(){
        if (instance == null){
            instance = new DeviceAccess();
        }
        return instance;
    }

    /**
     * 加载当前所有设备
     * @return 成功与否
     */
    public boolean accessAllDevices(){

        this.all_devices = JpcapCaptor.getDeviceList();

        if (this.all_devices == null){
            return false;
        }

        return true;
    }

    /**
     * 选择设备
     * @param i 设备编号
     * @return 是否成功
     */
    public synchronized boolean pickRecvDevice(int i){
        this.current_device = all_devices[ i - 1 ];
        return true;
    }

    public synchronized boolean pickSendDevice(int i){
        this.sender_device  = all_devices[i - 1];
        return true;
    }
    /**
     * 打开当前设备
     * @return boolean 返回成功与否
     */
    public synchronized boolean openCurrentDevice(){
        if (core != null){
            closeCurrentDevice();
        }
        try {
            core = JpcapCaptor.openDevice(this.current_device,MAX_BYTES_NUMBER,PROMISC,TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * 关闭当前设备
     */
    public synchronized void closeCurrentDevice(){
        if (this.core != null){
            core.close();
            core = null;
        }
    }
    /**
     * 设置过滤器
     * @param FILTER
     */
    public void setFILTER(String FILTER) {
        this.FILTER = FILTER;

        if (this.core != null){
            try {
                this.core.setFilter(this.FILTER,true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void closeSenderEngine(){
        if (this.sender != null){
            this.sender.close();
            this.sender = null;
        }
    }

    public NetworkInterface getCurrent_device() {
        return current_device;
    }

    public String getInfo(){
        this.core.updateStat();
        return "recv:" + this.core.received_packets + "|drop:" +this.core.dropped_packets;
    }

    /**
     *
     * @param macaddress
     * @return MAC地址字符串形式
     */
    public static String macToString(byte [] macaddress){
        String result = "";
        boolean flag = true;
        for (byte b : macaddress){
            if (flag){
                if (b < 16 && b >=0 ){
                    result += "0" + Integer.toHexString((b&0xff));
                }else {
                    result += Integer.toHexString((b&0xff));
                }
                flag = false;
            }else {
                if (b < 16 && b >=0 ){
                    result += ":0" + Integer.toHexString((b&0xff));
                }else {
                    result += ":" + Integer.toHexString((b&0xff));
                }
            }
        }
        return result;
    }

    /**
     *
     * @param macaddress
     * @return 底层MAC格式
     */
    public static byte [] stringToMac(String macaddress){
        byte [] result = new byte[6];
        String [] temp = macaddress.split(":");
        int i = 0;
        for (String x : temp){
            result[i++] = (byte)Integer.parseInt(x,16);
        }
        return result;
    }
}
