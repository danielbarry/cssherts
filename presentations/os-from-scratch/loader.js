/**
 * Loader
 *
 * This loader is responsible for loading pages from source without blocking
 * the browser the code is run in. In this loader, pages are converted from
 * MarkDown to HTML for browser viewing. Please use the source below for
 * information on how the conversion is performed.
 *
 * NOTE: This is a simplified version of markdown and is designed to carry
 *       little if any complexity in order to increase usability and speed. If
 *       bugs are found, please contact B[].
 *
 * Features:
 *   * Headers
 *   * Lists
 *   * Code blocks
 *   * Links
 *   * Images
 *   * Bold
 *   * Italics
 *   * In-line code blocks
 *
 * @author B[]
 * @version 1.0.9
 **/

/* ---- Constants ---- */

var TASK_BREAK_TIME_MS;
var TASK_PROCESS_TIME_MS;
var TAB_MAX;

/* ---- Global Variables ---- */

var tasksScanned;
var tasksCompleted;

/* ---- Stack ---- */

var stackTimeout;
var taskStack;
var varStack;

/* ---- Variables ---- */

var elements;

/**
 * Loader()
 *
 * The task runner for the entire program. This function is responsible for
 * scanning the entire page and converting MarkDown to valid HTML.
 **/
function Loader(){
  /* Initialise tasking system */
  init();
  /* Task payloads */
  elements = document.getElementsByName("md");
  for(var i = 0; i < elements.length; i++){
    /* Add task to remove element */
    addTask(function(){
      var elem = getVar();
      elem.innerHTML = "";
    });
    /* Add reference to element to be cleaned */
    addVar(elements[i]);
    /* Process the element lines */
    var lines = elements[i].innerHTML.split("\n");
    for(var e = 0; e < lines.length; e++){
      /* Add task to task stack */
      addTask(function(){
        var elem = getVar();
        var line = getVar();
        var skip = false;
        /* <<<< Entire line tests >>>> */
        if(line.length == 0){
          line = "<br /><br />";
        }
        /* <<<< Start of line tests >>>> */
        if(line[0] == '#'){
          var temp = line;
          /* Find out type of header */
          var len = line.length;
          var h = 1;
          for(var z = 1; z < len; z++){
            if(line[z] == '#'){
              h++;
            }else{
              /* Make sure next character is space */
              if(line[z] == ' '){
                /* Remove previous markers */
                temp = line.slice(h + 1);
              }
              z = line.length;
            }
          }
          /* Add HTML */
          temp = "<h" + h + ">" + temp + "</h" + h + ">";
          /* Replace line for searching */
          line = temp;
        }
        if(line[0] == ' '){
          if(line[1] == ' '){
            /* Check whether we have a list or potential code block */
            if(line[2] == ' '){
              /* Check whether we have code block */
              if(line[3] == ' '){
                /* Escape the string */
                temp = line.slice(4);
                temp = temp.replace(
                  /&/g, "&amp;"
                ).replace(
                  /</g, "&lt;"
                ).replace(
                  />/g, "&gt;"
                ).replace(
                  /"/g, "&quot;"
                );
                /* Check the length, add some space is zero */
                if(temp.length <= 0){
                  temp += ' ';
                }
                /* Throw some pre-tags around it */
                line = "<pre name=\"code\" style=\"margin:0px;\">" + temp + "</pre>";
                skip = true;
              }
            }else{
              /* Indent the list */
              var point = line.slice(2).split(" ");
              var pointLen = point[0].length;
              if(point[0] == "*"){
                point[0] = "&middot;&nbsp;";
              }
              var temp = "<tt name=\"list\">&nbsp;&nbsp;" + point[0];
              for(var z = point[0].length; z < TAB_MAX; z++){
                temp += "&nbsp;";
              }
              temp += "</tt>" + line.slice(2 + pointLen);
              line = temp + "<br />";
            }
          }
        }
        /* <<<< Middle of line tests >>>> */
        /* Only perform tests if we shouldn't be skipping */
        if(!skip){
          var temp = "";
          var images = line.split("![");
          if(!(images.length == 1 && !(images[0] == '!' && images[1] == '['))){
            for(var z = 0; z < images.length; z++){
              var endS = images[z].indexOf(']');
              var begC = images[z].indexOf('(', endS);
              var endC = images[z].indexOf(')', begC);
              /* If invalid, skip over */
              if(endS < 0 || begC < 0 || endC < 0 || endS + 1 != begC){
                /* Put everything back as it was */
                if(z > 0){
                  temp += "![";
                }
                temp += images[z];
              }else{
                temp += "<img alt=\"";
                temp += images[z].slice(0, endS);
                temp += "\" src=\"";
                temp += images[z].slice(begC + 1, endC);
                temp += "\">";
                /* Add everything that wasn't part of the breakup */
                temp += images[z].slice(endC + 1);
              }
            }
            line = temp;
          }
          temp = "";
          var links = line.split("[");
          if(!(links.length == 1 && line[0] != '[')){
            for(var z = 0; z < links.length; z++){
              var endS = links[z].indexOf(']');
              var begC = links[z].indexOf('(', endS);
              var endC = links[z].indexOf(')', begC);
              /* If invalid, skip over */
              if(endS < 0 || begC < 0 || endC < 0 || endS + 1 != begC){
                /* Put everything back as it was */
                if(z > 0){
                  temp += "[";
                }
                temp += links[z];
              }else{
                temp += "<a href=\"";
                temp += links[z].slice(begC + 1, endC);
                temp += "\">";
                temp += links[z].slice(0, endS);
                temp += "</a>";
                /* Add everything that wasn't part of the breakup */
                temp += links[z].slice(endC + 1);
              }
            }
            line = temp;
          }
          var pos = 0;
          while(pos >= 0){
            /* Search for first instance */
            pos = line.indexOf("**");
            if(pos >= 0){
              /* Replace first instance */
              line = line.slice(0, pos) + "<b>" + line.slice(pos + 2);
              /* Search for second instance */
              pos = line.indexOf("**");
              if(pos >= 0){
                /* Replace second instance */
                line = line.slice(0, pos) + "</b>" + line.slice(pos + 2);
              }
            }
          }
          pos = 0;
          while(pos >= 0){
            /* Search for first instance that doesn't start with spaces */
            pos = line.indexOf("*");
            if(pos >= 0){
              /* Replace first instance */
              line = line.slice(0, pos) + "<i>" + line.slice(pos + 1);
              /* Search for second instance */
              pos = line.indexOf("*");
              if(pos >= 0){
                /* Replace second instance */
                line = line.slice(0, pos) + "</i>" + line.slice(pos + 1);
              }
            }
          }
          pos = 0;
          while(pos >= 0){
            /* Search for first instance that doesn't start with spaces */
            pos = line.indexOf("`");
            if(pos >= 0){
              /* Replace first instance */
              line = line.slice(0, pos) + "<pre class=\"inline\">" + line.slice(pos + 1);
              /* Search for second instance */
              pos = line.indexOf("`");
              if(pos >= 0){
                /* Replace second instance */
                line = line.slice(0, pos) + "</pre>" + line.slice(pos + 1);
              }
            }
          }
        }
        /* Add line to element */
        elem.innerHTML += line;
      });
      /* Add reference to elements */
      addVar(elements[i]);
      /* Allow function to access line */
      addVar(lines[e]);
    }
    /* Add task to swap elements XMP for P */
    addTask(function(){
      var elem = getVar();
      var nElem = document.createElement('p');
      nElem.innerHTML = elem.innerHTML;
      elem.parentNode.insertBefore(nElem, elem);
      elem.parentNode.removeChild(elem);
    });
    /* Add reference to element to be cleaned */
    addVar(elements[i]);
  }
  /* Process tasks */
  process();
}

/**
 * init()
 *
 * The initialiser for the tasking system.
 **/
function init(){
  /* Allow the browser to process other tasks */
  TASK_BREAK_TIME_MS = 128;
  /* Time to process the tasks for */
  TASK_PROCESS_TIME_MS = 256;
  /* Record progress completion */
  tasksScanned = 0;
  tasksCompleted = 0;
  /* The overall task stack */
  taskStack = [];
  /* The variable stack */
  varStack = [];
  /* Set tab space size */
  TAB_MAX = 4;
}

/**
 * addTask()
 *
 * Adds task to back of task list and increment task count.
 **/
function addTask(func){
  taskStack.push(func);
  tasksScanned++;
}

/**
 * addVar()
 *
 * Adds a variable to the variable stack.
 **/
function addVar(v){
  varStack.push(v);
}

/**
 * getVar()
 *
 * Gets a variable and removes it from the list.
 **/
function getVar(){
  var r = varStack[0];
  varStack.shift();
  return r;
}

/**
 * process()
 *
 * Process tasks.
 **/
function process(){
  /* Make temporary date variable */
  var now = new Date();
  /* Set the stack time out for the future */
  stackTimeout = now.getTime() + TASK_PROCESS_TIME_MS;
  /* Iterate over tasks that remain */
  var i = 0;
  var run = true;
  for(; i < taskStack.length && run == true; i++){
    now = new Date();
    /* Check whether we have run our time */
    if(now.getTime() >= stackTimeout){
      /* Break out of the loop */
      run = false;
      /* Decrement indexing */
      i--;
    }else{
      /* Run next stack item */
      taskStack[i]();
      /* Increment number of tasks complete */
      tasksCompleted++;
    }
  }
  console.log("Time Took: " + (now.getTime() + TASK_PROCESS_TIME_MS - stackTimeout) + "ms")
  /* When we get here, removed processed items from the stack */
  var tempStack = taskStack.slice(i);
  taskStack = tempStack;
  console.log("Stack Remaining = " + taskStack.length);
  /* Register break out time callback if more processing required */
  if(taskStack.length > 0){
    setTimeout(function(){ process(); }, TASK_BREAK_TIME_MS);
  }
}
