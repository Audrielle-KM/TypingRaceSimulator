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

    private int progressBeforeSlideBack = 0; // typist's progress before slideback
    private double oldAccuracy = 0; // records typist initial accuaracy before game starts
    private boolean gotBurntOut = false; //(if typist ever burns out in game; when game is over, this value helps determine if typist loses accuracy)

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
     * Resets the typist to their initial state, ready for a new race.
     * Progress returns to zero, burnout is cleared entirely.
     */
    public void resetToStart()
    {
        typistProgress = 0;
        progressBeforeSlideBack = 0;

        burntOutState = false;
        burnOutTurnsRemaining = 0;
        gotBurntOut = false;
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
        progressBeforeSlideBack = typistProgress;
        typistProgress -= Math.abs(amount);
        if (typistProgress < 0) {
            typistProgress = 0;
        }

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

}
