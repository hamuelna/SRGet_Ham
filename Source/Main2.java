

import com.sun.xml.internal.ws.encoding.MtomCodec;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Hamuel on 1/14/16.
 */
public class Main2 {
    static String servHost;
    static int port;
    static String path;
    static String FileName;
    static String aUrl;

    public static void Second(String url) {
        //keep important header data
        KeepData rs = new KeepData();
        byte[] TotalByte = new byte[2048];

        aUrl = url;
        //extracting URL Data
        try {
            URL TheUrl = new URL(url);
            servHost = TheUrl.getHost();
            if (TheUrl.getPort() == -1){
                port = 80;
            }else {
                port = TheUrl.getPort();
            }
            path = TheUrl.getPath();

        } catch (MalformedURLException e) {
            System.out.println("The URL format is incorrect");
            //e.printStackTrace();
        }

        FileName = servHost.split("\\.")[1];
        File fs = new File(FileName+".ser");
        //Insert Socket Detail and open the socket
        SocketAddress servAdr = new InetSocketAddress(servHost, port);
        Socket s = new Socket();

        //init values to track how much have we downloaded the data
        int totalByte = 0;
        int currentByte = 0;
        int headerSize = 0;
        boolean startRecv = false;
        String contentLength = null;
        StringBuilder Data = new StringBuilder();
        StringBuilder headerContent = new StringBuilder();
        boolean isResume = false;
        boolean ResumeChk = false;

        try {
            //Read the file if the resumable file exist
            if (fs.exists()) {

                FileInputStream FileIn = new FileInputStream(FileName + ".hammy");
                FileIn.read();
                totalByte = rs.getByte();
                Data = rs.getData();
                isResume = true;
            }

            s.connect(servAdr); //connect the socket to server
            PrintWriter out = new PrintWriter(s.getOutputStream(), true); //open a stream to write data to send to socket

            if (isResume){
                out.println(HelperFX.getResumeReq(servHost, path, rs.getByte())); //ser file exist therefore send resume request
            }else {
                out.println(HelperFX.getDLRequest(servHost, path)); // send request to server to download
            }
            s.setReceiveBufferSize(1024);

            while (true){
                byte[] bb = new byte[1024];
                currentByte = s.getInputStream().read(bb); //store byte in bb and byte number in currentByte
                totalByte += currentByte; //add currentByte to the total Byte
                String stt = new String(bb, StandardCharsets.UTF_8); //convert byte to String
                //check that the header have been read already or not if yes start appending data
                if (startRecv){
                    Data.append(stt);
                    Data.append("");
                }
                //create file while downloading
                FileOutputStream DL_ing = new FileOutputStream(FileName+".hammy"); //create new file for storing progress
                DL_ing.write(bb);
                //header manipulation to get Content-Length in Byte
                if (stt.contains("Content-Length") && !startRecv){
                    String xx[] = stt.split("\n");
                    boolean headerExtracted = false;
                    for (String x: xx ){
                        if (!startRecv) {
                            headerSize += x.getBytes().length + 1; //calculating total headSize
                            headerContent.append(x + "\n"); //keep header content
                        }
                        if (headerExtracted){
                            Data.append(x+ "\n"); //Keep Data that is not a part of the header
                        }
                        if (x.contains("Content-Length") && !isResume){
                            contentLength = x.split(": ")[1]; //extract content length in Byte
                            rs.storeContentLength(x.split(": ")[1].replace("\r", ""));
                        }
                        if (x.contains("Last-Modified") && !isResume){
                            rs.storeDate(x.split(": ")[1].replace("\r","" )); //store the data to check if the file change or not
                        }
                        if (x.contains("ETag:") && !isResume){
                            rs.storeTag(x.split(": ")[1].replace("\r","")); //store the tag to check if it is the same file
                        }

                        if (x.contains("Last-Modified") && isResume){
                            //not finish but basicly check that the file have been modified or not
                           LocalDateTime.parse(x.split(": ")[1].replace("\r",""), DateTimeFormatter.RFC_1123_DATE_TIME).isEqual(rs.getDate());
                        }

                        if (x.contains("ETag: ") && isResume){
                            //not finish but check that the tag of the file is the same or not
                            boolean bbb = rs.getTag().equals(x.split(": ")[1].replace("\r",""));
                        }
                        //detect the head of the header
                        if (x.equals("\r")){
                            startRecv = true;
                            headerExtracted = true;
                       }
                        //check the header if the content can resume or not
                        if (isResume && x.contains("206 Partial Content")){

                            System.out.println("Start Resuming!!!");
                            ResumeChk = true;
                        }

                    }
                }
                //Keep Data in case of resume
                rs.storeByte(totalByte);
                rs.storeData(Data);

                //if cannot resume we delete the ser file and download all over from start
                if (isResume && !ResumeChk){
                    System.out.println("This URL does not support resume therefore we will overwrite instead");
                    fs.delete();
                    Second(url);
                    break;
                }

                if (isResume){
                    if (startRecv && (totalByte - headerSize >= rs.getContentLength())){
                        DL_ing.close();
                        FinishConnection(fs, s, Data, headerContent, totalByte, Integer.toString(rs.getByte()), headerSize);
                        break;
                    }
                }else {
                    if (startRecv && (totalByte - headerSize >= Integer.parseInt(contentLength.replace("\r", "")))){
                        DL_ing.close();
                        FinishConnection(fs, s, Data, headerContent, totalByte, contentLength, headerSize);
                        break;
                    }
                }

            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public static void FinishConnection(File fs, Socket s, StringBuilder Data,
                                        StringBuilder headerContent, int totalByte, String contentLength, int headerSize) throws IOException{
        BufferedWriter output = new BufferedWriter(
                new FileWriter(String.format("%s.txt", FileName))); //create a new file to keep "data"
        boolean SerDel = fs.delete();
        System.out.println("<-----Below is Header Content ----->");
        System.out.println(headerContent);
        System.out.println("<-----Below is Data Content ----->");
        System.out.println(Data);
        output.write(Data.toString());
        System.out.println(String.format("The total amount of data recieve is %d byte ", totalByte ));
        System.out.println(String.format("The header size is %d", headerSize) );
        System.out.println("The actual content length is: " + contentLength);
        System.out.println(String.format("Total Data Content is %d byte", totalByte - headerSize));
        System.out.println("Download Completed!");
        output.close();
        s.close();

    }



}
