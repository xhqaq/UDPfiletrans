package service;

import core.*;

import jdk.jshell.execution.Util;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.Adler32;
import java.util.zip.Checksum;
/**
 * 文件接收服务器
 */
public class UDPFileServer {

    /**
     * socket 缓冲区
     */
    private DatagramSocket socket;

    private DatagramSocket socket2;
    /**
     * 文件包数据大小
     */
    private int packetSize;
    /**
     * 名称长度值占用4字节
     * 文件长度值占用8字节
     */
    private byte[] fileInfo = new byte[30];
    /**
     * packet 缓冲区
     */
    private byte[] packetBuf;
    private byte[] byteBuf;
    private byte[] complete;
    private byte[] codedata;
    //private byte[] pklength;
    /**
     * 文件名长度值用4字节缓冲区接收 int 值
     */
    private DatagramPacket fileNameLenPacket;
    /**
     * 文件长度值使用8字节缓冲区接收 long 值
     */
    private DatagramPacket fileLenPacket;
    /**
     * 文件数据封包使用512字节缓冲区分段接收
     */
    private DatagramPacket sizePacket;
    private DatagramPacket filePacket;

    private DatagramPacket bytePacket;

    private DatagramPacket completePacket;
    private DatagramPacket codePacket;
    //private DatagramPacket pklengthPacket;
    private DatagramPacket ipPacket;
    private DatagramPacket fileinfoPacket;
    private int port;
    /**
     * 接收到的文件名
     */
    private String fileName;
    /**
     * 接收到的文件内容
     */
    private byte[] fileData;
    private boolean completed;
    private InetAddress address;
    /**
     * 初始化文件接收UDP服务
     *
     * @param port 监听端口
  //   * @param packetSize 封包大小
     * @throws SocketException
     */

    private Decoder dec;
    public UDPFileServer(int port) throws UnknownHostException,SocketException {
        this.socket2 = new DatagramSocket();
        this.socket = new DatagramSocket(port);
        //this.packetSize = packetSize;
       // this.packetBuf = new byte[packetSize+100];
        this.byteBuf = new byte[1];
        this.complete = new byte[4];
        this.port = port;
        this.completed = false;


        //fileNameLenPacket = new DatagramPacket(fileInfo,  4);
        //fileLenPacket = new DatagramPacket(fileInfo,  4,8);
        //sizePacket = new DatagramPacket(fileInfo,12,4);
        //ipPacket = new DatagramPacket(fileInfo,16,4);
        fileinfoPacket = new DatagramPacket(fileInfo,30);
        //filePacket = new DatagramPacket(packetBuf,  this.packetSize+100);
        bytePacket = new DatagramPacket(byteBuf,1);
        //this.pklength = new byte[4];
        //this.pklengthPacket = new DatagramPacket(pklength,4);

    }

    /**
     * 接收文件，
     * udp 包顺序: 1、文件名长度值 2、文件长度值 3、文件名 4、文件内容
     *
     * @return 文件内容字节数组
     * @throws IOException
     */
    private static ByteBuffer buffer = ByteBuffer.allocate(8);
    public static int bytesToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
    public static long bytesToLong(byte[] bytes) {
        buffer.clear();
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getLong();
    }
    public static byte[] intToBytes(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }
    /**将给定的字节数组转换成IPV4的十进制分段表示格式的ip地址字符串*/
    public static String binaryArray2Ipv4Address(byte[] addr){
        String ip="";
        for(int i=0;i<addr.length;i++){
            ip+=(addr[i]&0xFF)+".";
        }
        return ip.substring(0, ip.length()-1);
    }
    //public static final String HEADER_TOKEN = "KS";
    private static byte[] head ={0x12,0x13};
    public static byte[] decode1(byte[] b) throws Exception{

        byte[] headerToken = new byte[2];

        System.arraycopy(b,0,headerToken,0,2);
        //System.out.println(Arrays.toString(headerToken));
        //System.out.println(Arrays.toString(head));
        if(!Arrays.equals(head, headerToken)){
            System.out.println(401);
            return null;
        }
        byte[] jvalue = new byte[8];
        byte[] data = new byte[b.length-10];
        System.arraycopy(b,2,data,0,b.length-10);
        System.arraycopy(b,data.length+2,jvalue,0,8);
        long jiaoyan = bytesToLong(jvalue);
        Checksum checksum = new Adler32();
        checksum.update(data, 0, data.length);
        if(jiaoyan!=checksum.getValue()){
            System.out.println(403);
           // return null;
        }

        return data;
       // return data;*/
        //return b;
    }
    public void sendcomplete(int data)throws Exception{
        System.arraycopy(intToBytes(data),0,complete,0,4);
        socket2.send(completePacket);
    }
    public void receiveFile(String receivedir) throws Exception {
        try {
            // 读取文件名长度
            //this.socket.receive(fileNameLenPacket);
            //System.out.println(fileNameLenPacket.getAddress());
            //System.out.println(fileNameLenPacket.getPort());


            // 读取文件长度
            //this.socket.receive(fileLenPacket);
            //读取包大小
            //this.socket.receive(sizePacket);
            //读取对方ip
            //this.socket.receive(ipPacket);
            this.socket.receive(fileinfoPacket);
            byte[] codedfileinfomation = new byte[30];
            System.arraycopy(this.fileInfo, 0, codedfileinfomation, 0, 30);
            byte[] fileinfomation = decode1(codedfileinfomation);
            System.out.println(fileinfomation.length);

            // 取回文件名
            byte[] fileNameBuf = new byte[bytesToInt(fileinfomation)];
            DatagramPacket fnPacket = new DatagramPacket(fileNameBuf, fileNameBuf.length);
            this.socket.receive(fnPacket);

            this.fileName = new String(fileNameBuf);
            // 建立文件缓冲区,读取文件内容到缓冲区

            byte[] filelength = new byte[8];
            System.arraycopy(fileinfomation, 4, filelength, 0, 8);

            int fileLen = (int) bytesToLong(filelength);
            byte[] psize = new byte[4];
            System.arraycopy(fileinfomation, 12, psize, 0, 4);
            this.packetSize = bytesToInt(psize);
            byte[] addr = new byte[4];
            System.arraycopy(fileinfomation, 16, addr, 0, 4);
            String ip = binaryArray2Ipv4Address(addr);

            //String hostname = "127.0.0.1";
            //this.address = InetAddress.getByName(hostname);
            this.completePacket = new DatagramPacket(complete, 4, InetAddress.getByName(ip), this.port + 1);////
            System.out.println(ip);
            //this.packetBuf = new byte[packetSize+10];

            //filePacket = new DatagramPacket(packetBuf,  this.packetSize+10);
            //byte codeddata[] = new  byte[this.packetSize+10];
            // byte data[] = new byte[this.packetSize];
            this.fileData = new byte[fileLen];
            // int codepklength;
            //byte[] cplength = new byte[4];
            // int writePos = 0;

        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            int num = 0;
            this.dec = new IncrementalDecoder(packetSize);
            System.out.println(num);
            while (!completed){
                System.out.println(num);
                //socket.receive(pklengthPacket);
                //System.arraycopy(pklength,0,cplength,0,4);
                //codepklength = bytesToInt(cplength);
                this.codedata = new byte[packetSize+130];
                System.out.println(codedata.length);
                this.codePacket = new DatagramPacket(codedata,packetSize+130);


                byte[] codeddata = new byte[packetSize+130];
               // System.out.println(num);
                socket.setSoTimeout(2000);
                socket.receive(codePacket);
                //System.out.println(num);
                System.arraycopy(codedata,0,codeddata,0,packetSize+130);
                byte[] data1 = decode1(codeddata);
                byte[] datalength = new byte[4];
                System.arraycopy(data1,0,datalength,0,4);
                int dlength = bytesToInt(datalength);
                byte[] data0 =new byte[dlength];
                System.arraycopy(data1,4,data0,0,dlength);
               // System.out.println(num);
                num++;
                EncodedPacket ep = new EncodedPacket(data0);
                DecodedPacket dp = ep.decode();
                if(dp!=null) {
                    completed = dec.receive(dp);
                }

            }

            System.out.println(receivedir+fileName);
            dec.write(new FileOutputStream(receivedir+fileName));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            dec.write(bos);
            sendcomplete(5555);
            socket2.close();
            System.out.println("接收完成");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void close() {
        this.socket.close();
    }


    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }


}

