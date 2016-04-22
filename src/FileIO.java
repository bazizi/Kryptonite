
/**
 *
 * @author behnam
 */
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/** JDK 7+. */
public class FileIO {
  
  public static void main(String [] args) throws IOException{
    FileIO binary = new FileIO("test.txt");
    byte[] bytes = binary.read();
    FileIO.log(bytes);
    FileIO.log(bytes[0] ^  010100);
    FileIO.log("Hello".getBytes());
    //binary.write(bytes);
  }

  private String file_path;

  public FileIO(String file_name){
      this.file_path = file_name;
  }
  
  public byte[] read() throws IOException {
    Path path = Paths.get(this.file_path);
    return Files.readAllBytes(path);
  }
  
  public String getFilePath(){
      return file_path;
  }

    public String getFilePathWithoutExtension(){
      int pos = file_path.lastIndexOf(".");
      if (pos > 0) {
          return file_path.substring(0, pos);
      }
      return file_path;
    }
    
    public String getFileName(){
        return new File(this.getFilePath()).getName();
    }
    
    public void rename(String new_name){
        File old_file = new File(this.getFilePath());
        String old_file_dir = new File(this.getFilePath()).getParent();
        File new_file = new File(old_file_dir, new_name);
        new File(this.getFilePath()).renameTo(new_file);
    }

  
    public void write(byte[] aBytes) throws IOException {
      Path path = Paths.get(this.file_path);
      Files.write(path, aBytes); //creates, overwrites
    }
 
    public void append(byte[] aBytes) throws IOException {
      Path path = Paths.get(this.file_path);
      Files.write(path, aBytes, StandardOpenOption.APPEND); //creates, overwrites
    }

  
    public static void log(Object aMsg){
      System.out.println(String.valueOf(aMsg));
    }

}  