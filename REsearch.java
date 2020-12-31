import java.util.Scanner;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

// REsearch program made by NS174 - Natch Sadindum - 1269188 linked to partner Isiah Donald 1359651
/* This program takes a FSM file called "machines.txt" and a text file
* Usage is - java REsearch <textfile>
* The FSMs in the text file should be in the format "<stateName>,<character>,<nextState1>,<nextState2>"
* eg. 0,97,0,1
* the first state is considered the start state, and the last state the final state
*/

public class REsearch {
    static List<FSM> machines = new ArrayList<FSM>();  // Saves all FSMs from the text file, all new states used in deques are copies of FSMs in here
    Deque deque = new Deque();  // Creates a Deque object that we will use constantly

    //====================================================================================

    public static void main(String[] args) {
        if (args.length == 1) { // make sure the person has entered in a file name
            ReadFSM("machines.txt");
            ReadTextFile(args[0]);
        }
        else
            System.out.println("Usage: java REsearch <filename>");
    }

    //====================================================================================

    // Reads a text file and check each line against our regex via Checkline
    private static void ReadTextFile(String file) {
        try {
            int lineCount = 0;

            InputStream is = new FileInputStream(file);
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));

            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();

            while(line != null){
                CheckLine(line, lineCount);
                line = buf.readLine();
                lineCount++;
            }
        }

        catch (IOException e) {
            System.out.println("Could not find input text file!  Please check your filename");
        }
    }
    /* takes a line of text and runs the FSM check across the line
    if a match is found, stop and print out the line of text */

    private static void CheckLine(String s, int count) {
            //System.out.println("Starting new line " + s);
            Deque.Reset(machines.get(0));

            int index = 0;
            boolean success = false;
            String[] line = s.split("");
            FSM checkState = Deque.Head();

            // checks all current states ahead of SCAN until SCAN is reached
            while (index < line.length) {
                checkState = Deque.Pop(); // pop a current state

                //System.out.println("Checking State " + checkState.stateNum);

                // Checks if there is a match or a wildcard
                if (ConvertPart(line[index]) == checkState.character || checkState.character == 46) {  // if the current state and current character match
                    success = true;
                    //System.out.println("State match: " + checkState.stateNum);

                    // put next states at back
                    Deque.Put(GetFSM(checkState.nextState1));
                    Deque.Put(GetFSM(checkState.nextState2));

                }
                else {
                    //System.out.println("NO MATCH!");
                }
                //System.out.println("\r\n");

                // Condition 3 - if we reach the end state
                if (checkState.stateNum == machines.get(machines.size() - 1).stateNum) {
                    //System.out.println("MATCH FOUND");
                    PrintLine(s, count);
                     break;
                }
                // If we have cleared all the current states
                if (Deque.IsHeadScan()) {
                    // Move to next character
                    index++;
                    // if we had some successful match, then continue checking using our states
                    if (success) {
                        success = false;
                        Deque.MoveNextStates();
                        //System.out.println("Moving States\r\n");
                    }
                    // if no successful match, check next character and start from start state
                    else {
                        Deque.Reset(machines.get(0));
                        //System.out.println("no match at all for this character, starting from start");
                    }

                    // Move next states to front

                    // Condition 2 - if only the scan is left, moveto next character
                    if (Deque.OnlyScan()) {
                        //System.out.println("Scan is only object in deque, no match");
                        break;
                    }
                }


            }
            // Condition 1 - if we reach the end of the string
            // If we have reached this point, then we probably have failed and so we move to next line
    }

    //====================================================================================

    // Reads a text file containing a compilation of all FSM states
    private static void ReadFSM(String file) {
        // Reads each line, which contains the 4 parameters of a state
        try {

            // Creates two arrays which we use to convert our input FSM parameters to ints
            String[] inputs = new String[4];
            int[] parts = new int[4];

            // loads the first line
            InputStream is = new FileInputStream(file);
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            String line = buf.readLine();

            // read every line, converting each line into a FSM
            while(line != null){
                inputs = line.split(",");
                parts = setPartsArray(inputs);

                // Adds all machines into a list which we will access later
                FSM newState = new FSM(parts[0], parts[1], parts[2], parts[3]);
                machines.add(newState);
                line = buf.readLine();
            }
        }
        catch (Exception e) {
            System.out.println("Could not find machine file!  Please enter in a valid txt file!");
        }
    }
    // converts all string inputs from text file into int except the character of the state (2)
    private static int[] setPartsArray (String[] sa) {
        int[] p = new int[4];
        for (int i = 0; i < 4; i++) {
            if (i == 1)
                p[i] = ConvertPart(sa[i]);
            else
                p[i] = Integer.parseInt(sa[i]);
        }
        return p;
    }
    // changes the character from a string, to char to int
    private static int ConvertPart(String s) {
        char c = s.charAt(0);
        int i = Integer.valueOf(c);
        return i;
    }

    //====================================================================================

    // check if we are trying to get a state that exists
    private static boolean CheckFSM(int n) {
        if (n > machines.size() || n < 0)
            return false;
        else
            return true;
    }
    // Finds a FSM of state number n from the list
    private static FSM GetFSM(int n) {
        FSM r = new FSM();
        for (int i = 0; i < machines.size(); i++) {
            if (machines.get(i).stateNum == n)
                r = new FSM(machines.get(i).stateNum, machines.get(i).character,machines.get(i).nextState1,machines.get(i).nextState2);
        }
        return r;
    }
    // creates a new instance of an FSM, as all the FSMs in the list are static - forgot about static and it caught me for a while :(
    public static FSM CreateInstance(FSM f) {
        FSM r = new FSM(f.stateNum, f.character, f.nextState1, f.nextState2);
        return r;
    }

    //====================================================================================

    // If a match is found, print out the line
    public static void PrintLine(String s, int count) {
        System.out.println("Match found on line: " + count + " - " + s);
    }
}

// FSM object that stores 4 parameters - all parameters are expected to be set at start
class FSM {
    int stateNum;
    int character;
    int nextState1;
    int nextState2;

    public FSM next;

    // main constructor, takes 4 parameters
    public FSM(int sn, int cr, int n1, int n2) {
        stateNum = sn;
        character = cr;
        nextState1 = n1;
        nextState2 = n2;
    }
    // fallback constructor
    public FSM() {

    }
    // Prints a description of the FSM onto the console
    public void Describe() {
        if (next == null)
            System.out.println("StateNum: " + stateNum + "   Char: " + character + "   next1: " + nextState1 + "   next2: " + nextState2 + "   tail element");
            else
                System.out.println("StateNum: " + stateNum + "   Char: " + character + "   next1: " + nextState1 + "   next2: " + nextState2 + "   linked to: " + next.stateNum);
    }
}

class Deque {
    private static FSM scan = new FSM(9999, Integer.valueOf('|'), 9999,9999);
    private static FSM head = null;
    private static FSM tail = null;
    public static FSM Head() { return head; }

    // Default Deque constructor, not used, as we set things up automatically
    public Deque() {}
    // puts a FSM onto the front of the deque
    public static void Push(FSM f) {
        if (head == null) {
            head = f;
            head.next = null;
            tail = f;
        }
        else {
            FSM temp = f;
            temp.next = head;
            head = temp;
        }
    }
    // removes an FSM from the front of the deque
    public static FSM Pop() {

        FSM temp = head;
        head = head.next;
        return temp;
    }
    // Places an FSM at the back of the deque
    public static void Put(FSM f) {
        f.next = null;
        tail.next = f;
        tail = f;

    }

    // Checks if head is the scan
    public static boolean IsHeadScan() {
        if (head.stateNum == scan.stateNum)

            return true;
        else
            return false;
    }

    // moves the scan to the back of the deque/move next states in front of scan - assumed that scan is head on use
    public static void MoveNextStates() {
        //scan is head
        head = head.next;
        tail.next = scan;
        tail = scan;
        tail.next = null;
    }

    // Dumps all FSMs and scan onto console
    public static void Dump() {
        if (head == null)
            System.out.println("The Deque is currently empty!");
        else {
            FSM current = head;
            while (current != null) {
                current.Describe();
                current = current.next;
            }

        }

    }

    // Checks if the scan is the only thing elft in the deque
    public static boolean OnlyScan() {
        if (head == scan && tail == scan) {
            return true;
        }
        return false;
    }
    // Resets the deque back to the original form of initial state and scan
    public static void Reset(FSM f) {
        head = null;
        tail = null;

        head = new FSM(f.stateNum, f.character, f.nextState1, f.nextState2);

        tail = new FSM(scan.stateNum, scan.character, scan.nextState1, scan.nextState2);
        head.next = tail;
        tail.next = null;
    }
}
