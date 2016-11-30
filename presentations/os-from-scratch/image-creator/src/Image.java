package barray.ic;

import java.io.File;
import java.util.ArrayList;

/**
 * Image.java
 *
 * This class is responsible for generating the arbitrary image with the
 * arbitrary filesystem.
 **/
public class Image{
  private static final int BOOTSIZE = 512;
  private static final int TABLESIZE = 512;

  private File bootloader;
  private File output;
  private ArrayList<File> files;

  /**
   * Image()
   *
   * Initialise the class and prepare to generate an image.
   **/
  public Image(){
    files = new ArrayList<File>();
  }

  /**
   * addFile()
   *
   * Adds a file to the list to be generated into the image.
   *
   * @param file A String containing the full path for the binary to be added
   * to the image.
   **/
  public void addFile(String file){
    files.add(new File(file));
    System.out.println("[ADD] `" + file + "`");
  }

  /**
   * setBootloader()
   *
   * Sets the bootloader file by looking at the currently loaded bootloader
   * files and if not matched, loading the suggested bootloader file.
   *
   * @param file The path of the bootloader to be added.
   **/
  public void setBootloader(String file){
    /* Set the bootloader */
    bootloader = new File(file);
    /* Find bootloader if it exists */
    int remove = -1;
    for(int x = 0; x < files.size(); x++){
      if(files.get(x).getName().equals(bootloader.getName())){
        remove = x;
        break;
      }
    }
    /* Remove the file from the normal list if it was found */
    if(remove >= 0){
      System.out.println("[DEL] `" + files.get(remove).getName() + "`");
      bootloader = files.get(remove);
      files.remove(remove);
    }
  }

  /**
   * setOutput()
   *
   * Sets the output destination from the path provided.
   *
   * @param file The path of the output file to be generated..
   **/
  public void setOutput(String file){
    output = new File(file);
  }

  /**
   * isValid()
   *
   * Tests whether the image in it's current form could possibly generate a
   * valid image. A reason for why this may not be possible could be that a
   * binary can not be found or that a valid bootloader does not exist.
   *
   * @return Whether this image is valid and could generate a valid image.
   **/
  public boolean isValid(){
    /* TODO: Check whether bootloader is:
               * 512 bytes in size
               * Has the descriptor word */
    /* TODO: Check that each of the files exists. */
    /* TODO: Check that there are no repeats in the filenames. */
    return false;
  }

  /**
   * generate()
   *
   * Generates the image with all the specified parameters. If an error occurs,
   * this will be thrown into the error stream with a descriptive message. It
   * is recommended that the isValid() method is run before running this method
   * in order to prevent any possibility of partial builds.
   *
   * @return Whether the generation was successful.
   **/
  public boolean generate(){
    /* TODO: Generate the file table to be used. */
    /* TODO: Create output stream. */
    /* TODO: Push resources down the output stream. */
    /* TODO: Close the output stream. */
    /* TODO: Validate the image that has been generated. */
    return false;
  }
}
