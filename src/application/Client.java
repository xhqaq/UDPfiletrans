package application;

import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import service.*;


public class Client {
    public static int maxd;
    public static void main(String[] args){
        maxd=0;
        Scanner scanner = new Scanner(System.in);
        System.out.println("启动客户端");
        System.out.println("请输入ip");
        //String ip = scanner.nextLine();
        String ip = "127.0.0.1";
        System.out.println("请输入port");
        //int port = scanner.nextInt();
        int port = 9527;
        System.out.println("请输入size");
       // int size = scanner.nextInt();
        int size;
        System.out.println("请输入文件路径");
        //String pathname = scanner.next();
        String pathname = "D:\\50\\0.jpg";
        try {
            File file1 = new File(pathname);
            long filelength = file1.length();
            if(filelength>1372917){
                size = 65377;
            }
            else{
                size = (int)(filelength/21);
            }
            UDPFileClient client = new UDPFileClient(ip,port,size,pathname);
            //client.sendFile(pathname);
            client.start(pathname);
            client.close();
            System.out.println("发送完成");
            System.out.println(maxd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
