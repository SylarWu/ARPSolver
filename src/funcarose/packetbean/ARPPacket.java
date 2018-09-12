package funcarose.packetbean;

import funcarose.controller.DeviceAccess;
import jpcap.packet.Packet;

public class ARPPacket extends FramePacket{
    public static final int ARP_PACKET_LENGTH        = 28;

    public static final int ARP_PACKET_HEADER_LENGTH = 8;

    public static final int ARP_PACKET_DATA_LENGTH   = 20;

    public static final int IP_ADDRESS_LENGTH        = 4;

    private byte[] og_data;

    private byte[] arp_header = new byte[ARP_PACKET_HEADER_LENGTH];

    private byte[] arp_data = new byte[ARP_PACKET_DATA_LENGTH];

    private String type_hard = "";

    private String type_protocol = "";

    private short length_hard = 0;

    private short length_protocol = 0;

    private short opration = 0;

    private String src_ip  = null;

    private String dst_ip  = null;

    private String src_mac = null;

    private String dst_mac = null;

    public ARPPacket(Packet packet){
        super(packet);

        this.og_data = arpSplit(this.header);

        extractHeaderNData();

        extractHeader();

        extractData();
    }

    public ARPPacket(FramePacket packet){
        this((Packet) packet);
    }


    private void extractHeaderNData(){
        int i = 0;

        for (byte b : this.og_data){
            if (i < ARP_PACKET_HEADER_LENGTH){
                this.arp_header[i++]                          = b;
            }else {
                this.arp_data[i++ - ARP_PACKET_HEADER_LENGTH] = b;
            }
        }

    }

    private synchronized void extractHeader(){
        this.type_hard += this.arp_header[0] + "" + this.arp_header[1];
        this.type_protocol += this.arp_header[2] + "" + this.arp_header[3];
        this.length_hard = (short)(0 + this.arp_header[4]);
        this.length_protocol = (short)(0 + this.arp_header[5]);
        this.opration = (short) ((this.arp_header[6] << 8) + (this.arp_header[7]));
    }

    private  synchronized void extractData(){

        byte [] src_mac = new byte[FramePacket.MAC_ADDRESS_LENGTH];
        byte [] dst_mac = new byte[FramePacket.MAC_ADDRESS_LENGTH];
        byte [] src_ip  = new byte[IP_ADDRESS_LENGTH];
        byte [] dst_ip  = new byte[IP_ADDRESS_LENGTH];


        for (int i = 0; i < this.arp_data.length && i < ARP_PACKET_DATA_LENGTH ; i++){
            if (i < FramePacket.MAC_ADDRESS_LENGTH){
                //源MAC
                src_mac[i]                                                         = this.arp_data[i];

            }else if(i < FramePacket.MAC_ADDRESS_LENGTH + IP_ADDRESS_LENGTH){
                //源IP
                src_ip[i - FramePacket.MAC_ADDRESS_LENGTH]                         = this.arp_data[i];

            }else if(i < FramePacket.MAC_ADDRESS_LENGTH * 2 + IP_ADDRESS_LENGTH){
                //目的MAC
                dst_mac[i - FramePacket.MAC_ADDRESS_LENGTH - IP_ADDRESS_LENGTH]    = this.arp_data[i];

            }else {
                //目的IP
                dst_ip[i - FramePacket.MAC_ADDRESS_LENGTH * 2 - IP_ADDRESS_LENGTH] = this.arp_data[i];
            }
        }

        this.src_mac = DeviceAccess.macToString(src_mac);
        this.dst_mac = DeviceAccess.macToString(dst_mac);
        this.src_ip  = ipToString(src_ip);
        this.dst_ip  = ipToString(dst_ip);

    }

    public String getType_hard() {
        return type_hard;
    }

    public String getType_protocol() {
        return type_protocol;
    }

    public short getLength_hard() {
        return length_hard;
    }

    public short getLength_protocol() {
        return length_protocol;
    }

    public short getOpration() {
        return opration;
    }

    public String getSrc_ip() {
        return src_ip;
    }

    public String getDst_ip() {
        return dst_ip;
    }

    public String getSrc_mac() {
        return src_mac;
    }

    public String getDst_mac() {
        return dst_mac;
    }

    public static String ipToString(byte [] ip_address){

        return (ip_address[0]&0xff) + "." + (ip_address[1]&0xff) + "." + (ip_address[2]&0xff) + "." + (ip_address[3]&0xff);

    }

    public static byte [] stringToIp(String ip_address){
        String [] ip = ip_address.split("\\.");

        byte [] temp = new byte[4];

        for (int i =0 ;i<4;i++){
            temp[i] = (byte) Short.parseShort(ip[i]);
        }

        return temp;
    }

    private synchronized byte [] arpSplit(byte [] source){

        int prefix_num = FramePacket.MAC_ADDRESS_LENGTH * 2 + FramePacket.MAC_TYPE_LENGTH;

        byte [] result = new byte [ARP_PACKET_LENGTH];

        for (int i =  prefix_num; i < prefix_num + ARP_PACKET_LENGTH;i++){
            result[i - prefix_num] = source[i];
        }

        return result;
    }

}
