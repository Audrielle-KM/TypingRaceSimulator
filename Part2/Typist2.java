import java.util.ArrayList;

/**
 * The Typist Class is designed to represent an object(in this case, a competitor/typist) 
 * and contains private fields, a constructor, getter and setter methods. 
 * The class is built to store and manage the typist's data safely through encapsulation. 
 *
 * Starter code generously abandoned by Ty Posaurus, your predecessor,
 * who typed with two fingers and considered that "good enough".
 * He left a sticky note: "the slide-back thing is optional probably".
 * It is not optional. Good luck.
 *
 * @author Audrielle Kaytlyn Myesha
 * @version 2
 */
public class Typist2
{
    // Fields of class Typist
    // Hint: you will need six fields. Think carefully about their types.
    // One of them tracks how far along the passage the typist has reached.
    // Another tracks whether the typist is currently burnt out.
    // A third tracks HOW MANY turns of burnout remain (not just whether they are burnt out).
    // The remaining three should be fairly obvious.

    private String typistName;
    private char typistSymbol; // unicode char
    private double typistAccuracy; // accuracy rating
    private int typistProgress; // tracks how far along the passage
    private boolean burntOutState; // tracks if typist is burnt out or not
    private int burnOutTurnsRemaining = 0; // number of turns of burnout remaining
    private int mistypes; // number of mistypes during the race
    private int burnouts; // number of burnouts the typist burnt out during the race
    private double typistWPM; // tracks typist's WPM
    private double bestWPM = 0.0; // records personal best

    private int totalburnouts = 0;
    private int totalmistypes = 0;
    private int totalwins = 0;

    private int progressBeforeSlideBack = 0; // typist's progress before slideback
    private double oldAccuracy = 0; // records typist initial accuaracy before game starts
    private double oldBestWPM = 0.0; // record old personal best
    private boolean gotBurntOut = false; //(if typist ever burns out in game; when game is over, this value helps determine if typist loses accuracy)

    private ArrayList<RaceHistory> history = new ArrayList<>(); // history list that stores each frame of the race
    private ArrayList<String> badges = new ArrayList<>(); // badge list of all badges earned by typist

    // Constructor of class Typist
    /**
     * Constructor for objects of class Typist.
     * Creates a new typist with a given symbol, name, and accuracy rating.
     *
     * @param typistSymbol  a single Unicode character representing this typist (e.g. '①', '②', '③')
     * @param typistName    the name of the typist (e.g. "TURBOFINGERS")
     * @param typistAccuracy the typist's accuracy rating, between 0.0 and 1.0
     */
    public Typist2(char typistSymbol, String typistName, double typistAccuracy)
    {
        this.typistName = typistName; 
        this.typistSymbol = typistSymbol;
        this.typistAccuracy = typistAccuracy;

        oldAccuracy = typistAccuracy;
        oldBestWPM = bestWPM;
    }



    // Methods of class Typist

    /**
     * Sets this typist into a burnout state for a given number of turns.
     * A burnt-out typist cannot type until their burnout has worn off.
     *
     * @param turns the number of turns the burnout will last
     */
    public void burnOut(int turns)
    {
        if (turns > 0 && !burntOutState) {
            burntOutState = true;
            burnOutTurnsRemaining = turns;
        }
        
    }

    /**
     * stores typist's full race history (pos, WPM, accuracy, burnout, accuracy percentage)
     * 
     * @param race a snapshot of a race
     */
    public void addHistory(RaceHistory race)
    {
        history.add(race);
    }

    /**
     * Reduces the remaining burnout counter by one turn.
     * When the counter reaches zero, the typist recovers automatically.
     * Has no effect if the typist is not currently burnt out.
     */
    public void recoverFromBurnout()
    {
        if (burntOutState) { //if currently burnt out
            burnOutTurnsRemaining -= 1;

            if (burnOutTurnsRemaining == 0) {
                burntOutState = false;
            }
        }
    }

    /**
     * Returns the typist's accuracy rating.
     *
     * @return accuracy as a double between 0.0 and 1.0
     */
    public double getAccuracy() // accessor
    {
        return typistAccuracy;
    }

    /**
     * Returns the typist's accuracy before its changes after the race is over.
     * 
     * @return accuracy as a double between 0.0 and 1.0
     */
    public double getOldAccuracy()
    {
        return oldAccuracy;
    }

    /**
     * Returns the typist's full race history
     * 
     * @return typist's history array list
     */
    public ArrayList<RaceHistory> getHistory()
    {
        return history;
    }

    /**
     * Returns the typist's badges
     * 
     * @return typist's badge array list
     */
    public ArrayList<String> getBadges()
    {
        return badges;
    }

    /**
     * Returns the typist's current progress through the passage.
     * Progress is measured in characters typed correctly so far.
     * Note: this value can decrease if the typist mistypes.
     *
     * @return progress as a non-negative integer
     */
    public int getProgress() // accessor
    {
        return Math.abs(typistProgress);
    }

    /**
     * Returns the proportion of keystrokes tat were correct
     *
     * @return accuracy % ad a stirng
     */
    public String getAccuracyPercentage() // accessor
    {
        int correct_keystrokes = typistProgress - mistypes;

        double percentage = Math.abs(((correct_keystrokes)/ (double) (typistProgress + mistypes)) * 100); // non-negative
        return Math.round(percentage * 100.0) / 100.0 + "%"; // 2dp%
    }

    /**
     * Returns the typist's progress before slideback() is called
     * 
     */
    public int getProgressBeforeSlideBack() 
    {
        return progressBeforeSlideBack;
    }

    /**
     * Returns the name of the typist.
     *
     * @return the typist's name as a String
     */
    public String getName() // accessor
    {
        return typistName;
    }

    /**
     * Returns the WPM of the typist.
     *
     * @return the typist's WPM as a non-negative
     */
    public double getWPM() // accessor
    {
        return typistWPM;
    }

    /**
     * Returns the previous best WPM of the typist.
     *
     * @return the typist's previous personal best wpm as a non-negative
     */
    public double getOldBestWPM() // accessor
    {
        return oldBestWPM;
    }

    /**
     * Returns the final WPM of the typist.
     *
     * @return the typist's final WPM as a non-negative
     */
    public double getfinalWPM(long finish, long start) // accessor
    {
        long timeTaken = finish - start;
        double min = (timeTaken / 1000.0) / 60.0;

        int correct_keystrokes = (history.get(history.size() -1)).getPosition();

        double word = (correct_keystrokes + mistypes) / 5.0;

        return Math.round((word / min) * 100.0) / 100.0; // 2dp for consistency;
    }

    /**
     * Returns the best WPM the typist has performed
     *
     * @return the typist's best WPM as a non-negative
     */
    public double getPersonalBest() // accessor
    {
        return bestWPM;
    }
    
    /**
     * Returns the character symbol used to represent this typist.
     *
     * @return the typist's symbol as a char
     */
    public char getSymbol() // accessor
    {
        return typistSymbol;
    }

    /**
     * Returns the boolean value if the typist has ever burns out in the race
     */
    public boolean getGotBurntOut() //accessor
    {
        return gotBurntOut;
    }

    /**
     * Returns the number of turns of burnout remaining.
     * Returns 0 if the typist is not currently burnt out.
     *
     * @return burnout turns remaining as a non-negative integer
     */
    public int getBurnoutTurnsRemaining() //accessor
    {
        if (!burntOutState) {
            return 0;
        }
        return Math.abs(burnOutTurnsRemaining);
    }

    /**
     * Returns the number of mistypes typist has done.
     *
     * @return the typist's number of mistypes
     */
    public int getMistypes() //accessor
    {
       return mistypes;
    }

    /**
     * Returns the number of burnouts typist has gotten during the race.
     *
     * @return the typist's number of burnouts
     */
    public int getBurnoutsCount() //accessor
    {
       return burnouts;
    }

    /**
     * Returns the sum of burnouts the typist has faced in all races
     *
     * @return the typist's total number of burnouts in all races
     */
    public int getTotalBurnouts() //accessor
    {
       return totalburnouts;
    }
    
    /**
     * Resets the typist to their initial state, ready for a new race.
     * Progress returns to zero, burnout is cleared entirely.
     * Resets history list for next race
     */
    public void resetToStart()
    {

        oldBestWPM = bestWPM;
        typistProgress = 0;
        progressBeforeSlideBack = 0;

        burntOutState = false;
        burnOutTurnsRemaining = 0;
        gotBurntOut = false;
        mistypes = 0;
        burnouts = 0;

        history.clear();
        oldAccuracy = typistAccuracy;
    }

    /**
     * Returns true if this typist is currently burnt out, false otherwise.
     *
     * @return true if burnt out
     */
    public boolean isBurntOut()
    {
        if (!burntOutState){
            return false;
        }
        gotBurntOut = true; //set gotBurntOut to true, so when game finishes, this value determines accuaracy deduction
        return true;
    }

    /**
     * Advances the typist forward by one character along the passage.
     * Should only be called when the typist is not burnt out.
     */
    public void typeCharacter()
    {
        if (!burntOutState) {
            typistProgress += 1;
        }

    }

    /**
     * Moves the typist backwards by a given number of characters (a mistype).
     * Progress cannot go below zero — the typist cannot slide off the start.
     *
     * @param amount the number of characters to slide back (must be positive)
     */
    public void slideBack(int amount)
    {
        mistypes++;
        totalmistypes++;
        progressBeforeSlideBack = typistProgress;

        typistProgress -= Math.abs(amount);
        if (typistProgress < 0) {
            typistProgress = 0;
        }

    }

    /**
     * Adds new badge to the list of badges the typist has earned
     *
     * @param badge the badge the typist has earned
     */
    public void addBadge(String badge)
    {
        badges.add(badge);
    }

    /**
     * Sets the accuracy rating of the typist.
     * Values below 0.0 should be set to 0.0; values above 1.0 should be set to 1.0.
     *
     * @param newAccuracy the new accuracy rating
     */
    public void setAccuracy(double newAccuracy) // mutator
    {
        if (newAccuracy < 0.0) {
            typistAccuracy = 0.0;
        }
        else if (newAccuracy > 1.0) {
            typistAccuracy = 1.0;
        }
        else {
            typistAccuracy = Math.round(newAccuracy * 100.0) / 100.0; // 2dp for consistency
        }

    }

    /**
     * Sets the old accuracy rating of the typist.
     * Values below 0.0 should be set to 0.0; values above 1.0 should be set to 1.0.
     *
     * @param newAccuracy the new accuracy rating
     */
    public void setOldAccuracy(double newAccuracy) // mutator
    {
        if (newAccuracy < 0.0) {
            oldAccuracy = 0.0;
        }
        else if (newAccuracy > 1.0) {
            oldAccuracy = 1.0;
        }
        else {
            oldAccuracy = Math.round(newAccuracy * 100.0) / 100.0; // 2dp for consistency
        }
        typistAccuracy = oldAccuracy;

    }

    /**
     * Sets the symbol used to represent this typist.
     *
     * @param newSymbol the new symbol character
     */
    public void setSymbol(char newSymbol) // mutator
    {
        typistSymbol = newSymbol;

    }

    /**
     * Sets the typist's name
     *
     * @param newName the new name string
     */
    public void setName(String newName) // mutator
    {
        typistName = newName;

    }


    /**
     * Adds to typist's number of wins
     *
     */
    public void gainWins()
    {
        totalwins++;
    }

    /**
     * adds burnout count by 1
     */
    public void addBurnoutCount()
    {
        burnouts += 1;
        totalburnouts += burnouts;
    }

    /**
     * Sets the typist's WPM
     * 
     * @param current current time of the race
     * @param passageLength length of the passage
     * @param start starting time of the race
     */
    public void setWPM(long current, long start)
    {
        if (current > start)
        {
            long timeTaken = current - start;
            double min = (timeTaken / 1000.0) / 60.0;

            double word = (typistProgress + mistypes) / 5.0; // A standard word is defined as 5 chars, including spaces + punctuations

            typistWPM =  Math.round((word / min) * 100.0) / 100.0; // 2dp for consistency
            
            if (bestWPM < typistWPM)
            {
                bestWPM = typistWPM;
            }
        }
        else
        {
            typistWPM = 0.0;
        }
    }

}
