
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author behnam
 */
public class Test {
    public static void main(String [] args) throws IOException, NoSuchAlgorithmException{
        System.out.println(Arrays.toString(SHA256("Hello".getBytes())).length());
    }
    
    public static String getFileNameWithoutExtension(String fname){
        int pos = fname.lastIndexOf(".");
        if (pos > 0) {
            fname = fname.substring(0, pos);        
        }
        return fname;
    }
    
    public static byte [] SHA256(byte[] b) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(b); // Change this to "UTF-16" if needed
        return md.digest();
    }
    
    
}


