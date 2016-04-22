
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author          Behnam Azizi
 * @date            Feb. 15, 2015
 * @description     This class is used to encrypting and decrypting files
 */

public class Cryptor {
    private static FileIO file;
    private static final byte[] encryption_stamp = "10a34637ad661d98ba3344717656fcc76209c2f8".getBytes();
    
    public static void main(String args[]) throws IOException, NoSuchAlgorithmException{
        Cryptor.encrypt_file("x35", "test.txt");
        Cryptor.decrypt_file("x35", "test.txt");
        //Cryptor.encrypt_dir("xes", "/home/behnam/virusLab");
        //Cryptor.decrypt_dir("xes", "/home/behnam/virusLab");

    }
    
    public static byte[] encrypt_bytes(byte[] key, byte[] b){
        byte[] output = new byte[b.length];
        for(int i=0; i<b.length; i++){
            output[i] = (byte) (key[i % key.length] ^ b[i]);
        }

        return output;
    }
    
    
    public static byte[] decrypt_bytes(byte[] key, byte[] b){
        byte[] output = new byte[b.length];
        for(int i=0; i<b.length; i++){
            output[i] = (byte) (key[i % key.length] ^ b[i]);
        }
        return output;
    }
    
    public static String encrypt_file_name(String file_name){
        String keys = "abcdefghijklmnopqr.stuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
        String vals = "vFPBRMCagNrfGlnLYyHJqOoewxkIbsTKpAVQSiu_jmEzWhc.UtdXZD";
        String result = "";
        for (int i=0; i<file_name.length(); i++){
            for(int j=0; j<keys.length(); j++){
                if(file_name.charAt(i) == keys.charAt(j)){
                    result += vals.charAt(j);
                    break;
                }else if(j == keys.length() - 1){
                    result += file_name.charAt(i);                    
                }

            }
            
        }
//        System.out.println(result);
        return result;
    }
    
    public static String decrypt_file_name(String file_name){
        String keys = "vFPBRMCagNrfGlnLYyHJqOoewxkIbsTKpAVQSiu_jmEzWhc.UtdXZD";
        String vals = "abcdefghijklmnopqr.stuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
        String result = "";
        for (int i=0; i<file_name.length(); i++){
            for(int j=0; j<keys.length(); j++){
                if(file_name.charAt(i) == keys.charAt(j)){
                    result += vals.charAt(j);
                    break;
                }else if(j == keys.length() - 1){
                    result += file_name.charAt(i);                    
                }
            }
        }
//        System.out.println(result);
        return result;
    }
    
    public static boolean are_bytes_encrypted(byte[] b) throws NoSuchAlgorithmException{
        //System.out.println(Arrays.copyOfRange(b, 0, encryption_stamp.length));
        //System.out.println(encryption_stamp);
        return b.length >= encryption_stamp.length + SHA256("".getBytes()).length && Arrays.equals(Arrays.copyOfRange(b, 0, encryption_stamp.length), encryption_stamp);
    }

    
    public static String encrypt_dir(String key, String dir_path) throws IOException, NoSuchAlgorithmException{

        String message = "";
        ArrayList<File> files = DirWalker.walk(dir_path);
        
        for(File fl: files){
            message += Cryptor.encrypt_file(key, fl.getAbsolutePath());
        }
        return message;
    }

    public static String decrypt_dir(String key, String dir_path) throws IOException, NoSuchAlgorithmException{
        String message = "";
        ArrayList<File> files = DirWalker.walk(dir_path);
        for(File fl: files){
            message += Cryptor.decrypt_file(key, fl.getAbsolutePath());
        }
        return message;        
    }
    
    public static String encrypt_file(String key, String file_path) throws IOException, NoSuchAlgorithmException{        
        //Read encrypted file in bytes (contains digital signature + stamp)
        file = new FileIO(file_path);
        
        //all bytes read from file
        byte[] file_bytes = file.read();
                
        
        //all bytes - signature - stamp
        byte[] encrypted_bytes = null;
        
        //digital signature
        byte[] digital_signature = SHA256(file_bytes);
//        System.out.println("Digital signature is being written: " + Arrays.toString(digital_signature));
        
        
        //Convert user-entered key to bytes
        byte[] key_bytes = key.getBytes();
        
        
        //This byte-array will contain the output to be written on the encrypted file
        byte[] output = new byte[SHA256(file_bytes).length + encryption_stamp.length + file_bytes.length];

        //Copy encryption_stamp to the beginning of file to be written
        System.arraycopy(encryption_stamp, 0, output, 0, encryption_stamp.length);

        //Copy the digital signature right after the stamp
        System.arraycopy(digital_signature, 0, output, encryption_stamp.length, digital_signature.length);
        
        
        //Make sure this file is not already encrypted
        if(are_bytes_encrypted(file_bytes)){
            FileIO.log(file_path + " is already encrypted");
            return "Error:" + file_path + " is already encrypted\n";
        }

        //output = encryption_stamp + encrypted_bytes
        //encrypt file bytes
        encrypted_bytes = encrypt_bytes(key_bytes, file_bytes);

        //add encrypted bytes to the end output to be written
        //output = stamp + signature + encrypted bytes
        System.arraycopy(encrypted_bytes, 0, output, encryption_stamp.length + digital_signature.length, encrypted_bytes.length);

//        System.out.println("digicert + stamp:" + Arrays.toString(Arrays.copyOfRange(output, 0, SHA256("".getBytes()).length + encryption_stamp.length)));

        
        
        //finally write the output to encrypted file
        file.write(output);
//        file.rename(encrypt_file_name(file_path));
        
        //file.rename(encrypt_file_name(file.getFileName()));
        String message = file_path + " was encrypted successfully!\n";
        System.out.println(message);
        return message;

    }

    public static String decrypt_file(String key, String file_path) throws IOException, NoSuchAlgorithmException {
        String message = file_path + " was decrypted successfully!\n";

        //Read decrypted file in bytes
        file = new FileIO(file_path);
        
        //all bytes read from the file
        byte[] file_bytes = file.read();

        //digital signature is read from the file (if any)
        byte[] digital_signature = new byte[SHA256("".getBytes()).length];
                

        
        //convert key to bytes
        byte[] key_bytes = key.getBytes();

        //This is the final output written to the decrypted file
        byte[] output; 

        //Make sure this file is already encrypted
        if(!are_bytes_encrypted(file_bytes)){
            FileIO.log(file_path + " is not encrypted");
            return "Error:" + file_path + " is not encrypted\n";
        }

        System.arraycopy(file_bytes, encryption_stamp.length, digital_signature, 0, digital_signature.length);
//        System.out.println("digicert + stamp:" + Arrays.toString(Arrays.copyOfRange(file_bytes, 0, digital_signature.length + encryption_stamp.length)));
//        System.out.println(Arrays.toString(digital_signature));
        
        
        //This variable will contain encrypted bytes (file_bytes - encryption_stamp)
        byte[] encrypted_bytes = new byte[file_bytes.length - encryption_stamp.length - digital_signature.length];
        
        
        //Get the encrypted portion of the file (i.e., file_bytes - encryption_stamp part)
        System.arraycopy(file_bytes, encryption_stamp.length + digital_signature.length,
                encrypted_bytes, 0, file_bytes.length - encryption_stamp.length - digital_signature.length);
//        System.out.println(encrypted_bytes.length);
        output = decrypt_bytes(key_bytes, encrypted_bytes);

        //Write output to decrypted file
        if(Arrays.equals(digital_signature, SHA256(output))){
            file.write(output); 
//            file.rename(decrypt_file_name(file_path));
                //file.rename(decrypt_file_name(file.getFileName()));
    
        }else{
            //System.out.println(Arrays.toString(digital_signature));
            //System.out.println(Arrays.toString(SHA256(output)));
            message = "File could not be decrypted. Wrong password and/or signature.\n";
        }
        
        System.out.println(message);

        return message;

    }
    
    public static byte [] SHA256(byte[] b) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(b); // Change this to "UTF-16" if needed
        return md.digest();
    }

    
}
