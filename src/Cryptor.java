
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Behnam Azizi
 * @date Feb. 15, 2015
 * @description This class is used to encrypting and decrypting files
 */
public class Cryptor {
    private static final byte[] encryption_stamp = "10a34637ad661d98ba3344717656fcc76209c2f8".getBytes();
    private static final int checksum_size = 32;
    private static boolean was_warning_shown = false;

    public static void logByte(byte[] b) {
        System.out.println(b.toString());
    }

    private static byte[] crypt_bytes(byte[] key, byte[] b) {
        assert b.length == key.length;

        byte[] output = new byte[b.length];
        for (int i = 0; i < b.length; i++) {
            output[i] = (byte) (key[i] ^ b[i]);
        }

        return output;
    }


    public static boolean is_file_encrypted(RandomAccessFile f) throws NoSuchAlgorithmException, IOException {

        if (f.length() < encryption_stamp.length + checksum_size) {
            return false;
        }

        byte[] found_stamp = new byte[encryption_stamp.length];
        f.seek(f.length() - encryption_stamp.length - checksum_size);
        f.read(found_stamp, 0, encryption_stamp.length);
        f.seek(0);
//        System.out.println(Arrays.toString(found_stamp));
//        System.out.println(Arrays.toString(encryption_stamp));

        return Arrays.equals(encryption_stamp, found_stamp);
    }

    public static String encrypt_dir(String key, String dir_path, int buffer_size) throws IOException, NoSuchAlgorithmException {

        StringBuilder message = new StringBuilder();
        ArrayList<File> files = DirWalker.walk(dir_path);

        for (File fl : files) {
            message.append(Cryptor.encrypt_file(key, fl.getAbsolutePath(), buffer_size));
        }
        return message.toString();
    }

    public static String decrypt_dir(String key_path, String dir_path, int buffer_size) throws IOException, NoSuchAlgorithmException {
        StringBuilder message = new StringBuilder();
        ArrayList<File> files = DirWalker.walk(dir_path);
        for (File fl : files) {
            message.append(decrypt_file(key_path, fl.getAbsolutePath(), buffer_size));
        }
        return message.toString();
    }

    private static String get_result_message(int retVal, File f, int buffer_size) {
        switch (retVal) {
            case ErrorCode.E_FILE_ALREADY_ENCRYPTED:
                return String.format("ERROR: '%s' is already encrypted\n", f.getAbsolutePath());
            case ErrorCode.E_FILE_NOT_ENCRYPTED:
                return String.format("ERROR: '%s' is not encrypted\n", f.getAbsolutePath());
            case ErrorCode.E_KEY_TOO_SMALL:
                return String.format("ERROR: Key length must be at least "
                        + "as large as the buffer size (%d)\n", buffer_size);
            case ErrorCode.E_FILE_NOT_FOUND:
                return String.format("ERROR: '%s' not found or not accessible with this user\n", f.getAbsolutePath());
            case ErrorCode.E_OUT_OF_MEMORY:
                return String.format("ERROR: Your computer ran out of memeory\n");
            case ErrorCode.OK_FILE_ENCRYPTED_SUCCESSFULLY:
                return String.format("OK: '%s' was encrypted successfully\n", f.getAbsolutePath());               
            case ErrorCode.OK_FILE_DECRYPTED_SUCCESSFULLY:
                return String.format("OK: '%s' was decrypted successfully\n", f.getAbsolutePath());
            default:
                break;
        }
        return String.format("UNKNOWN ERROR: '%S'", f.getAbsolutePath());
        

    }

    public static String encrypt_file(String key_file_path, String file_path, int buffer_size) throws IOException, NoSuchAlgorithmException {
        int retVal = crypt(key_file_path, file_path, buffer_size, "E");
        return get_result_message(retVal, new File(file_path), buffer_size);

    }

    public static int crypt(String key_file_path, String file_path, int buffer_size, String operation) throws IOException, NoSuchAlgorithmException {
        File file = new File(file_path);
        File key = new File(key_file_path);
        int num_bytes_read = 0;
        RandomAccessFile file_input;
        RandomAccessFile key_input;
        RandomAccessFile file_output;
        byte[] file_buffer = new byte[buffer_size];
        byte[] key_buffer = new byte[buffer_size];
        byte[] encrypted_buffer;
        int total_bytes_read = 0;
        
        try{
            file_input = new RandomAccessFile(file, "r");
            key_input = new RandomAccessFile(key, "r");
            file_output = new RandomAccessFile(file, "rw");
        
        }catch (FileNotFoundException ex) {
            return ErrorCode.E_FILE_NOT_FOUND;
        }
        

        long bytes_remaining = (operation.equals("E")) ? file.length() : file.length() - encryption_stamp.length - checksum_size;
//        System.out.println("Key length:" + key.length());

        if (operation.equals("E") && is_file_encrypted(file_input)) {
            return ErrorCode.E_FILE_ALREADY_ENCRYPTED;
        } else if (operation.equals("D") && !is_file_encrypted(file_input)) {
            return ErrorCode.E_FILE_NOT_ENCRYPTED;
        }

        if (key.length() < buffer_size) {
            return ErrorCode.E_KEY_TOO_SMALL;
        } else if (!was_warning_shown && key.length() % buffer_size != 0) {
            System.out.println("WARNING: Key length should be a multiple "
                    + "of " + buffer_size + ". The last " + key.length() % buffer_size + " will be"
                    + "thrown away.");
            was_warning_shown = true;
        }

        // write encryption stamp
        // write hash of original fille
        // read into buffer, encrypt, write back to file       
        try {
//            System.out.println("file length:" + file.length());
//            System.out.println("bytes remaining:" + bytes_remaining);

            // while the file is not fully read
            while (bytes_remaining > 0) {
//                System.out.println("reading file");
                //input.read() returns -1, 0, or more :
                buffer_size = (int) ((buffer_size > bytes_remaining) ? bytes_remaining : buffer_size);
                // read buffer into file_buffer
                num_bytes_read = file_input.read(file_buffer, 0, buffer_size);

//                System.out.println("buffer size: " + buffer_size);
//                System.out.println("total bytes read:" + total_bytes_read);
//                System.out.println("num bytes read into buffer:" + num_bytes_read);
//                System.out.println("remaining num bytes:" + bytes_remaining);
//                System.out.println("file size:" + file.length());

                if (num_bytes_read > 0) {
                    // circular key input
                    key_input.seek(total_bytes_read % (key.length() - key.length() % buffer_size));
                    key_input.read(key_buffer, 0, num_bytes_read);
                    encrypted_buffer = crypt_bytes(key_buffer, file_buffer);
//                    System.out.println("file size:" + file.length());
                    file_output.seek(total_bytes_read);
                    file_output.write(encrypted_buffer, 0, num_bytes_read);
//                    System.out.println("file size:" + file.length());

                    total_bytes_read += num_bytes_read;
                    bytes_remaining -= num_bytes_read;

//                    System.out.println("key buffer:" + Arrays.toString(key_buffer));
//                    System.out.println("file buffer: " + Arrays.toString(file_buffer));
//                    System.out.println("enc buffer:" + Arrays.toString(encrypted_buffer));
//                    System.out.println("======================");
                } else {
                    System.err.println("No bytes were read");
                }

            }

            if (operation.equals("E")) {
                file_output.seek(file_output.length());
                file_output.write(encryption_stamp);

//                System.out.println("File length: " + file_output.length());
//                System.out.println("Stamp length: " + encryption_stamp.length);
                file_output.write(SHA256(new File(file_path)));
            } else {
                file_output.setLength(total_bytes_read);
            }

        } catch (OutOfMemoryError e) {
            return ErrorCode.E_OUT_OF_MEMORY;

        } finally {
//            System.out.println("file size:" + file.length());


            file_input.close();
            file_output.close();
            key_input.close();
        }

        if(operation.equals("E")){
            return ErrorCode.OK_FILE_ENCRYPTED_SUCCESSFULLY;
        }else{
            return ErrorCode.OK_FILE_DECRYPTED_SUCCESSFULLY;
        }

    }

    public static String decrypt_file(String key_file_path, String file_path, int buffer_size) throws IOException, NoSuchAlgorithmException {
        int retVal = crypt(key_file_path, file_path, buffer_size, "D");
        return get_result_message(retVal, new File(file_path), buffer_size);

    }

    public static byte[] SHA256(File f) throws NoSuchAlgorithmException, IOException {
        return getFileChecksum(MessageDigest.getInstance("SHA-256"), f);
    }

    private static byte[] getFileChecksum(MessageDigest digest, File file) throws IOException {
        //Get file file_input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);
        long bytes_remaining = file.length() - encryption_stamp.length;
        long bytes_read = 0;

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        //close the stream; We don't need it now.
        fis.close();

//        System.out.println("Checksum size: " + digest.digest().length);

        //Get the hash's bytes
        return digest.digest();
    }

}
