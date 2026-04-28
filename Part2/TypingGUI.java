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
    private static int passageLength;   // Total characters in the passage to type

    private static CardLayout layout; // Allows flipping through each page/section of GUI
    private static JPanel card;

    private Typist seat1Typist;
    private Typist seat2Typist;
    private Typist seat3Typist;
    private static int numberOfTypists = 0;
    private static Typist winnerTypist;

    // Accuracy thresholds for mistype and burnout events
    // (Ty tuned these values "by feel". They may need adjustment.)
    private static final double MISTYPE_BASE_CHANCE = 0.3;
    private static final int    SLIDE_BACK_AMOUNT   = 2;
    private static final int    BURNOUT_DURATION     = 3;

    // Dropdown list that holds values for passagelength & number of seats
    static JComboBox<String> lengthOption;
    static JComboBox<Integer> seatsOption;
    static JComboBox<String> styleOption;
    static JComboBox<String> keyboardType;
    static JComboBox<String> symbolOption;

    // GUI components that text values will be used across the program
    static JLabel selectedLength;
    static JLabel numberOfSeatsText;
    static JTextArea keyboardInfo;
    static JTextArea stylesInfo;

    static JButton autocorrectButton;
    static JButton nightShiftButton;
    static JButton caffeineButton;

    /**
     * Sets the passage length (Short, Medium, Long) [not Custom - different method]
     */
    private static void setPassageLength()
    {
         if (lengthOption.getSelectedItem().toString().equals("Short"))
        {
            passageLength = 10;
        }
        else if (lengthOption.getSelectedItem().toString().equals("Medium"))
        {
            passageLength = 20;
        }
        else if (lengthOption.getSelectedItem().toString().equals("Long"))
        {
            passageLength = 30;
        }
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
     * Chaning the components(buttons) background when it's clicked
     * 
     * @param button the modified button
     * @param onClickColour the colour when button is clicked
     * @param BackColour the original colour of the button
     */
    public static void setOnClickColour(JComponent button, Color onClickColour, Color BackColour)
    {
        button.setOpaque(true);
        button.addMouseListener(new MouseAdapter() {
            public void mouseClick(MouseEvent e){
                button.setBackground(onClickColour);
            }

            public void mouseReleased(MouseEvent e){
                button.setBackground(BackColour);
            }
        });
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

        JLabel mainMenuText = new JLabel("Main Menu");
        mainMenuText.setBounds(303, 31, 106, 30);
        mainMenuText.setFont(new Font("Arial", Font.ITALIC, 18));
        mainMenuText.setForeground(Color.decode("#ff7146"));
        mainPanel.add(mainMenuText);

        JLabel selectPassageLengthText = new JLabel("Select Passage Length:");
        selectPassageLengthText.setBounds(35, 71, 160, 21);
        selectPassageLengthText.setFont(new Font("Arial", Font.PLAIN, 14));
        selectPassageLengthText.setForeground(Color.decode("#1b1b1b"));
        mainPanel.add(selectPassageLengthText);

        String options[] = {"Short", "Medium", "Long", "Custom"};
        lengthOption = new JComboBox<String>(options);
        lengthOption.setBounds(35, 71, 160, 21);
        lengthOption.setLocation(35,111);
        mainPanel.add(lengthOption);

        selectedLength = new JLabel(lengthOption.getSelectedItem().toString() + "🗸");
        selectedLength.setBounds(201, 111, 106, 16);
        selectedLength.setForeground(Color.decode("#1b1b1b"));
        mainPanel.add(selectedLength);

        JTextField customLengthTextField = new JTextField("(5-50)");
        customLengthTextField.setBounds(90, 137, 94, 24);
        customLengthTextField.setBackground(Color.decode("#ffffff"));
        customLengthTextField.setForeground(Color.GRAY);
        customLengthTextField.setVisible(false);
        customLengthTextField.setEditable(false);
        mainPanel.add(customLengthTextField);

        customLengthTextField.addFocusListener(new FocusAdapter() { // sets Placeholder text
            public void onFocus(FocusEvent e){
                if (customLengthTextField.getText().equals("(5-50)"))
                    {
                        customLengthTextField.setText("");
                        customLengthTextField.setText("");
                        customLengthTextField.setForeground(Color.BLACK);
                    }
                }
            public void outFocus(FocusEvent e){
            if (customLengthTextField.getText().isEmpty())
                {
                    customLengthTextField.setText("(5-50)");
                    customLengthTextField.setForeground(Color.GRAY);
                }
            }
        });

        JButton setLengthButton = new JButton("Set");
        setLengthButton.setBounds(186, 142, 47, 29);
        setLengthButton.setBackground(Color.decode("#ffffff"));
        setLengthButton.setForeground(Color.decode("#1b1b1b"));
        setLengthButton.setFocusPainted(false);
        setLengthButton.setVisible(false);
        mainPanel.add(setLengthButton);

        lengthOption.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e)
            {
                selectedLength.setText(lengthOption.getSelectedItem().toString() + "🗸");

                if (lengthOption.getSelectedItem().toString().equals("Custom"))
                {
                    customLengthTextField.setVisible(true);
                    customLengthTextField.setEditable(true);
                    setLengthButton.setVisible(true);
                }
                else
                {
                    customLengthTextField.setVisible(false);
                    customLengthTextField.setEditable(false);
                    setLengthButton.setVisible(false);
                }
            }
        });
        setLengthButton.addActionListener(e -> { 
            try {
                
                setOnClickColour(setLengthButton,Color.decode("#7a7a7a"), Color.decode("#ffffff"));
                passageLength = Integer.parseInt(customLengthTextField.getText().trim());
                if (passageLength >= 5 && passageLength <= 50)
                {
                    setLengthButton.setText("🗸");

                    javax.swing.Timer timer = new javax.swing.Timer(1000, ee -> {
                    
                        customLengthTextField.setEditable(true);
                        setLengthButton.setText("Set");
                    
                    });
                    timer.setRepeats(false);
                    timer.start();
                    customLengthTextField.setEditable(false);
                }
                else
                {
                    javax.swing.Timer timer = new javax.swing.Timer(1000, ee -> {
                     
                    customLengthTextField.setEditable(true);
            
                    });
                    timer.setRepeats(false);
                    timer.start();
                    setLengthButton.setText("Set");
                    customLengthTextField.setText("Between 5-50!");
                    customLengthTextField.setEditable(false);
                     
                }

            } catch (NumberFormatException error) {
                
                javax.swing.Timer timer = new javax.swing.Timer(1000, ee -> {
                     
                    customLengthTextField.setEditable(true);
                    customLengthTextField.setText("(5-50");
                    customLengthTextField.setForeground(Color.GRAY);

                });

                timer.setRepeats(false);
                timer.start();
                setLengthButton.setText("Set");
                customLengthTextField.setText("Invalid number!");
                customLengthTextField.setEditable(false);
            }
        });

        JLabel seatCountText = new JLabel("Seat Count:");
        seatCountText.setBounds(37, 179, 106, 18);
        seatCountText.setFont(new Font("Arial", Font.PLAIN,  14));
        seatCountText.setForeground(Color.decode("#1b1b1b"));
        mainPanel.add(seatCountText);

        Integer[] seats = {2,3,4,5,6};
        seatsOption = new JComboBox<Integer>(seats);
        seatsOption.setSelectedItem(2);
        seatsOption.setBounds(35, 71, 100, 21);
        seatsOption.setLocation(37,210);
        mainPanel.add(seatsOption);

        numberOfSeatsText = new JLabel(seatsOption.getSelectedItem().toString() + "🗸");
        numberOfSeatsText.setBounds(150, 210, 106, 18);
        numberOfSeatsText.setForeground(Color.decode("#1b1b1b"));
        mainPanel.add(numberOfSeatsText);

        seatsOption.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e)
            {
                numberOfSeatsText.setText(seatsOption.getSelectedItem().toString() + "🗸");
                numberOfTypists = Integer.parseInt(seatsOption.getSelectedItem().toString());
            }
        });
        JLabel difficultyModifiersText = new JLabel("Difficulty Modifiers");
        difficultyModifiersText.setBounds(411, 68, 147, 21);
        difficultyModifiersText.setFont(new Font("Arial", Font.BOLD,  14));
        difficultyModifiersText.setForeground(Color.decode("#434343"));
        mainPanel.add(difficultyModifiersText);

        JLabel autocorrectText = new JLabel("Autocorrect:");
        autocorrectText.setBounds(304, 97, 106, 18);
        autocorrectText.setFont(new Font("Arial", Font.PLAIN,  14));
        autocorrectText.setForeground(Color.decode("#1b1b1b"));
        mainPanel.add(autocorrectText);

        autocorrectButton = new JButton("OFF");
        autocorrectButton.setBounds(401, 89, 42, 39);
        autocorrectButton.setBackground(Color.decode("#dd3333"));
        autocorrectButton.setForeground(Color.decode("#1b1b1b"));
        autocorrectButton.setFont(new Font("Arial", Font.PLAIN,  14));
        autocorrectButton.setFocusPainted(false);
        mainPanel.add(autocorrectButton);

        autocorrectButton.addActionListener(e -> {
            if (autocorrectButton.getText().toString().equals("ON"))
            {
                autocorrectButton.setBackground(Color.decode("#dd3333"));
                autocorrectButton.setText("OFF");
                
            }
            else
            {
                autocorrectButton.setBackground(Color.decode("#33dd8a"));
                autocorrectButton.setText("ON");
            }
        });

        JLabel caffeineModeText = new JLabel("Caffeine Mode:");
        caffeineModeText.setBounds(468, 94, 106, 18);
        caffeineModeText.setFont(new Font("Arial", Font.PLAIN, 14));
        caffeineModeText.setForeground(Color.decode("#1b1b1b"));
        mainPanel.add(caffeineModeText);

        caffeineButton = new JButton("OFF");
        caffeineButton.setBounds(582, 84, 42, 39);
        caffeineButton.setBackground(Color.decode("#dd3333"));
        caffeineButton.setForeground(Color.decode("#1b1b1b"));
        caffeineButton.setFont(new Font("Arial", Font.PLAIN,  14));
        caffeineButton.setFocusPainted(false);
        mainPanel.add(caffeineButton);

        caffeineButton.addActionListener(e -> {
            if (caffeineButton.getText().toString().equals("ON"))
            {
                caffeineButton.setText("OFF");
                caffeineButton.setBackground(Color.decode("#dd3333"));
            }
            else
            {
                caffeineButton.setText("ON");
                caffeineButton.setBackground(Color.decode("#33dd8a"));
            }
        });

        JLabel nightShiftText = new JLabel("Night Shift:");
        nightShiftText.setBounds(376, 133, 106, 18);
        nightShiftText.setFont(new Font("Arial", Font.PLAIN, 14));
        nightShiftText.setForeground(Color.decode("#1b1b1b"));
        mainPanel.add(nightShiftText);

        nightShiftButton = new JButton("OFF");
        nightShiftButton.setBounds(457, 121, 42, 39);
        nightShiftButton.setBackground(Color.decode("#dd3333"));
        nightShiftButton.setForeground(Color.decode("#1b1b1b"));
        nightShiftButton.setFont(new Font("Arial", Font.PLAIN, 14));
        nightShiftButton.setFocusPainted(false);
        mainPanel.add(nightShiftButton);
        
        nightShiftButton.addActionListener(e ->{
            if (nightShiftButton.getText().toString().equals("ON"))
            {
                nightShiftButton.setText("OFF");
                nightShiftButton.setBackground(Color.decode("#dd3333"));
            }
            else
            {
                nightShiftButton.setText("ON");
                nightShiftButton.setBackground(Color.decode("#33dd8a"));
            }
        });

        JTextArea autocorrectInfoText = new JTextArea("Autocorrect: When enabled, the slideBack amount is halved, simulating  modern phone keyboards. ");
        autocorrectInfoText.setBounds(306, 160, 327, 56);
        autocorrectInfoText.setFont(new Font("Arial", Font.PLAIN, 14));
        autocorrectInfoText.setForeground(Color.decode("#1b1b1b"));
        autocorrectInfoText.setLineWrap(true);
        autocorrectInfoText.setWrapStyleWord(true);
        autocorrectInfoText.setEditable(false);
        mainPanel.add(autocorrectInfoText);

        JTextArea caffieneInfoText = new JTextArea("Caffeine Mode: All typists gain a temporary speed boost for the first 10 turns,  followed by increased burnout risk.");
        caffieneInfoText.setBounds(305, 220, 312, 55);
        caffieneInfoText.setFont(new Font("Arial", Font.PLAIN, 14));
        caffieneInfoText.setForeground(Color.decode("#1b1b1b"));
        caffieneInfoText.setLineWrap(true);
        caffieneInfoText.setWrapStyleWord(true);
        caffieneInfoText.setEditable(false);
        mainPanel.add(caffieneInfoText);

        JTextArea nightShiftInfoText = new JTextArea("Night Shift: Accuracy ratings are slightly reduced across the board: everyone is tired. ");
        nightShiftInfoText.setBounds(306, 274, 337, 38);
        nightShiftInfoText.setFont(new Font("Arial", Font.PLAIN, 14));
        nightShiftInfoText.setForeground(Color.decode("#1b1b1b"));
        nightShiftInfoText.setLineWrap(true);
        nightShiftInfoText.setWrapStyleWord(true);
        nightShiftInfoText.setEditable(false);
        mainPanel.add(nightShiftInfoText);

        JButton continue1 = new JButton("Continue");
        continue1.setBounds(86, 280, 106, 30);
        continue1.setBackground(Color.decode("#ffffff"));
        continue1.setForeground(Color.decode("#1b1b1b"));
        continue1.setFont(new Font("Arial", Font.PLAIN, 14));
        continue1.setFocusPainted(false);
        mainPanel.add(continue1);

        //Customisation
        JPanel customisationPanel = new JPanel(layout);
        customisationPanel.setBackground(Color.decode("#eeeeee"));
        customisationPanel.setLayout(null);


        JLabel customisationHeading = new JLabel("Customisation Menu");
        customisationHeading.setBounds(250, 31, 300, 30);
        customisationHeading.setFont(new Font("Arial", Font.ITALIC, 18));
        customisationHeading.setForeground(Color.decode("#ff7146"));
        customisationPanel.add(customisationHeading);

        JLabel typingStyleText = new JLabel("Typing Style:");
        typingStyleText.setBounds(16, 71, 106, 18);
        typingStyleText.setFont(new Font("Arial", Font.PLAIN, 14));
        typingStyleText.setForeground(Color.decode("#1b1b1b"));
        customisationPanel.add(typingStyleText);

        String styles[] = {"Touch Typist", "Hunt & Peck", "Phone Thumbs", "Voice-to-Text"};
        styleOption = new JComboBox<String>(styles);
        styleOption.setBounds(20, 100, 140, 21);
        customisationPanel.add(styleOption);

        stylesInfo = new JTextArea("+ no change accuracy rating; " + "+" +  "0.05 burnout risk");
        stylesInfo.setBounds(20, 124, 106, 18);
        stylesInfo.setFont(new Font("Arial", Font.PLAIN, 8));
        stylesInfo.setForeground(Color.decode("#2bc36b"));
        stylesInfo.setLineWrap(true);
        stylesInfo.setWrapStyleWord(true);
        stylesInfo.setEditable(false);
        customisationPanel.add(stylesInfo);
    }
}
