import java.util.concurrent.TimeUnit;

import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

/**
 * A typing race simulation. Three typists race to complete a passage of text,
 * advancing character by character — or sliding backwards when they mistype.
 *
 * Originally written by Ty Posaurus, who left this project to "focus on his
 * two-finger technique". He assured us the code was "basically done".
 * We have found evidence to the contrary.
 *
 * @author Audrielle Kaytlyn Myesha
 * @version 2
 */
public class TypingGUI
{
    private int passageLength;   // Total characters in the passage to type

    private static CardLayout layout; // Allows flipping through each page/section of GUI
    private static JPanel card;

    private Typist seat1Typist;
    private Typist seat2Typist;
    private Typist seat3Typist;
    private Typist winnerTypist;

    // Accuracy thresholds for mistype and burnout events
    // (Ty tuned these values "by feel". They may need adjustment.)
    private static final double MISTYPE_BASE_CHANCE = 0.3;
    private static final int    SLIDE_BACK_AMOUNT   = 2;
    private static final int    BURNOUT_DURATION     = 3;

    /**
     * Constructor for objects of class TypingRace.
     * Sets up the race with a passage of the given length.
     * Initially there are no typists seated.
     *
     * @param passageLength the number of characters in the passage to type
     */
    public TypingGUI(int passageLength)
    {
        this.passageLength = passageLength;
        seat1Typist = null;
        seat2Typist = null;
        seat3Typist = null;
    }

    /**
     * Seats a typist at the given seat number (1, 2, or 3).
     *
     * @param theTypist  the typist to seat
     * @param seatNumber the seat to place them in (1–3)
     */
    public void addTypist(Typist theTypist, int seatNumber)
    {
        if (seatNumber == 1)
        {
            seat1Typist = theTypist;
        }
        else if (seatNumber == 2)
        {
            seat2Typist = theTypist;
        }
        else if (seatNumber == 3)
        {
            seat3Typist = theTypist;
        }
        else
        {
            System.out.println("Cannot seat typist at seat " + seatNumber + " — there is no such seat.");
        }
    }

    /**
     * Starts the typing race.
     * All typists are reset to the beginning, then the simulation runs
     * turn by turn until one typist completes the full passage.
     *
     * Note from Ty: "I didn't bother printing the winner at the end,
     * you can probably figure that out yourself."
     */
    public void startRace()
    {
        boolean finished = false;

        // Reset all typists to the start of the passage
        // (Ty was in a hurry here)
        seat1Typist.resetToStart();
        seat2Typist.resetToStart();
        seat3Typist.resetToStart(); // (FIXED)seat3Typist reset was missing

        while (!finished)
        {
            // Advance each typist by one turn
            advanceTypist(seat1Typist);
            advanceTypist(seat2Typist);
            advanceTypist(seat3Typist);

            // Print the current state of the race
            printRace();

            // Check if any typist has finished the passage
            if ( raceFinishedBy(seat1Typist) || raceFinishedBy(seat2Typist) || raceFinishedBy(seat3Typist) )
            {
                finished = true;
            }

            // Wait 200ms between turns so the animation is visible
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (Exception e) {}
        }

        /**
         * if finished is true
         * each competitor is checked if they have ever been burnt out mid-race and their accuracy is deducted by 0.02.
         * the winner's accuracy is improved by 0.04.
         * The winner's name is called out and their final accuracy is announced 
         * followed by how much it has improved/reduced/no changes from their old accuracy
         * 
         */

       if (finished) 
        {

            if (seat1Typist.getGotBurntOut())
            {
                seat1Typist.setAccuracy(seat1Typist.getAccuracy() - 0.02);
            }
            if (seat2Typist.getGotBurntOut())
            {
                seat2Typist.setAccuracy(seat2Typist.getAccuracy() - 0.02);
            }
            if (seat3Typist.getGotBurntOut())
            {
                seat3Typist.setAccuracy(seat3Typist.getAccuracy() - 0.02);
            }

            winnerTypist.setAccuracy(winnerTypist.getAccuracy() + 0.04);
            System.out.println("And the winner is..." + winnerTypist.getName());

            String winnerOldAccuracy = String.format("%.2f", winnerTypist.getOldAccuracy()); // 2 dp for consistency

            if (winnerTypist.getAccuracy() > winnerTypist.getOldAccuracy()) 
            {
                System.out.println("Final accuracy: " + winnerTypist.getAccuracy() + " (improved from " + winnerOldAccuracy + ")" );
            }
            else if (winnerTypist.getAccuracy() == winnerTypist.getOldAccuracy() )
            {
                System.out.println("Final accuracy: " + winnerTypist.getAccuracy() + " (no changes from  " + winnerOldAccuracy + ")" );
            }
            else 
            {
                System.out.println("Final accuracy: " + winnerTypist.getAccuracy() + " (reduced from " + winnerOldAccuracy + ")" );
            }
        }
    }

    /**
     * Simulates one turn for a typist.
     *
     * If the typist is burnt out, they recover one turn's worth and skip typing.
     * Otherwise:
     *   - They may type a character (advancing progress) based on their accuracy.
     *   - They may mistype (sliding back) — the chance of a mistype should decrease
     *     for more accurate typists.
     *   - They may burn out — more likely for very high-accuracy typists
     *     who are pushing themselves too hard.
     *
     * @param theTypist the typist to advance
     */
    private void advanceTypist(Typist theTypist)
    {
        if (theTypist.isBurntOut())
        {
            // Recovering from burnout — skip this turn
            theTypist.recoverFromBurnout();
            return;
        }

        // Attempt to type a character
        if (Math.random() < theTypist.getAccuracy())
        {
            theTypist.typeCharacter();
        }

        // Mistype check — the probability should reflect the typist's accuracy
        if (Math.random() < theTypist.getAccuracy() * MISTYPE_BASE_CHANCE)
        {
            theTypist.slideBack(SLIDE_BACK_AMOUNT);
        }

        // Burnout check — pushing too hard increases burnout risk
        // (probability scales with accuracy squared, capped at ~0.05)
        if (Math.random() < 0.05 * theTypist.getAccuracy() * theTypist.getAccuracy())
        {
            theTypist.burnOut(BURNOUT_DURATION);
        }
    }

    /**
     * Returns true if the given typist has completed the full passage.
     *
     * @param theTypist the typist to check
     * @return true if their progress has reached or passed the passage length
     */
    private boolean raceFinishedBy(Typist theTypist)
    {
        if (theTypist.getProgress() >= passageLength) //(FIXED) progress can overshoot so set to '>='
        {
            winnerTypist = theTypist; // assigns value to typist who won
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Prints the current state of the race to the terminal.
     * Shows each typist's position along the passage, burnout state,
     * and a WPM estimate based on current progress.
     */
    private void printRace()
    {
        System.out.print('\u000C'); // Clear terminal

        System.out.println("  TYPING RACE — passage length: " + passageLength + " chars");
        multiplePrint('=', passageLength + 3);
        System.out.println();

        printSeat(seat1Typist);
        System.out.println();

        printSeat(seat2Typist);
        System.out.println();

        printSeat(seat3Typist);
        System.out.println();

        multiplePrint('=', passageLength + 3);
        System.out.println();
        System.out.println("  [~] = burnt out    [<] = just mistyped");
    }

    /**
     * Prints a single typist's lane.
     *
     * Examples:
     *   |          ⌨           | TURBOFINGERS (Accuracy: 0.85)
     *   |    [zz]              | HUNT_N_PECK  (Accuracy: 0.40) BURNT OUT (2 turns)
     *
     * Note: Ty forgot to show when a typist has just mistyped. That would
     * be a nice improvement — perhaps a [<] marker after their symbol.
     *
     * @param theTypist the typist whose lane to print
     */
    private void printSeat(Typist theTypist)
    {
        int spacesBefore = theTypist.getProgress();
        int spacesAfter  = passageLength - theTypist.getProgress();
        String typistAccuracy = String.format("%.2f", theTypist.getAccuracy()); // for consistent formatting

        System.out.print('|');
        multiplePrint(' ', spacesBefore);

        // Always show the typist's symbol so they can be identified on screen.
        // Append ~ when burnt out so the state is visible without hiding identity.
        System.out.print(theTypist.getSymbol());
        if (theTypist.isBurntOut())
        {
            System.out.print('~');
            spacesAfter--; // symbol + ~ together take two characters
        }

        if (!theTypist.isBurntOut() && theTypist.getProgressBeforeSlideBack() > theTypist.getProgress()) {
            spacesAfter -= 1; // symbol take 1 char
            int gapsBetweenSymbol = theTypist.getProgressBeforeSlideBack() - theTypist.getProgress(); // distance from when slide back occured and current progress
            
            int gapsAfter = gapsBetweenSymbol + 2; // num chars covered when slide back occured + [<]
            multiplePrint(' ', gapsBetweenSymbol);
            
            String s = "[";
            if (gapsAfter > spacesAfter) // detects if '[<]' goes beyond finishline and fixes printing positioning
            {
                int distance = gapsAfter - spacesAfter;
                if (distance < 2)
                {
                    s += "<";
                    System.out.print(s);
                }
                else
                {
                    System.out.print(s);
                }
            }
            else
            {
                s += "<]";
                System.out.print(s);
            }
            spacesAfter -= gapsAfter;
        }

        multiplePrint(' ', spacesAfter);
        System.out.print('|');
        System.out.print(' ');

        // Print name and accuracy
        if (theTypist.isBurntOut()) // if burnt out
        {

            System.out.print(theTypist.getName()
                + " (Accuracy: " +typistAccuracy + ")"
                + " BURNT OUT (" + theTypist.getBurnoutTurnsRemaining() + " turns)");
        }
        else if (!theTypist.isBurntOut() && theTypist.getProgressBeforeSlideBack() > theTypist.getProgress()) // if mistypes
        {
            System.out.print(theTypist.getName()
                + " (Accuracy: " + typistAccuracy + ")"
                + "  ← just mistyped");
        }
        else // neither burnt out nor mistyped
        {
            System.out.print(theTypist.getName()
                + " (Accuracy: " + typistAccuracy + ")");
        }
    }

    /**
     * Prints a character a given number of times.
     *
     * @param aChar the character to print
     * @param times how many times to print it
     */
    private void multiplePrint(char aChar, int times)
    {
        int i = 0;
        while (i < times)
        {
            System.out.print(aChar);
            i = i + 1;
        }
    }

    /**
     * The main method that creates the GUI of the TypingRace
     * Creates main menu and customisation page.
     * Pages that user have to select options to set up the race
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("TypingRace");
        layout = new CardLayout();
        card = new JPanel(layout);

        JLabel gameName = new JLabel("TYPING RACE");
        gameName.setBounds(282, 5, 148, 29);
        gameName.setFont(new Font("Arial", Font.BOLD, 20));
        gameName.setForeground(Color.decode("#f3753f"));

        frame.add(gameName, BorderLayout.NORTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(667, 400);

        //Main Menu
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.decode("#eeeeee"));
        mainPanel.setLayout(null);
    }
}
