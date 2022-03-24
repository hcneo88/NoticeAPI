package org.eservice.notice.component;

import java.awt.Desktop;
import java.io.*;

import org.springframework.stereotype.Component;

@Component
public class GenerateHTML {
    
    public static void showHTML() throws Exception {
        File f = new File("source.htm");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write("<html><body><h1>Blah, Blah!</h1>");
        bw.write("<textarea cols=75 rows=10>");
        for (int ii=0; ii<20; ii++) {
            bw.write("Blah blah..");
        }
        bw.write("</textarea>");
        bw.write("</body></html>");
        bw.close();

        Desktop.getDesktop().browse(f.toURI());

    }

    /*
    public static void main(String[] args) throws Exception {
        GenerateHTML.showHTML();
    }
    */
}
