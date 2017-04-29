import java.awt.CardLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
//import javax.imageio.ImageIO;
//import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.lang.System;
//import javax.swing.JPasswordField;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author behnam
 */
public class MainFrame {
    // Size of buffer
    private static int default_buffer_size = 1000;
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws IOException, NoSuchAlgorithmException {
        if(args.length >= 3){
            switch (args[0]) {
                case "-ef":
                    System.out.println(Cryptor.encrypt_file(args[2], args[1], (args.length == 3)?  default_buffer_size : Integer.parseInt(args[3])));
                    return;
                case "-ed":
                    System.out.println(Cryptor.encrypt_dir(args[2], args[1], (args.length == 3)?  default_buffer_size : Integer.parseInt(args[3])));
                    return;
                case "-df":
                    System.out.println(Cryptor.decrypt_file(args[2], args[1], (args.length == 3)?  default_buffer_size : Integer.parseInt(args[3])));
                    return;
                case "-dd":
                    System.out.println(Cryptor.decrypt_dir(args[2], args[1], (args.length == 3)?  default_buffer_size : Integer.parseInt(args[3])));          
                    return;
                default:
                    break;
            }
        }
        String help = "Command line usage examples:\n"
                + "Encrypt directory:\n     "
                + "java -jar Kryptonite.jar -ed /PATH/TO/DIR /PATH/TO/KEY\n\n"
                + "Encrypt file:\n     "
                + "java -jar Kryptonite.jar -ef /PATH/TO/FILE /PATH/TO/KEY\n\n"                
                + "Decrypt directory:\n     "
                + "java -jar Kryptonite.jar -dd /PATH/TO/DIR /PATH/TO/KEY\n\n"
                + "Decrypt file:\n     "
                + "java -jar Kryptonite.jar -df /PATH/TO/FILE /PATH/TO/KEY\n\n";

        System.out.println(help);

    }
}
