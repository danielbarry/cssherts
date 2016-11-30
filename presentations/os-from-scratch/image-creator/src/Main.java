package barray.ic;

/**
 * Main.java
 *
 * The entry class for the program, responsible for collecting and
 * understanding the parameters for the program.
 **/
public class Main{
  private static final String BOOTLOADER = "boot.bin";
  private static final String OUTPUT = "os.img";
  private static final int MEDIASIZE = 1474560;

  private String bootloader;
  private String output;
  private int mediaSize;
  private Image image;

  /**
   * main()
   *
   * The main entry method for the program, where it's responsibility is to go
   * from a static context to an instantiated one.
   *
   * @param args The arguments from the command line.
   **/
  public static void main(String[] args){
    new Main(args);
  }

  /**
   * Main()
   *
   * The main initialiser for the program, responsible for doing a pass on the
   * command line parameters to determine what to execute next.
   *
   * @param args The arguments from the command line.
   **/
  public Main(String[] args){
    /* Initialise the variables */
    bootloader = BOOTLOADER;
    output = OUTPUT;
    mediaSize = MEDIASIZE;
    image = new Image();
    /* Check that there is at least one command line argument */
    if(args.length <= 0){
      /* Display the help */
      printHelp();
    }else{
      /* Store the current mode */
      boolean optionsMode = true;
      /* Iterate over the passed parameters */
      for(int x = 0; x < args.length; x++){
        /* While in options mode, iterate over the options */
        if(optionsMode){
          /* Check the options */
          switch(args[x]){
            case "-a" :
            case "--about" :
              /* Display the about */
              printAbout();
              break;
            case "-b" :
            case "--boot" :
              /* Make sure we have enough parameters */
              if(++x < args.length){
                /* Store the bootloader */
                bootloader = args[x];
              }else{
                System.err.println("[ERR] Cannot set bootloader");
                printHelp();
              }
              break;
            case "-h" :
            case "--help" :
              /* Display the help */
              printHelp();
              break;
            case "-o" :
            case "--output" :
              /* Make sure we have enough parameters */
              if(++x < args.length){
                /* Store the output */
                output = args[x];
              }else{
                System.err.println("[ERR] Cannot set output");
                printHelp();
              }
              break;
            case "-s" :
            case "--size" :
              /* Make sure we have enough parameters */
              if(++x < args.length){
                /* Try to store the output */
                try{
                  mediaSize = Integer.parseInt(args[x]);
                }catch(NumberFormatException e){
                  System.err.println("[ERR] Invalid size number");
                  printHelp();
                }
              }else{
                System.err.println("[ERR] Cannot set media size");
                printHelp();
              }
              break;
            case "-v" :
            case "--version" :
              /* Display the version */
              printVersion();
              break;
            default :
              /* Switch to the file input mode */
              optionsMode = false;
              /* Roll back X so that it is processed */
              x--;
              break;
          }
        }else{
          /* Add the file to the list of files */
          image.addFile(args[x]);
        }
      }
    }
    /* Set various settings to image instance */
    image.setBootloader(bootloader);
    System.out.println("[SET] bootloader -> " + bootloader);
    image.setOutput(output);
    System.out.println("[SET] output -> " + output);
    image.setMediaSize(mediaSize);
    System.out.println("[SET] mediaSize -> " + mediaSize);
    /* Build an image if possible */
    if(image.isValid()){
      if(image.generate()){
        System.out.println("[>>>] Finished generation");
      }else{
        System.err.println("[ERR] Unable to generate image");
      }
    }else{
      System.err.println("[ERR] Image is invalid, no image will be created");
    }
  }

  private void printAbout(){
    System.out.println(
      "Written by Daniel Barry 2016"
    );
    exit();
  }

  private void printHelp(){
    System.out.println(
      "\n  ic [OPT] <FILE 1> .. <FILE N>" +
      "\n" +
      "\n    Legal" +
      "\n      USE THIS PROGRAM AT YOUR OWN RISK." +
      "\n" +
      "\n    Description" +
      "\n      This program has been design to generate an bootable image" +
      "\n      for a specific test Operating System with a custom file" +
      "\n      table. This program is to be used for the Operating System" +
      "\n      described in the book by Daniel Barry." +
      "\n" +
      "\n    OPTions" +
      "\n      -a" +
      "\n      --about         Information about program" +
      "\n      -b" +
      "\n      --boot          Set the bootloader" +
      "\n                      Default: " + BOOTLOADER +
      "\n      -h" +
      "\n      --help          Displays this help" +
      "\n      -o" +
      "\n      --output        Set the output" +
      "\n                      Default: " + OUTPUT +
      "\n      -s" +
      "\n      --size          Set the media size (bytes)" +
      "\n                      Default: " + MEDIASIZE +
      "\n      -v" +
      "\n      --version       Program version" +
      "\n" +
      "\n    FILE" +
      "\n      Adds a file to the image to be created." +
      "\n" +
      "\n    Additional" +
      "\n      Meaning of tags:" +
      "\n        [ADD] The addition of something." +
      "\n        [SET] A setting was updated." +
      "\n        [ERR] An error occurred." +
      "\n"
    );
    exit();
  }

  private void printVersion(){
    System.out.println(
      "version 0.0.1"
    );
    exit();
  }

  private void exit(){
    System.exit(0);
  }
}
