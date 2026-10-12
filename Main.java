
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
//NAME: Emir Hot 
//ASSIGNMENT: Prgm 3
//LAB SECTION Wednesday 
//LECTURE SECTION: Tues/Thurs

public class Main{

    public static void main(String[] args) throws FileNotFoundException {
        ArrayList<SentList> sentList = new ArrayList<SentList>();
        ArrayList<SentList> posList = new ArrayList<SentList>();
        ArrayList<SentList> negList = new ArrayList<SentList>();
        ArrayList<Words> wordList = new ArrayList<Words>();

        //load sentiment, positive words and negative words arraylists
        readSentimentList(sentList, posList, negList);

        //read review
        //load ArrayList wordList that will contain original review & pos & neg
        String inFileName;
        String outFileName = "reviews.txt";
        PrintWriter outFile = new PrintWriter(outFileName);

        // open input file adding review + number + ".txt" to review
        // if not able to open, print a message and continue
        // else process the file
        // if the file can be read properly, print the results
        for (int i = 1; i <= 8; i++){
            inFileName = "review" + i + ".txt";
            wordList.clear();
            Words.setPosFlag(false);
            Words.setNegFlag(false);

            if (readReview(sentList, posList, negList, wordList, inFileName)){
                printReview(wordList, inFileName, outFile);
            }
        }

        outFile.close();
    }

    //PRE:  accept the empty ArrayLists created in main 
    //POST: the arrays are loaded with the proper words and information
    public static void readSentimentList(ArrayList<SentList> sentList,
                                         ArrayList<SentList> posList,
                                         ArrayList<SentList> negList){
        String csvFilePath = "sentiment.txt";
        String line;
        double tempValue;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            while ((line = br.readLine()) != null) {
                 // Split the line by commas into an array of strings
                 String[] values = line.split(",");

                 if (values.length != 2)
                     continue;

                 try {
                     tempValue = Double.parseDouble(values[1].trim());
                 }
                 catch (NumberFormatException e) {
                     continue;
                 }

                 //Create object SentList & add to sentList arraylist
                 SentList newWord = new SentList(values[0].trim(), tempValue);
                 sentList.add(newWord);

                 //if word values are pos add to posList
                 //if word values are neg, add to neglist
                 if (tempValue > 1.25){
                     posList.add(newWord);
                 }
                 if (tempValue < -1.25){
                     negList.add(newWord);
                 }
            }
        }
        catch (IOException e) {
            System.out.println("Error reading the file.");
            e.printStackTrace();
        }
    }

    //PRE: accept the word lists and file name to open 
    //POST: read the file while the line is not null
    //      each word is edited (to lower case without punctuation)
    //      the sentiment value is accessed
    //      if the word is positive - update to a random word in the negative list and update the word value
    //      if the word is negative - update to a random positive word in the positive list & update the word value

    public static boolean readReview(ArrayList<SentList> sentList,
                                  ArrayList<SentList> posList,
                                  ArrayList<SentList> negList,
                                  ArrayList<Words> wordList,
                                  String fileName){

        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                 // Split the line by commas into an array of strings
                 String[] values = line.split(" ");

                 //for each word
                 //  strip off punctuation at the end of word set charWord to null or value of punctuation
                 //  save origWord, posWord, negWord
                 //  change to lower case  (set editWord)
                 //  look up word in sentList (set origValue, posVlue, negValue)
                 //  if positive enough, find new negword in negList & reset negWord & NegValue, reset negFlag
                 //  if negative enough, find new posword in posList & reset posWord & posValue, reset posFlag

                 for (int i = 0; i < values.length; i++){
                     String origWord = values[i];

                     if (origWord.length() == 0)
                         continue;

                     String editWord = origWord.toLowerCase();

                     while (editWord.length() > 0 &&
                            isPunctuation(editWord.charAt(editWord.length() - 1))){
                         editWord = editWord.substring(0, editWord.length() - 1);
                     }

                     double origValue = getSentiment(sentList, editWord);

                     String posWord = origWord;
                     String negWord = origWord;

                     double posValue = origValue;
                     double negValue = origValue;

                     if (origValue < -1 && posList.size() > 0){
                         int random = (int)(Math.random() * posList.size());
                         posWord = posList.get(random).word;
                         posValue = posList.get(random).value;
                         Words.setPosFlag(true);
                     }

                     if (origValue > 1 && negList.size() > 0){
                         int random = (int)(Math.random() * negList.size());
                         negWord = negList.get(random).word;
                         negValue = negList.get(random).value;
                         Words.setNegFlag(true);
                     }

                     Words word = new Words(origWord, editWord, posWord, negWord,
                                           origValue, posValue, negValue);
                     wordList.add(word);
                 }
            }
            br.close();
            return true;
        }
        catch (IOException e) {
            System.out.println("Error reading the file: " + fileName);
            return false;
        }
    }

    //PRE:  accept the updated wordlist 
    //POST: loop through word list, create a string that will be the original, positive & negative reviews
    //      print each review
    public static void printReview(ArrayList<Words> wordList, String inFile,
                                  PrintWriter outFile){

        double originalTotal = 0;
        double positiveTotal = 0;
        double negativeTotal = 0;
        double oldPositive = 0;
        double newPositive = 0;
        double oldNegative = 0;
        double newNegative = 0;

        boolean changedPositive = false;
        boolean changedNegative = false;

        String originalReview = "";
        String positiveReview = "";
        String negativeReview = "";

        for (int i = 0; i < wordList.size(); i++){
            Words word = wordList.get(i);

            originalTotal += word.sentOrigValue;
            positiveTotal += word.sentPosValue;
            negativeTotal += word.sentNegValue;

            if (i > 0){
                originalReview += " ";
                positiveReview += " ";
                negativeReview += " ";
            }

            originalReview += word.origWord;
            positiveReview += word.posWord;
            negativeReview += word.negWord;

            if (word.sentOrigValue < -1 && !word.posWord.equals(word.origWord)){
                changedPositive = true;
                oldPositive += word.sentOrigValue;
                newPositive += word.sentPosValue;
            }

            if (word.sentOrigValue > 1 && !word.negWord.equals(word.origWord)){
                changedNegative = true;
                oldNegative += word.sentOrigValue;
                newNegative += word.sentNegValue;
            }
        }

        outFile.println("Original Review for File: " + inFile);
        outFile.println();

        printWrapped(originalReview, outFile);

        outFile.println();
        outFile.printf("Original Sentiment: %.2f%n", originalTotal);

        outFile.println();
        outFile.println();

        if (!changedPositive){
            outFile.println("Positive Review for File: " + inFile
                    + ". Review cannot be made more positive");
        }
        else {
            outFile.println("Positive Review for File: " + inFile);

            printWrapped(positiveReview, outFile);

            outFile.println("WORDS UPDATED TO BE MORE POSITIVE:");

            for (int i = 0; i < wordList.size(); i++){
                Words word = wordList.get(i);

                if (word.sentOrigValue < -1 && !word.posWord.equals(word.origWord)){
                    outFile.printf("%20s %7.2f %20s %7.2f%n",
                            word.editWord, word.sentOrigValue,
                            word.posWord, word.sentPosValue);
                }
            }

            outFile.printf("Total: %22.2f %28.2f%n",
                           oldPositive, newPositive);
            outFile.println();
            outFile.printf("Positive Sentiment: %.2f%n", positiveTotal);
        }

        outFile.println();
        outFile.println();

        if (!changedNegative){
            outFile.println("Negative Review for File: " + inFile
                    + ". Review cannot be made more negative");
        }
        else {
            outFile.println("Negative Review for File: " + inFile);

            printWrapped(negativeReview, outFile);

            outFile.println("WORDS UPDATED TO BE MORE NEGATIVE:");

            for (int i = 0; i < wordList.size(); i++){
                Words word = wordList.get(i);

                if (word.sentOrigValue > 1 && !word.negWord.equals(word.origWord)){
                    outFile.printf("%20s %7.2f %20s %7.2f%n",
                            word.editWord, word.sentOrigValue,
                            word.negWord, word.sentNegValue);
                }
            }

            outFile.printf("Total: %22.2f %28.2f%n",
                           oldNegative, newNegative);
            outFile.println();
            outFile.printf("Negative Sentiment: %.2f%n", negativeTotal);
        }

        outFile.println();
        outFile.println();
        outFile.println();
        outFile.println();
    }

    //PRE: accept a review and output file -
    //POST: print the review with lines of 80 characters or less
    public static void printWrapped(String review, PrintWriter outFile){
        String[] words = review.split(" ");
        int lineLength = 0;

        for (int i = 0; i < words.length; i++){
            String word = words[i];

            if (lineLength > 0 && lineLength + 1 + word.length() > 80){
                outFile.println();
                lineLength = 0;
            }

            if (lineLength > 0){
                outFile.print(" ");
                lineLength++;
            }

            while (word.length() > 80){
                outFile.println(word.substring(0, 80));
                word = word.substring(80);
                lineLength = 0;
            }

            outFile.print(word);
            lineLength += word.length();
        }

        outFile.println();
    }

    //PRE:  accept a character 
    //POST: return true if this character is punctuation; false otherwise
    //      There may be a better way to write this!
    static boolean isPunctuation(char ch) {
        if (ch == '!' || ch == '\"' || ch == '#' || ch == '$' || ch == '%' || ch == '&' || ch == '\'' || ch == '(' || ch == ')' || ch == '*' || ch == '+' || ch == ',' || ch == '-' || ch == '.' || ch == '/' || ch == ':' || ch == ';' || ch == '<' || ch == '=' || ch == '>' || ch == '?' || ch == '@' || ch == '[' || ch == '\\' || ch == ']' || ch == '^' || ch == '`' || ch == '{' || ch == '|' || ch == '}')
          return true;
        return false;
    }

    //PRE:  accept the sentiment words list and a word to find - this searches through the sent list for a specific word, if it finds it, it returns the sent value, otherwise it returns 0
    //POST: return the value of the sentiment if found, 0 otherwise
    static double getSentiment (ArrayList<SentList> sentList, String eWord) {

        for (int i = 0; i < sentList.size(); i++){
            if (sentList.get(i).word.equals(eWord)){
                return sentList.get(i).value;
            }
        }

        return 0.0;
    }
}
