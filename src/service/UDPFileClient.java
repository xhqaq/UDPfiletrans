package service;

import core.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

/**
 * 文件发送客户端
 */
public class UDPFileClient {

    private InetAddress address;

    private int port;
    /**
     * 包大小
     */
    private int packetSize;
    /**
     * 文件包
     */
    private byte[] packetBuf;

    private byte[] data;
    /**
     * 名称长度值占用4字节
     * 文件长度值占用8字节
     */
    private byte[] fileInfoBuf = new byte[30];

    /**
     * 文件名 packet
     */

    private DatagramPacket fileNameLenPacket;
    /**
     * 文件长度 packet
     */
    private DatagramPacket fileLenPacket;
    /**
     * 文件数据 packet
     */
    private DatagramPacket sizePacket;
    private DatagramPacket ipPacket;
    private DatagramPacket pklengthPacket;
    private DatagramPacket codePacket;
    private DatagramSocket socket = new DatagramSocket();
    private DatagramPacket fileinfoPacket;

    private byte[] codeddata ;
    private byte[] pklength;

    private String path;

    private static boolean completed;
    /**
     * 初始化文件发送
     * @param hostname 目标主机名称
     * @param port 目标端口
     * @param packetSize 封包大小
     * @throws UnknownHostException
     * @throws SocketException
     */
    public UDPFileClient(String hostname, int port,int packetSize,String path) throws UnknownHostException, SocketException {

        this.address = InetAddress.getByName(hostname);

        this.port = port;
        this.packetSize = packetSize;
        //this.data = new byte[this.packetSize];

        //this.packetBuf = new byte[this.packetSize+10];
        this.path = path;
        // 文件名长度值用4字节

        //this.fileNameLenPacket = new DatagramPacket(this.fileInfoBuf,4,this.address,port);
        // 文件长度使用8字节
        //this.fileLenPacket = new DatagramPacket(this.fileInfoBuf,4,8,this.address,port);

        //this.sizePacket = new DatagramPacket(this.fileInfoBuf,12,4,this.address,port);
        // 文件使用512字节分段发送
        //this.ipPacket = new DatagramPacket(fileInfoBuf,16,4,this.address,port);

        this.fileinfoPacket = new DatagramPacket(this.fileInfoBuf,30,this.address,port);


        this.codeddata = new byte[packetSize+130];
        this.codePacket = new DatagramPacket(codeddata,packetSize+130,this.address,port);

        this.completed = false;
        //this.pklength = new byte[4];

        //this.pklengthPacket = new DatagramPacket(pklength,4,this.address,port);
    }

    /**
     * udp 包顺序: 1、文件名长度值 2、文件长度值 3、文件名 4、文件内容
     *  filePath 文件路径
     * @throws IOException
     */
    public void start(String pathname) throws Exception{
        Reader reader = new Reader(port+1);////
        reader.start();
        sendFile(pathname);
    }
    public class Reader extends Thread {
        private byte[] complete= new byte[4];
        private DatagramPacket completePacket;
        private DatagramSocket socket2;
        public Reader(int port2)throws Exception{
            this.socket2 = new DatagramSocket(port2);
            completePacket = new DatagramPacket(complete,4);
        }
        public void run(){
            while(!socket.isClosed()){
                try {
                    socket2.receive(completePacket);
                    if(bytesToInt(complete)==5555){
                        completed = true;
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
            socket2.close();
        }

    }
    /**将给定的用十进制分段格式表示的ipv4地址字符串转换成字节数组*/
    public static byte[] ipv4Address2BinaryArray(String ipAdd){
        byte[] binIP = new byte[4];
        String[] strs = ipAdd.split("\\.");
        for(int i=0;i<strs.length;i++){
            binIP[i] = (byte) Integer.parseInt(strs[i]);
        }
        return binIP;
    }
    public static int bytesToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
    public static byte[] intToBytes(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }
    private static ByteBuffer buffer = ByteBuffer.allocate(8);
    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }
    //public static final String HEADER_TOKEN = "KS";
    public static byte[] code(byte[] b) throws Exception{
        byte[] coded = new byte[b.length+10];
        byte[] head ={0x12,0x13};
        System.arraycopy(head,0,coded,0,2);

        Checksum checksum = new Adler32();
        checksum.update(b, 0, b.length);
        System.arraycopy(b,0,coded,2,b.length);
        System.arraycopy(longToBytes(checksum.getValue()),0,coded,b.length+2,8);
        return coded;

        //return b;
    }
    private void transmit(EncodedPacket packet) {
//		    filename = "/" + filename;
        byte[] data = packet.toByteArray();

        try {

            byte[] datalength =intToBytes(data.length);

            byte[] data0 = new byte[packetSize+120];
            System.arraycopy(datalength,0,data0,0,4);
            System.arraycopy(data,0,data0,4,data.length);


            byte[] data1=code(data0);



            System.arraycopy(data1,0,codeddata,0,data1.length);
            System.out.println(data1.length);
            socket.send(codePacket);

           // dos.write(data1,0,data.length+10);
           // dos.flush();
            //Thread.sleep(2);
        } catch (Exception e1) {
            // TODO Auto-generated catch block

           // logger.error("transmiterror",e1);
        }//client.getOutputStream()返回此套接字的输出流
    }
    public void sendFile(String filePath) throws Exception {
        // 读取系统文件

        File file = new File(filePath);
        byte[] fileBuf = new byte[(int)file.length()];


        byte[] readBuf = new byte[2048];
        int readLen,staPos = 0;
        FileInputStream inputStream =new FileInputStream(file);
        while ((readLen = inputStream.read(readBuf))!=-1){
            System.arraycopy(readBuf,0,fileBuf,staPos,readLen);
            staPos += readLen;
        }
        // 发送文件名长度值和文件长度值
        InetAddress addr = InetAddress.getLocalHost();
        byte[] add = ipv4Address2BinaryArray(addr.getHostAddress());

        byte[] fileinfomation = new byte[20];

        System.arraycopy(intToBytes(file.getName().getBytes().length),0,fileinfomation,0,4);
        System.arraycopy(longToBytes(file.length()),0,fileinfomation,4,8);
        System.arraycopy(intToBytes(packetSize),0,fileinfomation,12,4);
        System.arraycopy(add,0,fileinfomation,16,4);
        byte[] codefileinfo = code(fileinfomation);
        System.arraycopy(codefileinfo,0,this.fileInfoBuf,0,30);
        //socket.send(fileNameLenPacket);
        //socket.send(fileLenPacket);
        //socket.send(sizePacket);
        //socket.send(ipPacket);
        socket.send(fileinfoPacket);
        // 发送文件名
        DatagramPacket fileNamPacket = new DatagramPacket(file.getName().getBytes(),file.getName().getBytes().length,address,port);
        socket.send(fileNamPacket);

        // 发送文件




        Encoder enc = new Encoder(fileBuf, packetSize);
        enc.encode(new Encoder.Callback() {
            int readIndex = 0;
            public boolean call(Encoder encoder, EncodedPacket packet) {
                // int i=FileController.pause;
                transmit(packet);
                //System.out.println(readIndex);
                readIndex++;
                // System.out.println(completed+"dddd");
                return completed;
            }
        });

    }
    public void close(){
        this.socket.close();
    }




}

