package barray.ic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Image.java
 *
 * This class is responsible for generating the arbitrary image with the
 * arbitrary filesystem.
 **/
public class Image{
  private static final int BOOTSIZE = 512;
  private static final int TABLESIZE = 512;
  private static final int NAMESIZE = 4;
  private static final int CHUNKSIZE = 512;

  private File bootloader;
  private File output;
  private int mediaSize;
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
   * setMediaSize()
   *
   * Sets the media size for the target device.
   *
   * @param size The size of the media in bytes to be generated.
   **/
  public void setMediaSize(int size){
    mediaSize = size;
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
    /* Check that the bootloader exists */
    if(!bootloader.exists()){
      System.err.println("[ERR] The bootloader does not exist");
      return false;
    }
    if(!bootloader.isFile()){
      System.err.println("[ERR] The bootloader is not a file");
      return false;
    }
    if(!bootloader.canRead()){
      System.err.println("[ERR] The bootloader cannot be read");
      return false;
    }
    /* Check that each of the files exists */
    for(int x = 0; x < files.size(); x++){
      File file = files.get(x);
      if(!file.exists()){
        System.err.println("[ERR] `" + file.getName() + "` does not exist");
        return false;
      }
      if(!file.isFile()){
        System.err.println("[ERR] `" + file.getName() + "` is not a file");
        return false;
      }
      if(!file.canRead()){
        System.err.println("[ERR] `" + file.getName() + "` cannot be read");
        return false;
      }
    }
    /* Bootloader is 512 bytes in size */
    if(bootloader.length() != BOOTSIZE){
      System.err.println("[ERR] The bootloader is not 512 bytes");
      return false;
    }
    /* Bootloader has descriptor word */
    InputStream is = null;
    try{
      is = new FileInputStream(bootloader);
    }catch(IOException e){
      System.err.println("[ERR] Unable to read bootloader");
      return false;
    }
    byte[] buffer = new byte[BOOTSIZE];
    try{
      is.read(buffer, 0, BOOTSIZE);
    }catch(IOException e){
      System.err.println("[ERR] Unable to read bootloader bytes");
      return false;
    }
    try{
      is.close();
    }catch(IOException e){
      /* Do nothing */
    }
    if((buffer[510] & 0xFF) != 85 || (buffer[511] & 0xFF) != 170){
      System.err.println("[ERR] Incorrect bootloader descriptor");
      return false;
    }
    /* Check that there are no repeats in the filenames */
    HashMap<String, File> filenames = new HashMap<String, File>();
    for(int x = 0; x < files.size(); x++){
      String file = files.get(x).getName();
      if(filenames.get(file) == null){
        filenames.put(file, files.get(x));
      }else{
        System.err.println("[ERR] `" + file + "` exists more than once");
        return false;
      }
    }
    /* Check the output stream won't overwrite a file */
    if(output.exists()){
      System.err.println("[ERR] Output file would be overwritten");
      return false;
    }
    /* Check that the media size is large enough for the files */
    long totalFileSize = bootloader.length() + TABLESIZE;
    for(int x = 0; x < files.size(); x++){
      totalFileSize += files.get(x).length();
    }
    if(totalFileSize > mediaSize){
      System.err.println("[ERR] Files too large for target media");
      return false;
    }
    /* Check if the media size is okay to use in RAM */
    /* NOTE: http://stackoverflow.com/questions/12807797/java-get-available-memory#12807848 */
    long allocatedMemory = Runtime.getRuntime().totalMemory() -
      Runtime.getRuntime().freeMemory();
    long freeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
    if(mediaSize > freeMemory){
      System.err.println("[ERR] JVM doesn't have enough RAM to process media");
      return false;
    }
    return true;
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
    /* Generate a byte array the size of the target media */
    byte[] buffer = new byte[mediaSize];
    int bPos = 0;
    /* Store the order of the files to be added to the file table */
    ArrayList<String> filenames = new ArrayList<String>();
    /* Add the bootloader to the image */
    int writeSize = loadBuffer(bPos, CHUNKSIZE, buffer, bootloader);
    if(writeSize < 0){
      return false;
    }else{
      bPos += writeSize;
    }
    /* Jump over the table */
    int tableSize = TABLESIZE;
    if(TABLESIZE % CHUNKSIZE > 0){
      tableSize += CHUNKSIZE - (TABLESIZE % CHUNKSIZE);
    }
    bPos += tableSize;
    /* Push resources into their respect positions */
    for(int x = 0; x < files.size(); x++){
      writeSize = loadBuffer(bPos, CHUNKSIZE, buffer, files.get(x));
      if(writeSize < 0){
        return false;
      }else{
        bPos += writeSize;
      }
      for(int y = 0; y < writeSize / CHUNKSIZE; y++){
        filenames.add(files.get(x).getName());
      }
    }
    /* Add the bootloader to the table */
    byte[] fn = cutAndPad(bootloader.getName(), NAMESIZE, '\0').getBytes();
    int tPos = 0;
    for(int x = 0; x < fn.length; x++){
      buffer[(int)(bootloader.length() + tPos + x)] = fn[x];
    }
    tPos += NAMESIZE;
    /* Add the table to the table */
    String tableName = "tble";
    for(int y = 0; y < tableSize / CHUNKSIZE; y++){
      fn = cutAndPad(tableName, NAMESIZE, '\0').getBytes();
      for(int x = 0; x < fn.length; x++){
        buffer[(int)(bootloader.length() + tPos + x)] = fn[x];
      }
      tPos += NAMESIZE;
    }
    /* Add the files to the table */
    for(int y = 0; y < filenames.size(); y++){
      /* TODO: Check that tPos hasn't run over the allocated table space. */
      fn = cutAndPad(filenames.get(y), NAMESIZE, '\0').getBytes();
      for(int x = 0; x < fn.length; x++){
        buffer[(int)(bootloader.length() + tPos + x)] = fn[x];
      }
      tPos += NAMESIZE;
    }
    /* TODO: Validate the image that has been generated. */
    /* Create and write output stream */
    OutputStream os = null;
    try{
      os = new FileOutputStream(output);
      os.write(buffer);
    }catch(IOException e){
      System.err.println("[ERR] Can't write to output file");
      return false;
    }
    try{
      if(os != null){
        os.close();
      }
    }catch(IOException e){
      /* Do nothing */
    }
    return true;
  }

  /**
   * loadBuffer()
   *
   * Loads a file from the disk into the buffer at a given position, padding
   * the file out to the chunk size for the media. This method then returns the
   * size of the file it wrote with the padding.
   *
   * @param offset The offset to inset the file at.
   * @param chunkSize The chunk size for the media.
   * @param buffer The buffer to insert the file into.
   * @param file The file to be added to the buffer.
   * @return The size of the file written to the buffer, -1 on error. This
   * method will also write to the error stream with a more detailed
   * explanation of the failure.
   **/
  private int loadBuffer(int offset, int chunkSize, byte[] buffer, File file){
    /* TODO: Placement for floppy disks with >18 sectors being saved. */
    /* Calculate the padding */
    int paddedSize = (int)file.length();
    if(file.length() % chunkSize > 0){
      paddedSize += chunkSize - (file.length() % chunkSize);
    }
    /* Load the file into the buffer */
    InputStream is = null;
    try{
      is = new FileInputStream(file);
      is.read(buffer, offset, paddedSize);
    }catch(IOException e){
      System.err.println("[ERR] Could not read `" + file.getName() + "`");
      return -1;
    }
    try{
      if(is != null){
        is.close();
      }
    }catch(IOException e){
      /* Do nothing */
    }
    return paddedSize;
  }

  /**
   * cutAndPad()
   *
   * Cut the string to the desired length and pad it if it is not long enough
   * to fill the cut length. This method will also remove the file extension if
   * it exists (anything after the first dot).
   *
   * @param str The string to be cut and padded.
   * @param len The length to cut the string to.
   * @param pad The padding to use on the string.
   * @return The cut and padded string.
   **/
  private String cutAndPad(String str, int len, char pad){
    int dot = str.indexOf('.');
    str = str.substring(0, dot > 0 ? dot : str.length());
    if(str.length() >= len){
      return str.substring(0, len);
    }else{
      for(int x = str.length(); x < len; x++){
        str += pad;
      }
      return str;
    }
  }
}
