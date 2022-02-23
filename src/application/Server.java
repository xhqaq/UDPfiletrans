package application;

import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import core.*;
import service.*;
public class Server {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("启动服务端");
        String baseDir = "D:\\receivefolder\\";
        System.out.println(baseDir);
        System.out.println("请输入port");
        //int port = scanner.nextInt();
        int port = 9527;
        //System.out.println("请输入size");
       // int size = scanner.nextInt();

        try {
            UDPFileServer server = new UDPFileServer(port);
            while (true){
                System.out.println("等待接收");
                server.receiveFile(baseDir + File.separator+ "receive" + File.separator);
                System.out.println("收到文件:"+ server.getFileName());
                /*File file = new File(baseDir + File.separator+ "receive" + File.separator+server.getFileName());
                if (!file.getParentFile().exists()){
                    file.getParentFile().mkdir();
                }
                FileOutputStream fo = new FileOutputStream(file);
                fo.write(server.getFileData());
                fo.close();*/
                System.out.println("是否退出(Y/N)?");

                if ("y".equalsIgnoreCase(scanner.next())){
                    break;
                }
            }
            server.close();
            System.out.println("服务器已关闭");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
