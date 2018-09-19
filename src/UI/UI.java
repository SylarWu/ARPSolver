package UI;

import com.sun.tools.javac.Main;
import funcarose.controller.DeviceAccess;
import funcarose.controller.PacketDispatch;
import funcarose.packetbean.ARPPacket;
import funcarose.packetbean.FramePacket;
import jpcap.NetworkInterfaceAddress;
import jpcap.packet.Packet;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import java.awt.Font;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;

public class UI {

    private JFrame MainFrame;

    private JTextField tfDstMac;
    private JTextField tfSrcMac;
    private JTextField tfDstIp;
    private JTextField tfSrcIp;

    private JTable infoTable;

    private static DeviceAccess deviceAccess = DeviceAccess.getInstance();
    private static PacketDispatch dispatch = PacketDispatch.getInstance();

    private static UI instance = null;
    private static int No = 0;
    private DefaultTableModel model;
    private static volatile boolean KEEPING = true;
    private static boolean ARPFILTER = false;
    private static Thread loop = new Thread(new Runnable() {
        @Override
        public void run() {
            while (KEEPING){
                deviceAccess.start();
            }
        }
    });

    public static UI getInstance(){
        if (instance == null){
            instance = new UI();
        }
        return instance;
    }

    /**
     * Create the application.
     */
    private UI() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        MainFrame = new JFrame();
        MainFrame.setAlwaysOnTop(true);
        MainFrame.setTitle("ARPSolver");
        MainFrame.setBounds(100, 100, 1200, 499);
        MainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel labelNetInterface_S = new JLabel("\u9009\u62E9\u7F51\u5361");
        labelNetInterface_S.setFont(new Font("宋体", Font.PLAIN, 15));

        JComboBox choseNetInterface_S = new JComboBox();
        choseNetInterface_S.setModel(new DefaultComboBoxModel(deviceAccess.getDeviceInfo()));
        choseNetInterface_S.setFont(new Font("宋体", Font.PLAIN, 15));
        choseNetInterface_S.setSelectedIndex(0);
        deviceAccess.pickSendDevice(1);
        deviceAccess.sendInitial();
        choseNetInterface_S.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("comboBoxChanged")){
                    deviceAccess.pickSendDevice(choseNetInterface_S.getSelectedIndex() + 1);

                    deviceAccess.sendInitial();
                }
            }
        });

        JLabel labelDstMac = new JLabel("\u76EE\u7684MAC\u5730\u5740");
        labelDstMac.setFont(new Font("宋体", Font.PLAIN, 15));

        JLabel labelSrcMac = new JLabel("\u6E90MAC\u5730\u5740");
        labelSrcMac.setFont(new Font("宋体", Font.PLAIN, 15));

        tfDstMac = new JTextField();
        tfDstMac.setFont(new Font("宋体", Font.PLAIN, 15));
        tfDstMac.setColumns(10);

        tfSrcMac = new JTextField();
        tfSrcMac.setFont(new Font("宋体", Font.PLAIN, 15));
        tfSrcMac.setColumns(10);

        JLabel labelDstIp = new JLabel("\u76EE\u7684IP\u5730\u5740");
        labelDstIp.setFont(new Font("宋体", Font.PLAIN, 15));

        JLabel labelSrcIp = new JLabel("\u6E90IP\u5730\u5740");
        labelSrcIp.setFont(new Font("宋体", Font.PLAIN, 15));

        tfDstIp = new JTextField();
        tfDstIp.setFont(new Font("宋体", Font.PLAIN, 15));
        tfDstIp.setColumns(10);

        tfSrcIp = new JTextField();
        tfSrcIp.setFont(new Font("宋体", Font.PLAIN, 15));
        tfSrcIp.setColumns(10);


        ButtonGroup operationGroup = new ButtonGroup();

        JRadioButton requestChose = new JRadioButton("\u8BF7\u6C42\u62A5\u6587");
        requestChose.setFont(new Font("宋体", Font.PLAIN, 15));

        JRadioButton replyChose = new JRadioButton("\u5E94\u7B54\u62A5\u6587");
        replyChose.setFont(new Font("宋体", Font.PLAIN, 15));

        operationGroup.add(requestChose);
        operationGroup.add(replyChose);

        JButton btnSend = new JButton("\u53D1\u9001");
        btnSend.setFont(new Font("宋体", Font.PLAIN, 15));
        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = 1;
                String dstMac = (tfDstMac.getText() == null ) ? "" : tfDstMac.getText();
                String srcMac = (tfSrcMac.getText() == null ) ? "" : tfSrcMac.getText();
                String dstIp  = (tfDstIp.getText() == null ) ? "" : tfDstIp.getText();
                String srcIp  = (tfSrcIp.getText() == null ) ? "" : tfSrcIp.getText();
                if (requestChose.isSelected()){
                    i = 1;
                }else if (replyChose.isSelected()){
                    i = 2;
                }

                if (dstMac.equals("")){
                    dstMac = "ff:ff:ff:ff:ff:ff";
                    tfDstMac.setText(dstMac);
                }
                if (srcMac.equals("")){
                    srcMac = DeviceAccess.macToString(deviceAccess.getSender_device().mac_address);
                    tfSrcMac.setText(srcMac);
                }

                dispatch.addSendQueue(deviceAccess.sendARPPrepared(i,dstMac,dstIp,srcMac,srcIp));
            }
        });

        JLabel labelNetInterface_R = new JLabel("\u9009\u62E9\u7F51\u5361");
        labelNetInterface_R.setFont(new Font("宋体", Font.PLAIN, 15));

        JComboBox choseNetInterface_R = new JComboBox();
        choseNetInterface_R.setFont(new Font("宋体", Font.PLAIN, 15));
        choseNetInterface_R.setModel(new DefaultComboBoxModel(deviceAccess.getDeviceInfo()));
        choseNetInterface_R.setSelectedIndex(0);
        deviceAccess.pickRecvDevice(1);
        deviceAccess.openCurrentDevice();
        choseNetInterface_R.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("comboBoxChanged")){
                    deviceAccess.pickRecvDevice(choseNetInterface_R.getSelectedIndex() + 1);

                    deviceAccess.openCurrentDevice();
                }
            }
        });

        JButton btnStartRecv = new JButton("\u5F00\u59CB");
        btnStartRecv.setFont(new Font("宋体", Font.PLAIN, 15));

        JButton btnStopRecv = new JButton("\u505C\u6B62");
        btnStopRecv.setEnabled(false);
        btnStopRecv.setFont(new Font("宋体", Font.PLAIN, 15));

        btnStartRecv.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnStopRecv.setEnabled(true);
                btnStartRecv.setEnabled(false);
                if (e.getActionCommand().equals("开始")){
                    if (KEEPING == true){
                        loop.start();
                    }else {
                        loop = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (KEEPING){
                                    deviceAccess.start();
                                }
                            }
                        });
                        KEEPING = true;

                        loop.start();
                    }

                }
            }
        });
        btnStopRecv.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("停止")) {
                    KEEPING = false;
                    btnStartRecv.setEnabled(true);
                    btnStopRecv.setEnabled(false);
                }
            }
        });



        JButton btnClear = new JButton("\u6E05\u9664");
        btnClear.setFont(new Font("宋体", Font.PLAIN, 15));
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                No = 0;
                model.setRowCount(0);
            }
        });

        JButton btnQuit = new JButton("\u9000\u51FA");
        btnQuit.setFont(new Font("宋体", Font.PLAIN, 15));
        btnQuit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deviceAccess.closeSenderEngine();
                deviceAccess.closeCurrentDevice();
                dispatch.close();

                MainFrame.dispose();
            }
        });

        JScrollPane infoScrollPane = new JScrollPane();
        JCheckBox arpSelected = new JCheckBox("\u53EA\u663E\u793AARP\u5305");
        arpSelected.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (arpSelected.isSelected()){
                    ARPFILTER = true;
                }else {
                    ARPFILTER = false;
                }
            }
        });
        GroupLayout groupLayout = new GroupLayout(MainFrame.getContentPane());
        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addGap(111)
                                                .addComponent(btnStartRecv, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
                                                .addGap(40)
                                                .addComponent(btnStopRecv, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
                                                .addGap(46)
                                                .addComponent(btnClear, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED, 633, Short.MAX_VALUE)
                                                .addComponent(btnQuit, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addGap(30)
                                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                                        .addGroup(groupLayout.createSequentialGroup()
                                                                .addComponent(labelNetInterface_S)
                                                                .addGap(18)
                                                                .addComponent(choseNetInterface_S, GroupLayout.PREFERRED_SIZE, 888, GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(groupLayout.createSequentialGroup()
                                                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                                                                        .addGroup(groupLayout.createSequentialGroup()
                                                                                .addComponent(labelSrcMac, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(18)
                                                                                .addComponent(tfSrcMac))
                                                                        .addGroup(groupLayout.createSequentialGroup()
                                                                                .addComponent(labelDstMac)
                                                                                .addGap(18)
                                                                                .addComponent(tfDstMac, GroupLayout.PREFERRED_SIZE, 292, GroupLayout.PREFERRED_SIZE)))
                                                                .addGap(18)
                                                                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                                                                        .addComponent(labelDstIp, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(labelSrcIp, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE))
                                                                .addGap(18)
                                                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                                                                        .addComponent(tfSrcIp)
                                                                        .addComponent(tfDstIp, GroupLayout.PREFERRED_SIZE, 312, GroupLayout.PREFERRED_SIZE)))
                                                        .addGroup(groupLayout.createSequentialGroup()
                                                                .addComponent(labelNetInterface_R, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18)
                                                                .addComponent(choseNetInterface_R, GroupLayout.PREFERRED_SIZE, 885, GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18)
                                                                .addComponent(arpSelected))
                                                        .addComponent(infoScrollPane, GroupLayout.PREFERRED_SIZE, 1103, GroupLayout.PREFERRED_SIZE))))
                                .addContainerGap(51, GroupLayout.PREFERRED_SIZE))
                        .addGroup(groupLayout.createSequentialGroup()
                                .addGap(102)
                                .addComponent(requestChose)
                                .addGap(111)
                                .addComponent(replyChose, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED, 506, Short.MAX_VALUE)
                                .addComponent(btnSend)
                                .addGap(232))
        );
        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addGap(26)
                                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(labelNetInterface_S)
                                        .addComponent(choseNetInterface_S, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(18)
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                                        .addComponent(labelDstMac, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(tfDstMac, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addGap(18)
                                                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                                                        .addComponent(labelSrcMac, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(tfSrcMac, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addGap(18)
                                                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                                        .addComponent(replyChose, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(requestChose)
                                                        .addComponent(btnSend)))
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                                        .addComponent(tfDstIp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(labelDstIp, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
                                                .addGap(18)
                                                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                                        .addComponent(tfSrcIp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(labelSrcIp, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))))
                                .addGap(18)
                                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(labelNetInterface_R, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(choseNetInterface_R, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(arpSelected))
                                .addGap(18)
                                .addComponent(infoScrollPane, GroupLayout.PREFERRED_SIZE, 171, GroupLayout.PREFERRED_SIZE)
                                .addGap(14)
                                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(btnStartRecv)
                                        .addComponent(btnStopRecv, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnClear, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnQuit, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        infoTable = new JTable();
        model = new DefaultTableModel(
                new Object[][] {
                },
                new String[] {
                        "\u7F16\u53F7", "\u534F\u8BAE\u7C7B\u578B", "\u65F6\u95F4\u6233", "\u957F\u5EA6", "\u6E90IP\u5730\u5740", "\u6E90MAC\u5730\u5740", "\u76EE\u7684IP\u5730\u5740", "\u76EE\u7684MAC\u5730\u5740"
                }
        );
        infoTable.setModel(model);
        infoTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        infoTable.getColumnModel().getColumn(1).setPreferredWidth(65);
        infoTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        infoTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        infoTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        infoTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        infoTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        infoTable.getColumnModel().getColumn(7).setPreferredWidth(90);
        infoTable.setFont(new Font("宋体", Font.PLAIN, 15));
        infoTable.setFillsViewportHeight(true);
        infoScrollPane.setViewportView(infoTable);
        MainFrame.getContentPane().setLayout(groupLayout);
    }

    public void start(){
        MainFrame.setVisible(true);
    }

    public synchronized void renderingPacket(Packet packet){

        FramePacket temp = (FramePacket) packet;

        String [] row = new String[8];

        if (temp.getType().equals("0806")) {

            ARPPacket x = new ARPPacket(temp);

            row[0] = String.valueOf(No++);
            row[1] = x.getType();
            row[2] = String.valueOf(x.sec);
            row[3] = String.valueOf(x.len);
            row[4] = x.getSrc_ip();
            row[5] = x.getSrc_mac();
            row[6] = x.getDst_ip();
            row[7] = x.getDst_mac();

            model.addRow(row);
            return;
        }
        if (ARPFILTER){
            return;
        }else {
            row[0] = String.valueOf(No++);
            row[1] = temp.getType();
            row[2] = String.valueOf(temp.sec);
            row[3] = String.valueOf(temp.len);
            row[4] = "";
            row[5] = temp.getSrc_mac();
            row[6] = "";
            row[7] = temp.getDst_mac();
            model.addRow(row);
        }
    }
}
