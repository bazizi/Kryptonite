/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author          Behnam Azizi
 * @date            Feb. 15, 2015
 * @description     This class is used to traverse a directory
 * 
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DirWalker {


    public static boolean isSymlink(File file) throws IOException {
      if (file == null)
        throw new NullPointerException("File must not be null");
      File canon;
      if (file.getParent() == null) {
        canon = file;
      } else {
        File canonDir = file.getParentFile().getCanonicalFile();
        canon = new File(canonDir, file.getName());
      }
      return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }    

    
    public static ArrayList<File> walk( String path ) throws IOException {

        File root = new File( path );
        File[] list = root.listFiles();
        ArrayList<File> output = new ArrayList<File>();

        if (list == null) return output;

        for ( File f : list ) {
            if(!isSymlink(f)){
                if ( f.isDirectory() ) {
                    System.out.println( "Dir:" + f.getAbsoluteFile() );
                    output.addAll(walk( f.getAbsolutePath() ));
                }
                else {
    //                System.out.println( "File:" + f.getAbsoluteFile() );
                    output.add(f.getAbsoluteFile());
                }
            }
        }
        return output;
    }

    public static void main(String[] args) {
        System.out.println(args[0]);
    }
}
