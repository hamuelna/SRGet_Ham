import java.io.File;

/**
 * Created by Hamuel on 1/14/16.
 */
public class TD {
    public static void main(String[] args) {
        //mainDL test01 = new mainDL("http://docs.oracle.com/javase/8/docs/api/overview-summary.html", "java8doc.txt");
        //test01.newDL();
//        TD test12 = new TD();
//        test12.startDL("http://pngimg.com/upload/chicken_PNG2145.png", "chicken.png");

        //TD test22 = new TD();
        //test22.startDL("http://n467us.com/Data%20Files/Seattle%20Sectional%20South.jpg", "bigimg2.jpg");
        //mainDL test03 = new mainDL("https://www.httpwatch.com/httpgallery/chunked/chunkedimage.aspx", "testimg.aspx");
        //test03.newDL();
        //TestSer.testFile();
        //test Resume
//        chkDL test411 = new chkDL("http://n467us.com/Data%20Files/Seattle%20Sectional%20South.jpg");
//        concurDL test41 = new concurDL("http://n467us.com/Data%20Files/Seattle%20Sectional%20South.jpg","TestIMG_C.jpg", 5);

        chkDL test42 = new chkDL("http://cs.muic.mahidol.ac.th/~ktangwon/bigfile.xyz");
        concurDL test421 = new concurDL("http://cs.muic.mahidol.ac.th/~ktangwon/bigfile.xyz","bigFileC" , 3);
    }

    public void startDL(String url, String filename){
        mainDL DL = new mainDL(url, filename);
        File headFile = new File(filename+".HEAD");
        File dataFile = new File(filename+".DATA");
        if (headFile.exists() && dataFile.exists()){
            DL.startResume(headFile, dataFile);
        }else {
            DL.newDL();
        }

    }
}
