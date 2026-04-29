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
public class TypingGUI {
    private static int passageLength;   // Total characters in the passage to type

    private static CardLayout layout; // Allows flipping through each page/section of GUI
    private static JPanel card;
    private static ArrayList<JTextArea> bars = new ArrayList<>();
    private static ArrayList<JTextArea> typiststatus = new ArrayList<>();
    private static ArrayList<Typist2> typists = new ArrayList<>();
    private static ArrayList<Typist2> rankedtypists = new ArrayList<>();
    private static ArrayList<Typist2> winnerHistory = new ArrayList<>();
    private static int numberOfTypists = 0;
    private static Typist2 winnerTypist;

    // Original accuracy thresholds for mistype and burnout events
    private static final double MISTYPE_BASE_CHANCE = 0.3;
    private static final int    SLIDE_BACK_AMOUNT   = 2;
    private static final int    BURNOUT_DURATION     = 3;
    //Wait 200ms between turns so the animation is visible
    private static final int SPEED = 200;
    
    // difficulty modifier and attribute impact values for mistype and burnout events
    private static final double NIGHT_SHIFT_ACCURACY = 0.2;
    private static final int CAFFEINE_SPEED_BOOST = 50;
    private static final int WRIST_SUPPORT_BURNOUT = 1;
    private static final double NOISE_CANCELLING_MISTYPE_CHANCE = 0.15;
    private static final double BURNOUT_RISK_CAFFEINE = 0.08;
    private static final double ENERGY_DRINK_ACCURACY = 0.3;

    // Updated accuracy thresholds after applying with difficulty modifiers and accessories for mistype and burnout events
    private static double NEW_MISTYPE_CHANCE = MISTYPE_BASE_CHANCE;
    private static int NEW_SLIDE_BACK = SLIDE_BACK_AMOUNT;
    private static int NEW_BURNOUT_DURATION = BURNOUT_DURATION;
    private static int NEW_SPEED = SPEED;
    private static int caffeineTurns = 0;

    // Dropdown list that holds values for passagelength, number of seats, typing style, keyboard type and symbols
    static JComboBox<String> lengthOption;
    static JComboBox<Integer> seatsOption;
    static JComboBox<String> styleOption;
    static JComboBox<String> keyboardType;
    static JComboBox<String> symbolOption;

    static Color progressBarColourSet; // Stores RGB colour of progress bar

    // GUI components that text values will be used across the program
    static JLabel selectedLength;
    static JLabel numberOfSeatsText;
    static JTextArea keyboardInfo;
    static JTextArea stylesInfo;

    static JButton autocorrectButton;
    static JButton nightShiftButton;
    static JButton caffeineButton;
    static JButton wristSupportButton;
    static JButton energyDrinkButton;
    static JButton noiseCHButton;

    static JSlider slider; // Race history slider per frame

    //Match timer
    static Timer time;
    static long startTime; // stores starting time
    static long finishTime; // stores finishing time
    static boolean finished = false;

    /**
     * halves slide back amount duration
     * 
     */
    private static void autocorrectON()
    {

        if (autocorrectButton.getText().equals("ON"))
        {
            NEW_SLIDE_BACK /= 2;
        }
    }

    /**
     * allow speed boost followed by increased burnout risk
     * 
     * @param speedvalue how much to increase speed
     */
    private static void caffeineModeON()
    {

        if (caffeineButton.getText().equals("ON"))
        {
            NEW_SPEED -= CAFFEINE_SPEED_BOOST;
            NEW_BURNOUT_DURATION += BURNOUT_RISK_CAFFEINE;
        }
    }

    /**
     * Sets the typing style of the race from a predefined list.
     * Incfluences the accuracy and burnout profile.
     * 
     */
    private static void setTypingStyle()
    {
        double accuracy = 0.0;

        for (Typist2 typist : typists)
        {
            if (styleOption.getSelectedItem().toString().equals("Touch Typist"))
            {
                stylesInfo.setText("+ no change accuracy rating; " + "+0.05 burnout risk");
            }
            else if (styleOption.getSelectedItem().toString().equals("Hunt & Peck"))
            {
                accuracy += 0.2;
                typist.setOldAccuracy(typist.getAccuracy() + accuracy);
                
            }
            else if (styleOption.getSelectedItem().toString().equals("Phone Thumbs"))
            {
                accuracy += 0.2;
                typist.setOldAccuracy(typist.getAccuracy() + accuracy);
                
            }
            else
            {
                accuracy += 0.5;
                typist.setOldAccuracy(typist.getAccuracy() + accuracy);
                
            }
        }
    }


/**
 * 
 * Sets keyboard type from a list of different keyboards (text of JCombobox keyboardType).
 * Each has different effects on speed and mistype rates.
 * 
 */
    private static void setKeyboardType()
    {
        int speedChange = 0;
        double mistypeChance = 0;
        

        if (keyboardType.getSelectedItem().toString().equals("Mechanical"))
        {
            keyboardInfo.setText( "normal speed" + "; " + "+" + Math.round(MISTYPE_BASE_CHANCE * 100.0) / 100.0 +" mistype chance");
        }
        else if (keyboardType.getSelectedItem().toString().equals("Membrane"))
        {
            speedChange += 30;
            NEW_SPEED += speedChange;

            mistypeChance += 0.3;
            NEW_MISTYPE_CHANCE -= mistypeChance;
        }
        else if (keyboardType.getSelectedItem().toString().equals("Touchscreen"))
        {
            speedChange += 35;
            NEW_SPEED -= speedChange;

            mistypeChance += 0.35;
            NEW_MISTYPE_CHANCE += mistypeChance;
        }
        else
        {
            speedChange += 10;
            NEW_SPEED += speedChange;

            mistypeChance += 0.1;
            NEW_MISTYPE_CHANCE -= mistypeChance;
        }
        
    }

    /**
     * reduces accuracy ratings slightly across the board 
     * and increases burnout duration (everyone is tired)
     * 
     * if night shift difficulty modifier is enabled
     */
    private static void  nightShiftON()
    {

        if (nightShiftButton.getText().equals("ON"))
        {
            for (Typist2 typist : typists)
            {
                typist.setAccuracy(typist.getAccuracy() - NIGHT_SHIFT_ACCURACY);
            }
            NEW_BURNOUT_DURATION += 1;
        }
    }
    

    /**
     * reduces burnout duration if wrist support accessory is enabled
     * 
     */
    private static void  wristSupportON()
    {

        if (wristSupportButton.getText().equals("ON"))
        {
            NEW_BURNOUT_DURATION  -= WRIST_SUPPORT_BURNOUT;
        }
    }

    /**
     * reduces chance to mistype
     * 
     * if noise cancelling headphones accessory is enabled.
     */
    private static void noiseCancellingHeadphonesON()
    {

        if (noiseCHButton.getText().equals("ON"))
        {
            NEW_MISTYPE_CHANCE  -= NOISE_CANCELLING_MISTYPE_CHANCE;
        }
    }


    /**
     * Applies global modifiers that affect all typists and optional add-ons that affect performance.
     * Sets up the race with a passage of given length
     * 
     * Initially there are no typists seated
     * Confirms total number of typists is set to the number of seats user has applied
     * 
     * Seats a typist at the given seat number and add given symbol set by user
     */
    public static void confirmChoices()
    {
        setKeyboardType();
        setTypingStyle();

        autocorrectON();
        noiseCancellingHeadphonesON();
        wristSupportON();
        caffeineModeON();
        nightShiftON();

        setPassageLength();

        numberOfTypists = Integer.parseInt(seatsOption.getSelectedItem().toString());
        
        int unicode = getUnicodeSymbol();
        addTypist(unicode);
    }

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
     * Returns the type integer unicode for the symbols set by the user
     * 
     * @return unicode symbol code
     */
    private static int getUnicodeSymbol()
    {
        int unicode = 0x2460;
        if (symbolOption.getSelectedItem().toString().equals("①②③"))
        {
            unicode = 0x2460;
        }
        else if (symbolOption.getSelectedItem().toString().equals("⑴⑵⑶"))
        {
            unicode = 0x2474;
        }
        else if (symbolOption.getSelectedItem().toString().equals("⓵⓶⓷"))
        {
            unicode = 0x24F5;
        }
        else if (symbolOption.getSelectedItem().toString().equals("♠♢♣♡"))
        {
            unicode = 0x2660;
        }
        else if (symbolOption.getSelectedItem().toString().equals("♳♴♵"))
        {
            unicode = 0x2673;
        }
        else if (symbolOption.getSelectedItem().toString().equals("⚀⚁⚂"))
        {
            unicode = 0x2680;
        }

        return unicode;
    }

    /**
     * Seats a typist and give a random initial accuracy from 0.10 - 0.50
     *
     * @param theTypist  the typist to seat
     * @param unicode the unicode symbol
     */
    public static void addTypist(int unicode)
    {
        for (Typist2 typist : typists)
        {
            typist.setSymbol((char) (unicode + typists.indexOf(typist)));
        }

        while (typists.size() < numberOfTypists)
        {
            double accuracy = Math.round((0.10 + Math.random()* 0.50) * 100) / 100.0; //2dp for consistency
            typists.add(new Typist2((char) (unicode + typists.size()), "N/A", accuracy));
        }
        while (typists.size() > numberOfTypists)
        {
            typists.remove(typists.size() - 1);
        }
    }


    /**
     * Displays graphical version of TypingRace
     * 
     * Creates main menu and customisation page.
     * Pages that user have to select options to set up the race
     * 
     */
    public static void startRace()
    {
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
        setLengthButton.setBounds(186, 142, 55, 29);
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

        //Difficulty Modifiers
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
        autocorrectButton.setBounds(401, 89, 60, 39);
        autocorrectButton.setBackground(Color.decode("#dd3333"));
        autocorrectButton.setForeground(Color.decode("#1b1b1b"));
        autocorrectButton.setFont(new Font("Arial", Font.PLAIN,  10));
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
        caffeineButton.setBounds(582, 84, 60, 39);
        caffeineButton.setBackground(Color.decode("#dd3333"));
        caffeineButton.setForeground(Color.decode("#1b1b1b"));
        caffeineButton.setFont(new Font("Arial", Font.PLAIN,  10));
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
        nightShiftButton.setBounds(482, 121, 60, 39);
        nightShiftButton.setBackground(Color.decode("#dd3333"));
        nightShiftButton.setForeground(Color.decode("#1b1b1b"));
        nightShiftButton.setFont(new Font("Arial", Font.PLAIN, 10));
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

        styleOption.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e)
            {
                if (styleOption.getSelectedItem().toString().equals("Touch Typist"))
                {
                    stylesInfo.setText("+ no change accuracy rating; " + "+0.05 burnout risk");
                }
                else if (styleOption.getSelectedItem().toString().equals("Hunt & Peck"))
                {
                    stylesInfo.setText("+" + "0.2" + " accuracy rating; " + "+0.06 burnout risk");
                }
                else if (styleOption.getSelectedItem().toString().equals("Phone Thumbs"))
                {
                    
                    stylesInfo.setText("-" + "0.2" + " accuracy rating; " + "+0.02 burnout risk");
                }
                else
                {
                    
                    stylesInfo.setText("+" + "0.5" + " accuracy rating; " + "+0.09 burnout risk");
                }
            }
        });

        JLabel keyboardTypeText = new JLabel("Keyboard Type:");
        keyboardTypeText.setBounds(16, 150, 106, 18);
        keyboardTypeText.setFont(new Font("Arial", Font.PLAIN, 14));
        keyboardTypeText.setForeground(Color.decode("#1b1b1b"));
        customisationPanel.add(keyboardTypeText);

        String keyboard[] = {"Mechanical", "Membrane", "Touchscreen", "Stenography"};
        keyboardType = new JComboBox<String>(keyboard);
        keyboardType.setBounds(20, 185, 112, 21);
        customisationPanel.add(keyboardType);

        keyboardInfo = new JTextArea("normal speed" + "; " + "+" + Math.round(MISTYPE_BASE_CHANCE * 100.0) / 100.0 +" mistype chance");
        keyboardInfo.setBounds(16, 208, 106, 18);
        keyboardInfo.setFont(new Font("Arial", Font.PLAIN, 8));
        keyboardInfo.setForeground(Color.decode("#2bc36b"));
        keyboardInfo.setLineWrap(true);
        keyboardInfo.setWrapStyleWord(true);
        keyboardInfo.setEditable(false);
        customisationPanel.add(keyboardInfo);

        keyboardType.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e)
            {
                int speedChange = 0;
                double mistypeChance = 0;
        

                if (keyboardType.getSelectedItem().toString().equals("Mechanical"))
                {
                    keyboardInfo.setText( "normal speed" + "; " + "+" + Math.round(MISTYPE_BASE_CHANCE * 100.0) / 100.0 +" mistype chance");
                }
                else if (keyboardType.getSelectedItem().toString().equals("Membrane"))
                {
                    speedChange += 30;

                    mistypeChance += 0.3;
                    keyboardInfo.setText("-" + speedChange + "ms; " + "+" + Math.round((NEW_MISTYPE_CHANCE - mistypeChance)* 100.0) / 100.0 +" mistype chance");
                }
                else if (keyboardType.getSelectedItem().toString().equals("Touchscreen"))
                {
                    speedChange += 35;

                    mistypeChance += 0.35;
                    keyboardInfo.setText("+" + speedChange + "ms; " + "+" + Math.round((NEW_MISTYPE_CHANCE + mistypeChance)* 100.0) / 100.0 +" mistype chance");
                }
                else
                {
                    speedChange += 10;

                    mistypeChance += 0.1;
                    keyboardInfo.setText("-" + speedChange + "ms; " + "+" + Math.round((NEW_MISTYPE_CHANCE - mistypeChance)* 100.0) / 100.0 +" mistype chance");
                }
            }
        });

        JLabel typistSymbolText = new JLabel("Typists' Symbol:");
        typistSymbolText.setBounds(197, 71, 106, 18);
        typistSymbolText.setFont(new Font("Arial", Font.PLAIN, 14));
        typistSymbolText.setForeground(Color.decode("#1b1b1b"));
        customisationPanel.add(typistSymbolText);

        String symbols[] = {"①②③", "⑴⑵⑶", "⓵⓶⓷", "♠♢♣♡","♳♴♵","⚀⚁⚂"};
        symbolOption = new JComboBox<String>(symbols);
        symbolOption.setBounds(210, 100, 130, 21);
        customisationPanel.add(symbolOption);

        JLabel progressBarColour = new JLabel("Progress Bar Colour:");
        progressBarColour.setBounds(197, 146, 141, 18);
        progressBarColour.setFont(new Font("Arial", Font.PLAIN, 14));;
        progressBarColour.setForeground(Color.decode("#1b1b1b"));
        customisationPanel.add(progressBarColour);

        JButton colourChangeButton = new JButton("Selected Colour: N/A");
        colourChangeButton.setBounds(197, 180, 200, 30);
        colourChangeButton.setBackground(Color.decode("#ffffff"));
        colourChangeButton.setForeground(Color.decode("#1b1b1b"));
        colourChangeButton.setFont(new Font("Arial", Font.PLAIN, 14));
        colourChangeButton.setFocusPainted(false);
        customisationPanel.add(colourChangeButton);

        colourChangeButton.addActionListener(e ->{
            progressBarColourSet = JColorChooser.showDialog(null, "Pick a Colour", Color.WHITE);
            setOnClickColour(colourChangeButton,Color.decode("#7a7a7a"), Color.decode("#ffffff"));
            
            if (progressBarColourSet != null)
            {
                colourChangeButton.setText("Selected Colour: " + getColourName(progressBarColourSet));
            }
        });

        JLabel setTypistNameText = new JLabel("Set Typists Names:");
        setTypistNameText.setBounds(197, 210, 141, 18);
        setTypistNameText.setFont(new Font("Arial", Font.PLAIN, 14));;
        setTypistNameText.setForeground(Color.decode("#1b1b1b"));
        customisationPanel.add(setTypistNameText);

        JButton setNameButton = new JButton("Set");
        setNameButton.setBounds(197, 230, 200, 30);
        setNameButton.setBackground(Color.decode("#ffffff"));
        setNameButton.setForeground(Color.decode("#1b1b1b"));
        setNameButton.setFont(new Font("Arial", Font.PLAIN, 14));
        setNameButton.setFocusPainted(false);
        customisationPanel.add(setNameButton);

        setNameButton.addActionListener(e ->{
            setOnClickColour(setNameButton,Color.decode("#7a7a7a"), Color.decode("#ffffff"));
            changeTypistsNames();
        });

        //Accessories
        JLabel accessoriesLabel = new JLabel("Accessories");
        accessoriesLabel.setBounds(475, 50, 147, 21);
        accessoriesLabel.setFont(new Font("Arial", Font.BOLD,  14));
        accessoriesLabel.setForeground(Color.decode("#434343"));
        customisationPanel.add(accessoriesLabel);

        wristSupportButton = new JButton("OFF");
        wristSupportButton.setBounds(536, 66, 106, 30);
        wristSupportButton.setBackground(Color.decode("#eb3251"));
        wristSupportButton.setForeground(Color.decode("#1b1b1b"));
        wristSupportButton.setFont(new Font("Arial", Font.PLAIN, 14));
        wristSupportButton.setFocusPainted(false);
        customisationPanel.add(wristSupportButton);

        wristSupportButton.addActionListener(e -> {
            if (wristSupportButton.getText().toString().equals("ON"))
            {
                wristSupportButton.setBackground(Color.decode("#dd3333"));
                wristSupportButton.setText("OFF");
                
            }
            else
            {
                wristSupportButton.setBackground(Color.decode("#33dd8a"));
                wristSupportButton.setText("ON");
            }
        });

        noiseCHButton = new JButton("OFF");
        noiseCHButton.setBounds(535, 124, 106, 30);
        noiseCHButton.setBackground(Color.decode("#eb3251"));
        noiseCHButton.setForeground(Color.decode("#1b1b1b"));
        noiseCHButton.setFont(new Font("Arial", Font.PLAIN, 14));
        noiseCHButton.setFocusPainted(false);
        customisationPanel.add(noiseCHButton);

        noiseCHButton.addActionListener(e -> {
            if (noiseCHButton.getText().toString().equals("ON"))
            {
                noiseCHButton.setBackground(Color.decode("#dd3333"));
                noiseCHButton.setText("OFF");
                
            }
            else
            {
                noiseCHButton.setBackground(Color.decode("#33dd8a"));
                noiseCHButton.setText("ON");
            }
        });

        energyDrinkButton = new JButton("OFF");
        energyDrinkButton.setBounds(535, 177, 106, 30);
        energyDrinkButton.setBackground(Color.decode("#eb3251"));
        energyDrinkButton.setForeground(Color.decode("#1b1b1b"));
        energyDrinkButton.setFont(new Font("Arial", Font.PLAIN, 14));
        energyDrinkButton.setFocusPainted(false);
        customisationPanel.add(energyDrinkButton);

        energyDrinkButton.addActionListener(e -> {
            if (energyDrinkButton.getText().toString().equals("ON"))
            {
                energyDrinkButton.setBackground(Color.decode("#dd3333"));
                energyDrinkButton.setText("OFF");
                
            }
            else
            {
                energyDrinkButton.setBackground(Color.decode("#33dd8a"));
                energyDrinkButton.setText("ON");
            }
        });

        JTextArea wristSupportInfoText = new JTextArea("Reduce burnout duration");
        wristSupportInfoText.setBounds(539, 104, 106, 18);
        wristSupportInfoText.setFont(new Font("Arial", Font.PLAIN, 8));
        wristSupportInfoText.setForeground(Color.decode("#1b1b1b"));
        wristSupportInfoText.setLineWrap(true);
        wristSupportInfoText.setWrapStyleWord(true);
        wristSupportInfoText.setEditable(false);
        customisationPanel.add(wristSupportInfoText);

        JTextArea energyDrinkInfoText = new JTextArea("Increase accuracy for the first half of the race, decrease it in the second half");
        energyDrinkInfoText.setBounds(537, 211, 104, 39);
        energyDrinkInfoText.setFont(new Font("Arial", Font.PLAIN, 8));
        energyDrinkInfoText.setForeground(Color.decode("#1b1b1b"));
        energyDrinkInfoText.setLineWrap(true);
        energyDrinkInfoText.setWrapStyleWord(true);
        energyDrinkInfoText.setEditable(false);
        customisationPanel.add(energyDrinkInfoText);

        JTextArea noiseCHInfoText = new JTextArea("Reduce the chance of a mistype");
        noiseCHInfoText.setBounds(539, 156, 106, 18);
        noiseCHInfoText.setFont(new Font("Arial", Font.PLAIN, 8));
        noiseCHInfoText.setForeground(Color.decode("#1b1b1b"));
        noiseCHInfoText.setLineWrap(true);
        noiseCHInfoText.setWrapStyleWord(true);
        noiseCHInfoText.setEditable(false);
        customisationPanel.add(noiseCHInfoText);

        JLabel wristSupportText = new JLabel("Wrist Support:");
        wristSupportText.setBounds(416, 69, 106, 18);
        wristSupportText.setFont(new Font("Arial", Font.PLAIN, 14));
        wristSupportText.setForeground(Color.decode("#1b1b1b"));
        customisationPanel.add(wristSupportText);

        JLabel noiseCText = new JLabel("Noise-Cancelling");
        noiseCText.setBounds(416, 122, 130, 37);
        noiseCText.setFont(new Font("Arial", Font.PLAIN, 14));;
        noiseCText.setForeground(Color.decode("#1b1b1b"));
        customisationPanel.add(noiseCText);

        JLabel noiseHText = new JLabel("Headphones:");
        noiseHText.setBounds(416, 140, 130, 37);
        noiseHText.setFont(new Font("Arial", Font.PLAIN, 14));;
        noiseHText.setForeground(Color.decode("#1b1b1b"));
        customisationPanel.add(noiseHText);

        JLabel energyDrinkText = new JLabel("Energy Drink:");
        energyDrinkText.setBounds(416, 185, 106, 18);
        energyDrinkText.setFont(new Font("Arial", Font.PLAIN, 14));
        energyDrinkText.setForeground(Color.decode("#1b1b1b"));
        customisationPanel.add(energyDrinkText);

        JButton continue2 = new JButton("Start Race");
        continue2.setBounds(86, 280, 106, 30);
        continue2.setBackground(Color.decode("#ffffff"));
        continue2.setForeground(Color.decode("#1b1b1b"));
        continue2.setFont(new Font("Arial", Font.PLAIN, 14));
        continue2.setFocusPainted(false);
        customisationPanel.add(continue2);

        JButton backButton = new JButton("Back");
        backButton.setBounds(18, 280, 65, 30);
        backButton.setBackground(Color.decode("#ffffff"));
        backButton.setForeground(Color.decode("#1b1b1b"));
        backButton.setFont(new Font("Arial", Font.PLAIN, 10));
        backButton.setFocusPainted(false);
        customisationPanel.add(backButton);
        
        //Race Display
        JPanel racePanel = new JPanel(layout);
        racePanel.setBackground(Color.decode("#eeeeee"));
        racePanel.setLayout(new BoxLayout(racePanel, BoxLayout.Y_AXIS));
        racePanel.setBorder(BorderFactory.createEmptyBorder(0,0,1,0));

        card.add(mainPanel, "main");
        card.add(customisationPanel,"customise");
        card.add(racePanel, "race");


        continue1.addActionListener(e ->{
            setOnClickColour(continue1,Color.decode("#7a7a7a"), Color.decode("#ffffff"));
            layout.show(card, "customise");

            setPassageLength();

            numberOfTypists = Integer.parseInt(seatsOption.getSelectedItem().toString());
        
            int unicode = getUnicodeSymbol();
            addTypist(unicode);
        });
        continue2.addActionListener(e ->{
            setOnClickColour(continue2,Color.decode("#7a7a7a"), Color.decode("#ffffff"));
            confirmChoices();
            racePanel.removeAll();
            layout.show(card, "race");
            racePanel.add(SymbolDetails());
            racePanel.add(ModifiersDetails());
            racePanel.add(AccessoriesDetails());
            for (int i = 0; i < numberOfTypists; i++)
            {
                Typist2 theTypist = typists.get(i);
                racePanel.add(createProgressBar(theTypist));
            }

            racePanel.revalidate();
            racePanel.repaint();

            Race();
        });
        backButton.addActionListener(e ->{
            setOnClickColour(backButton,Color.decode("#7a7a7a"), Color.decode("#ffffff"));
            layout.show(card, "main");
        });
        
        frame.add(card, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    /**
     * Starts the typing race.
     * the simulation runs
     * turn by turn until one typist completes the full passage.
     *
     * if caffeine mode is set to true, each typist have a temporary speed boost
     * with increased burnout risk for first 10 turns
     * 
     */
    public static void Race()
    {
        finished = false;
         for (Typist2 typist : typists)
        {
            typist.resetToStart();
        }
        
        
        time = new Timer(NEW_SPEED, e-> {
            if (!finished) {
                for (Typist2 typist : typists)
                {
                    typist.setWPM(0, startTime);
                    if (caffeineButton.getText().equals("ON"))
                    {
                        caffeineTurns++;

                        if (caffeineTurns >= 11)
                        {
                            NEW_SPEED = SPEED;
                        }
                        
                    }

                    if (!finished)
                    {
                        advanceTypist(typist);

                        // Print the current state of the race
                    
                        printRace();
                    }
                }
            }


                
        });
        startTime = System.currentTimeMillis(); // records starting time
        time.start();

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
     * If caffeine mode is ON, an additional burnout duration + burnout risk
     * Each tying style affects probability scales capped by certain amounts
     * 
     * 
     * @param theTypist the typist to advance
     */
    private static void advanceTypist(Typist2 theTypist)
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

        // Mistype check — the probability should reflect the typist's accuracy)
        if (Math.random() < theTypist.getAccuracy() * NEW_MISTYPE_CHANCE)
        {
            theTypist.slideBack(NEW_SLIDE_BACK);
        }

        // Burnout check — pushing too hard increases burnout risk
        // (probability scales with accuracy squared, capped at ~0.05)
        if (caffeineButton.getText().equals("ON"))
        {
            if (styleOption.getSelectedItem().toString().equals("Touch Typist"))
            {
                if (Math.random() < (0.05 + BURNOUT_RISK_CAFFEINE) * theTypist.getAccuracy() * theTypist.getAccuracy())
                {
                    theTypist.burnOut(NEW_BURNOUT_DURATION);
                    theTypist.addBurnoutCount();
                }
            }
            else if (styleOption.getSelectedItem().toString().equals("Hunt & Peck"))
            {
                if (Math.random() < (0.06 + BURNOUT_RISK_CAFFEINE) * theTypist.getAccuracy() * theTypist.getAccuracy())
                {
                    theTypist.burnOut(NEW_BURNOUT_DURATION);
                    theTypist.addBurnoutCount();
                } 
            }
            else if (styleOption.getSelectedItem().toString().equals("Phone Thumbs"))
            {
                if (Math.random() < (0.02 + BURNOUT_RISK_CAFFEINE) * theTypist.getAccuracy() * theTypist.getAccuracy())
                {
                    theTypist.burnOut(NEW_BURNOUT_DURATION);
                    theTypist.addBurnoutCount();
                }
            }
            else
            {
                if (Math.random() < (BURNOUT_RISK_CAFFEINE + 0.08) * theTypist.getAccuracy() * theTypist.getAccuracy())
                {
                    theTypist.burnOut(NEW_BURNOUT_DURATION);
                    theTypist.addBurnoutCount();
                }
            }
        }
        else
        {
             if (styleOption.getSelectedItem().toString().equals("Touch Typist"))
            {
                if (Math.random() < 0.05 * theTypist.getAccuracy() * theTypist.getAccuracy())
                {
                    theTypist.burnOut(NEW_BURNOUT_DURATION);
                    theTypist.addBurnoutCount();
                }
            }
            else if (styleOption.getSelectedItem().toString().equals("Hunt & Peck"))
            {
                if (Math.random() < 0.06 * theTypist.getAccuracy() * theTypist.getAccuracy())
                {
                    theTypist.burnOut(NEW_BURNOUT_DURATION);
                    theTypist.addBurnoutCount();
                } 
            }
            else if (styleOption.getSelectedItem().toString().equals("Phone Thumbs"))
            {
                if (Math.random() < 0.02 * theTypist.getAccuracy() * theTypist.getAccuracy())
                {
                    theTypist.burnOut(NEW_BURNOUT_DURATION);
                    theTypist.addBurnoutCount();
                }
            }
            else
            {
                if (Math.random() < 0.08 * theTypist.getAccuracy() * theTypist.getAccuracy())
                {
                    theTypist.burnOut(NEW_BURNOUT_DURATION);
                    theTypist.addBurnoutCount();
                }
            }
        }
    }

    /**
     * Displays the current state of the race to the GUI.
     * Shows each typist's position along the passage, burnout state,
     * and a WPM estimate based on current progress.
     */
    private static void printRace()
    {

        /**try {
                TimeUnit.MILLISECONDS.sleep(NEW_SPEED);
            } catch (Exception e) {}*/


        for (Typist2 typist : typists)
        {
            printSeat(typist);
            typist.setWPM(System.currentTimeMillis(), startTime);

            raceFinishedBy(typist);
            
        }
    }

    /**
     * Creates a row to represent a typist's details and performance for the leaderboard
     * 
     * Background color of Gold for 1st place, Silver for 2nd place, Bronze for 3rd place
     * Consists of 8 columns, rank no., badges achieved, typist name, total points, final accuracy, accuracy %, burnouts, personal best WPM, WPM from the race)
     * @return the created row
    */
    private static JPanel leaderboardRowCreate(Typist2 theTypist)
    {
        JPanel row = new JPanel(new GridLayout(1,8));
        row.setPreferredSize(new Dimension(220,40));
        
        if (rankedtypists.indexOf(theTypist) == 0)
        {
            row.setBackground(Color.decode("#bca45b"));
        }
        else if (rankedtypists.indexOf(theTypist) == 1)
        {
            row.setBackground(Color.decode("#7d7d7d"));
        }
        else if (rankedtypists.indexOf(theTypist) == 2)
        {
            row.setBackground(Color.decode("#a27e64"));
        }
        else
        {
            row.setBackground(Color.decode("#1c1c1c"));   
        }

        JLabel rank = new JLabel();
        rank.setText(Integer.toString(rankedtypists.indexOf(theTypist) + 1));
        rank.setForeground(Color.decode("#D9D9D9"));
        rank.setPreferredSize(new Dimension(2,40));
        row.add(rank);
        JLabel badge = new JLabel();
        if (theTypist.getBadges().isEmpty()) {
            badge.setText("N/A"); }
        else {
            badge.setText(theTypist.getBadges().toString());
        }
        badge.setForeground(Color.decode("#D9D9D9"));
        badge.setPreferredSize(new Dimension(5,40));
        row.add(badge);
        if (theTypist.getName().equals("N/A"))
        {
            JTextArea name = new JTextArea(String.valueOf(theTypist.getSymbol()));
            name.setForeground(Color.decode("#ffffff"));
            name.setPreferredSize(new Dimension(8,40));
            name.setWrapStyleWord(true);
            name.setLineWrap(true);
            name.setEditable(false);
            name.setOpaque(false);
            row.add(name);
        }
        else {
            JTextArea name = new JTextArea(theTypist.getName());
            name.setForeground(Color.decode("#ffffff"));
            name.setPreferredSize(new Dimension(8,40));
            name.setWrapStyleWord(true);
            name.setLineWrap(true);
            name.setEditable(false);
            name.setOpaque(false);
            row.add(name);
        }
        JTextArea points = new JTextArea(Integer.toString(theTypist.getPoints()));
        points.setForeground(Color.decode("#ffffff"));
        points.setPreferredSize(new Dimension(8,40));
        points.setWrapStyleWord(true);
        points.setLineWrap(true);
        points.setEditable(false);
        points.setOpaque(false);
        row.add(points);
        JTextArea accuracy = new JTextArea(String.valueOf(theTypist.getAccuracy()) + " (Improved from: " + theTypist.getOldAccuracy() + ")");
        accuracy.setForeground(Color.decode("#ffffff"));
        accuracy.setFont(new Font("Arial", Font.PLAIN,  7));
        accuracy.setPreferredSize(new Dimension(8,40));
        accuracy.setWrapStyleWord(true);
        accuracy.setLineWrap(true);
        accuracy.setEditable(false);
        accuracy.setOpaque(false);
        row.add(accuracy);
        JTextArea burnouts = new JTextArea(String.valueOf(theTypist.getBurnoutsCount()));
        burnouts.setForeground(Color.decode("#ffffff"));
        burnouts.setPreferredSize(new Dimension(8,40));
        burnouts.setWrapStyleWord(true);
        burnouts.setLineWrap(true);
        burnouts.setEditable(false);
        burnouts.setOpaque(false);
        row.add(burnouts);
        JTextArea WPM = new JTextArea(String.valueOf(theTypist.getfinalWPM(finishTime, startTime)));
        WPM.setForeground(Color.decode("#ffffff"));
        WPM.setPreferredSize(new Dimension(8,40));
        WPM.setWrapStyleWord(true);
        WPM.setLineWrap(true);
        WPM.setEditable(false);
        WPM.setOpaque(false);
        row.add(WPM);
        JTextArea personalBest = new JTextArea(String.valueOf(theTypist.getPersonalBest()));
        personalBest.setForeground(Color.decode("#D9D9D9"));
        personalBest.setPreferredSize(new Dimension(8,40));
        personalBest.setWrapStyleWord(true);
        personalBest.setLineWrap(true);
        personalBest.setEditable(false);
        personalBest.setOpaque(false);
        row.add(personalBest);
        JTextArea accuracypercentage = new JTextArea(String.valueOf(theTypist.getAccuracyPercentage()));
        accuracypercentage.setForeground(Color.decode("#D9D9D9"));
        accuracypercentage.setPreferredSize(new Dimension(8,40));
        accuracypercentage.setWrapStyleWord(true);
        accuracypercentage.setLineWrap(true);
        accuracypercentage.setEditable(false);
        accuracypercentage.setOpaque(false);
        row.add(accuracypercentage);

        return row;
    }

    /**
     * Changing the components(buttons) background when it's clicked
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
     * Creates the row of the headings for the leaderboard, ranks, badge etc.
     * @return the created row
     */
    private static JPanel leaderboardRowHeadings()
    {
        JPanel row = new JPanel(new GridLayout(1,8));
        row.setSize(220,40);
        
        JLabel rank = new JLabel();
        rank.setText("Rank");
        rank.setForeground(Color.decode("#000000"));
        rank.setPreferredSize(new Dimension(2,40));
        row.add(rank);
        JLabel badge = new JLabel();
        badge.setText("Badge");
        badge.setForeground(Color.decode("#000000"));
        badge.setPreferredSize(new Dimension(5,40));
        row.add(badge);
        JTextArea name = new JTextArea("Name");
        name.setForeground(Color.decode("#000000"));
        name.setPreferredSize(new Dimension(8,40));
        name.setWrapStyleWord(true);
        name.setLineWrap(true);
        name.setEditable(false);
        name.setOpaque(false);
        row.add(name);
        JTextArea points = new JTextArea("Points");
        points.setForeground(Color.decode("#000000"));
        points.setPreferredSize(new Dimension(8,40));
        points.setWrapStyleWord(true);
        points.setLineWrap(true);
        points.setEditable(false);
        points.setOpaque(false);
        row.add(points);
        JTextArea accuracy = new JTextArea("Final Accuracy");
        accuracy.setForeground(Color.decode("#000000"));
        accuracy.setPreferredSize(new Dimension(8,40));
        accuracy.setFont(new Font("Arial", Font.PLAIN,  10));
        accuracy.setWrapStyleWord(true);
        accuracy.setLineWrap(true);
        accuracy.setEditable(false);
        accuracy.setOpaque(false);
        row.add(accuracy);
        JTextArea burnouts = new JTextArea("Burnouts");
        burnouts.setForeground(Color.decode("#000000"));
        burnouts.setPreferredSize(new Dimension(8,40));
        burnouts.setFont(new Font("Arial", Font.PLAIN,  8));
        burnouts.setWrapStyleWord(true);
        burnouts.setLineWrap(true);
        burnouts.setEditable(false);
        burnouts.setOpaque(false);
        row.add(burnouts);
        JTextArea WPM = new JTextArea("WPM");
        WPM.setForeground(Color.decode("#000000"));
        WPM.setPreferredSize(new Dimension(8,40));
        WPM.setWrapStyleWord(true);
        WPM.setLineWrap(true);
        WPM.setEditable(false);
        WPM.setOpaque(false);
        row.add(WPM);
        JTextArea personalBest = new JTextArea("Best WPM");
        personalBest.setForeground(Color.decode("#000000"));
        personalBest.setPreferredSize(new Dimension(8,40));
        personalBest.setWrapStyleWord(true);
        personalBest.setLineWrap(true);
        personalBest.setEditable(false);
        personalBest.setOpaque(false);
        row.add(personalBest);
        JTextArea accuracypercentage = new JTextArea("%Accuracy");
        accuracypercentage.setForeground(Color.decode("#000000"));
        accuracypercentage.setFont(new Font("Arial", Font.PLAIN,  8));
        accuracypercentage.setPreferredSize(new Dimension(8,40));
        accuracypercentage.setWrapStyleWord(true);
        accuracypercentage.setLineWrap(true);
        accuracypercentage.setEditable(false);
        accuracypercentage.setOpaque(false);
        row.add(accuracypercentage);

        return row;
    }

    /**
     * Calculates and give points to typists accounting for finishing position, WPM achieved and whether the typist burnt out
     */
    private static void rewardSystem()
    {
        for (Typist2 typist : rankedtypists)
        {
            boolean burnouts = typist.getGotBurntOut();
            int position = rankedtypists.indexOf(typist);

            if (position == 1) //1st place reward
            {
                typist.setPoints(3);
                typist.setAccuracy(typist.getAccuracy() + 0.08);
            }
            else if (position == 2) //2nd place reward
            {
                typist.setPoints(2);
                typist.setAccuracy(typist.getAccuracy() + 0.05);
            }
            else if (position == 3) //3rd place reward
            {
                typist.setPoints(1);
                typist.setAccuracy(typist.getAccuracy() + 0.02);
            }
            
            
            if (typist.getPersonalBest() > typist.getOldBestWPM())
            {
                typist.setPoints(1);
                typist.setAccuracy(typist.getAccuracy() + 0.01);
            }
            if (burnouts) // if burnt out then points reduced by 2
            {
                typist.setPoints(-2);
            }
            else
            {
                typist.setAccuracy(typist.getAccuracy() + 0.01);
            }
        }
    }


    /**
     * Returns true if the given typist has completed the full passage.
     * Creates history page for the GUI as a display of the full race history/trends over time.
     * Also creates the leaderboard page to show comparison view of all typists on a chosen metric (After each race, track cumulative points across races and maintain global leaderboard)
     *
     * @param theTypist the typist to check
     * @return true if their progress has reached or passed the passage length
     */
    private static void raceFinishedBy(Typist2 theTypist)
    {
        if (theTypist.getProgress() >= passageLength) //(FIXED) progress can overshoot so set to '>='
        {
            winnerTypist = theTypist; // assigns value to typist who won
            winnerTypist.gainWins();
            finishTime = System.currentTimeMillis();
            finished = true;
            System.out.println(winnerTypist.getName());

            winnerHistory.add(winnerTypist);
            if (winnerHistory.size() >= 3)
            {
                Typist2 winner2gamesago = winnerHistory.get(winnerHistory.size() - 3);
                Typist2 previouswinner = winnerHistory.get(winnerHistory.size() - 2);

                if (winner2gamesago == winnerTypist && previouswinner == winnerTypist)
                {
                    if (!winnerTypist.getBadges().contains("⚡"))
                    {
                        winnerTypist.addBadge("⚡"); // Add Speed demon badge if player has earned 3 consecutive wins
                    }
                }

            }
            rankedtypists = new ArrayList<>(typists);
            rankedtypists.sort((a,b) -> b.getProgress() - a.getProgress()); // re-orders descending

            //Comparison/Replay View
            JPanel historyPanel = new JPanel(layout);
            historyPanel.setBackground(Color.decode("#eeeeee"));
            historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
            historyPanel.setBorder(BorderFactory.createEmptyBorder(0,0,1,0));
           

            JPanel historyslider = new JPanel();
            JTextArea viewText = new JTextArea("Race History: ");
            viewText.setBounds(0, 40, 147, 21);
            viewText.setFont(new Font("Arial", Font.BOLD,  14));
            viewText.setForeground(Color.decode("#434343"));
            historyslider.add(viewText);

            slider = new JSlider();
            slider.setMaximum((winnerTypist.getHistory().size() - 1));
            slider.setValue(0);
            slider.setMajorTickSpacing(1);
            slider.setPaintTicks(true);
            slider.setSnapToTicks(true);
            slider.setBounds(40, 40, 106, 30);
            slider.setForeground(Color.decode("#52e3a4"));
            historyslider.add(slider);

            historyPanel.add(historyslider);

            JPanel replayBars = new JPanel(layout);
            replayBars.setBackground(Color.decode("#eeeeee"));
            replayBars.setLayout(new BoxLayout(replayBars, BoxLayout.Y_AXIS));
            replayBars.setBorder(BorderFactory.createEmptyBorder(0,0,1,0));
            historyPanel.add(replayBars);

            ReplaySystem(0, replayBars);

            slider.addChangeListener(e->{
                int value = slider.getValue();
                slider.setValue(value);

                //replayBars.removeAll();
                ReplaySystem(value, replayBars);
            });

            JButton continueButton = new JButton("Leaderboard>>");
            continueButton.setBounds(86, 280, 106, 30);
            continueButton.setBackground(Color.decode("#ffffff"));
            continueButton.setForeground(Color.decode("#1b1b1b"));
            continueButton.setFont(new Font("Arial", Font.PLAIN, 14));
            continueButton.setFocusPainted(false);
            replayBars.add(continueButton);

            continueButton.addActionListener(e ->{
                setOnClickColour(continueButton,Color.decode("#7a7a7a"), Color.decode("#ffffff"));
                JPanel leaderboardPanel = new JPanel(layout);
                leaderboardPanel.setBackground(Color.decode("#1e1e1e"));
                leaderboardPanel.setLayout(null);

                JLabel heading = new JLabel("Leaderboard");
                heading.setBounds(359, 19, 149, 34);
                heading.setFont(new Font("Arial", Font.BOLD,  25));
                heading.setForeground(Color.decode("#D9D9D9"));
                leaderboardPanel.add(heading);

                JButton mainmenuButton = new JButton("Main Menu>>");
                mainmenuButton.setBounds(500, 24, 106, 29);
                mainmenuButton.setFont(new Font("Arial", Font.ITALIC,  10));
                mainmenuButton.setBackground(Color.decode("#2e2e2e"));
                mainmenuButton.setForeground(Color.decode("#D9D9D9"));
                mainmenuButton.setFocusPainted(false);

                leaderboardPanel.add(mainmenuButton);

                mainmenuButton.addActionListener(ee ->{
                    layout.show(card, "main");
                    bars.clear();
                    typiststatus.clear();
                    winnerTypist = null;

                    setOnClickColour(mainmenuButton,Color.decode("#7a7a7a"), Color.decode("#ffffff"));

                });


                JPanel leaderboard = new JPanel();
                //leaderboard.setBounds(174, 69, 417, 229);
                leaderboard.setBackground(Color.decode("#373737"));
                leaderboard.setLayout(new GridLayout(0,1));
                //leaderboardPanel.add(leaderboard);

                leaderboard.add(leaderboardRowHeadings());
                for (Typist2 typist : rankedtypists)
                {
                    leaderboard.add(leaderboardRowCreate(typist));

                    if (typist.getRacesCompleted() >= 5 && typist.getTotalBurnouts() == 0)
                    {
                        if (!typist.getBadges().contains("☝"))
                        {
                            typist.addBadge("☝");
                        }
                    }

                    if (typist.getWins() >= 10 && !typist.getBadges().contains("☠"))
                    {
                        typist.addBadge("☠");
                    }
                }

                JScrollPane scroll = new JScrollPane(leaderboard);
                scroll.setBounds(174, 69, 417, 220);
                scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                leaderboardPanel.add(scroll);

                JTextArea badges = new JTextArea("⚡Speed Demon = 3 consecutive wins    ☝Iron Fingers = 5 races without a burnout     ☠Keyboard Smasher = 10 total wins             [Rank Awards:         +3pts = 1st place,         +2pts = 2nd place,        +1pt = 3rd place;         +1pt = new best personal;                 -2pts = burnt out during race]");
                badges.setBounds(12, 66, 124, 225);
                badges.setBackground(Color.decode("#B2B2B2"));
                badges.setForeground(Color.decode("#656565"));
                badges.setEditable(false);
                badges.setWrapStyleWord(true);
                badges.setLineWrap(true);
                leaderboardPanel.add(badges);

                JLabel badgesHeading = new JLabel("Badges");
                badgesHeading.setBounds(49, 46, 55, 18);
                heading.setFont(new Font("Arial", Font.BOLD,  14));
                badgesHeading.setForeground(Color.decode("#D9D9D9"));
                leaderboardPanel.add(badgesHeading);

                card.add(leaderboardPanel, "leaderboard");
                layout.show(card, "leaderboard");
            });

            card.add(historyPanel, "history");
            
            layout.show(card, "history");
            rewardSystem();
        }
    }

    /**
     * Returns the closest simple colour name from given RGB colour
     * @param colour
     * @return the Colour name and its brightness if given
     */
    public static String getColourName(Color colour)
    {
        int r = colour.getRed();
        int g = colour.getGreen();
        int b = colour.getBlue();

        int colourBrightness = r + g + b;
        String brightness = "";

        if (colourBrightness < 100) {
            brightness = "Dark ";
        }
        else if (colourBrightness > 600){
            brightness = "Light ";
        }

        if (r > g && r > b) {
            return brightness + "Red";}
        else if (g > r && g > b) {
            return brightness + "Green";}
        else if (b > r && b > g) {
            return "Blue";}

        else if (r == g && r > b) {
            return brightness + "Yellow";
        }
        else if (r == b && r > g) {
            return brightness+ "Purple";
        }
        else if (g == b && g > r) {
            return brightness + "Cyan";
        }

        return brightness + "Grey";
    }

    /**
     * Creates a new window that lists the seated typists and allow user to change their names
     * If left empty, window closes and a new window appears that says a "Name cannot be empty"
     */
    public static void changeTypistsNames()
    {
        JDialog dialog = new JDialog((JFrame) null, "Set Typists Names", true);
        dialog.setLayout(new BorderLayout());

        JPanel list = new JPanel();
        list.setLayout(new GridLayout(numberOfTypists, 1));

        JTextField[] fields = new JTextField[typists.size()];
        for (int t = 0; t < typists.size(); t++)
        {
            fields[t] = new JTextField(typists.get(t).getName());
            list.add(fields[t]);
        }

        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(ee -> {

            for (int i = 0; i < numberOfTypists; i++)
            {
                if (!fields[i].getText().trim().isEmpty())
                {
                    typists.get(i).setName(fields[i].getText());

                }
                else {JOptionPane.showMessageDialog(dialog, "Name cannot be empty!");}
            }
            dialog.dispose();
        });

        JScrollPane scroll = new JScrollPane(list);

        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(confirmButton, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

     /**
     * Prints a character a given number of times.
     *
     * @param aChar the character to print
     * @param times how many times to print it
     */
    private static String multiplePrint(char aChar, int times)
    {
        int i = 0;
        String s = "";
        while (i < times)
        {
            s += aChar;
            i = i + 1;
        }
        return s;
    }

    /**
     * updating a single typist's lane
     * @param theTypist
     */
    private static void printSeat(Typist2 theTypist)
    {
        int spacesBefore = theTypist.getProgress();
        int spacesAfter  = passageLength - theTypist.getProgress();
        String typistAccuracy = String.format("%.2f", theTypist.getAccuracy()); // for consistent formatting

        int index = typists.indexOf(theTypist);

        JTextArea seat = bars.get(index);
        JTextArea typistInfo = typiststatus.get(index);

        String passage = "|";

        passage += multiplePrint('◉', spacesBefore);
        seat.setText(passage);

        // Always show the typist's symbol so they can be identified on screen.
        // Append ◌ when burnt out so the state is visible without hiding identity.
        passage += theTypist.getSymbol();
        seat.setText(passage);
        if (theTypist.isBurntOut())
        {
            passage += "◌";
            seat.setText(passage);
            spacesAfter--; // symbol + ◌ together take two characters
        }

        if (!theTypist.isBurntOut() && theTypist.getProgressBeforeSlideBack() > theTypist.getProgress()) {

            seat.setText(passage);

            String s = "◍";
            passage += s;
            seat.setText(passage);
            spacesAfter--;
        }

        //If Energy Drink is enabled, first half increases accuracy then second half decreases it back to original
        if (energyDrinkButton.getText().equals("ON"))
        {
            if (spacesAfter >= passageLength/2) // Second half decrease accuracy back
            {
                theTypist.setAccuracy(theTypist.getOldAccuracy());
            }
            else // First half increases accuracy slightly
                theTypist.setAccuracy(theTypist.getAccuracy() + ENERGY_DRINK_ACCURACY);
        }

        passage += multiplePrint('○', spacesAfter);
        passage += "⚑";
        passage += " ";
        seat.setText(passage);
        theTypist.addHistory(new RaceHistory(spacesBefore, theTypist.getWPM(), theTypist.getAccuracy(), theTypist.isBurntOut(), theTypist.getProgressBeforeSlideBack()/** , theTypist.getAccuracyPercentage(passageLength)*/));


        //Print name and accuracy
        String info = "";
        if (theTypist.isBurntOut()) // if burnt out
        {

            info = theTypist.getName()
                + " (Accuracy: " + typistAccuracy + ")"
                + " BURNT OUT (" + theTypist.getBurnoutTurnsRemaining() + " turns)";
        }
        else if (!theTypist.isBurntOut() && theTypist.getProgressBeforeSlideBack() > theTypist.getProgress()) // if mistypes
        {
            info = theTypist.getName()
                + " (Accuracy: " + typistAccuracy + ")"
                + "  ← just mistyped";
        }
        else // neither burnt out nor mistyped
        {
           info = theTypist.getName()
                + " (Accuracy: " + typistAccuracy + ")";
        }
        typistInfo.setText(info);
        
    }

    /**
     * Creating a single typist's lane
     * 
     * @param theTypist the typist/seat
     * @return the lane (row)
     */
    public static JPanel createProgressBar(Typist2 theTypist)
    {

        JPanel row = new JPanel(new GridBagLayout());
        row.setPreferredSize(new Dimension(200,20));
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.fill = GridBagConstraints.HORIZONTAL;

        int spacesBefore = theTypist.getProgress();
        int spacesAfter  = passageLength - theTypist.getProgress();
        String typistAccuracy = String.format("%.2f", theTypist.getAccuracy()); // for consistent formatting

        String passage = "|";

        JTextArea seat = new JTextArea(passage);
        seat.setEditable(false);
        seat.setLineWrap(true);
        seat.setWrapStyleWord(true);
        seat.setForeground(progressBarColourSet);
        seat.setBounds(0, 0, passageLength, 10);


        g.weightx = 1;
        g.gridx = 0;
        row.add(seat, g);
        bars.add(seat);

        passage += multiplePrint('◉', spacesBefore);
        seat.setText(passage);

        // Always show the typist's symbol so they can be identified on screen.
        passage += theTypist.getSymbol();
        seat.setText(passage);

        passage += multiplePrint('○', spacesAfter);
        passage += "⚑";
        passage += " ";
        seat.setText(passage);

        g.gridx = 1;
        g.weightx = 0.3;
        String info = "";
        JTextArea typistInfo  = new JTextArea(info);
        typistInfo.setEditable(false);
        typistInfo.setLineWrap(true);
        typistInfo.setWrapStyleWord(true);
        typiststatus.add(typistInfo);

        //Print name and accuracy
        if (theTypist.isBurntOut()) // if burnt out
        {

            info = theTypist.getName()
                + " (Accuracy: " +typistAccuracy + ")"
                + " BURNT OUT (" + theTypist.getBurnoutTurnsRemaining() + " turns)";
        }
        else if (!theTypist.isBurntOut() && theTypist.getProgressBeforeSlideBack() > theTypist.getProgress()) // if mistypes
        {
            info = theTypist.getName()
                + " (Accuracy: " + typistAccuracy + ")"
                + "  ← just mistyped";
        }
        else // neither burnt out nor mistyped
        {
           info = theTypist.getName()
                + " (Accuracy: " + typistAccuracy + ")";
        }
        typistInfo.setText(info);
        row.add(typistInfo, g);
        
        return row;
    }

    /**
     * Creates a panel/row for race page to show the passage length and symbols to represent when a typist is burnt out or has just mistyped
     * ◌ (dotted cirecle) = burnt out and ◍ (circle with lines pattern inside) = just mistyped
     * @return the row of race details
     */
    public static JPanel SymbolDetails()
    {
        JPanel row = new JPanel();
        JTextArea lengthInfo = new JTextArea("passage length: " + passageLength + " chars ");
        lengthInfo.setBounds(0, 0, 106, 30);
        lengthInfo.setFont(new Font("Arial", Font.BOLD, 18));
        lengthInfo.setForeground(Color.decode("#ff7146"));
        row.add(lengthInfo);

        JTextArea symbolInfo = new JTextArea(" [◌] = burnt out    [◍] = just mistyped");
        symbolInfo.setBounds(30, 0, 106, 30);
        symbolInfo.setForeground(Color.decode("#ff7146"));
        row.add(symbolInfo);

        return row;
    }

    /**
     * Creates the row of applied difficulty modifiers
     * @return the row
     */
    public static JPanel ModifiersDetails()
    {
        JPanel row = new JPanel();

        JTextArea difficultyModifiersText = new JTextArea("Difficulty Modifiers");
        difficultyModifiersText.setBounds(0, 25, 147, 21);
        difficultyModifiersText.setFont(new Font("Arial", Font.BOLD,  14));
        difficultyModifiersText.setForeground(Color.decode("#434343"));
        row.add(difficultyModifiersText);

        JTextArea modifiers = new JTextArea("Autocorrect: " + autocorrectButton.getText() + " " + "Caffeine Mode: " + caffeineButton.getText() + " " + "Night Shift: " + nightShiftButton.getText());
        modifiers.setBounds(30, 0, 106, 30);
        modifiers.setForeground(Color.decode("#535353"));
        row.add(modifiers);

        return row;
    }

    /**
     * Creates the row of applied accessories
     * @return the row
     */
    public static JPanel AccessoriesDetails()
    {
        JPanel row = new JPanel();

        JTextArea accessoriesText = new JTextArea("Accessories");
        accessoriesText.setBounds(0, 40, 147, 21);
        accessoriesText.setFont(new Font("Arial", Font.BOLD,  14));
        accessoriesText.setForeground(Color.decode("#434343"));
        row.add(accessoriesText);

        JTextArea accessories = new JTextArea("Wrist Support: " + wristSupportButton.getText() + " " + "Energy Drink: " + energyDrinkButton.getText() + " " + "Noise-Cancelling Headphones: " + noiseCHButton.getText());
        accessories.setBounds(30, 40, 106, 30);
        accessories.setForeground(Color.decode("#535353"));
        row.add(accessories);
        return row;
    }

    /**
     * Creates a single typist's lane of the finished race
     * 
     * @param race a snapshot of the race
     * @param theTypist the typist
     * @return the lane (row)
     */
    private static JPanel createBarReplay(RaceHistory race, Typist2 theTypist)
    {
        int index = typists.indexOf(theTypist);

        JPanel row = new JPanel(new GridBagLayout());
        row.setPreferredSize(new Dimension(200,20));
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.fill = GridBagConstraints.HORIZONTAL;

        String passage = "|";

        JTextArea seat = bars.get(index);
        seat.setEditable(false);
        seat.setLineWrap(true);
        seat.setWrapStyleWord(true);
        seat.setForeground(progressBarColourSet);
        seat.setBounds(0, 0, passageLength, 10);

        g.weightx = 1;
        g.gridx = 0;
        row.add(seat, g);

        int spacesBefore = race.getPosition();
        int spacesAfter  = passageLength - race.getPosition();


        passage += multiplePrint('◉', spacesBefore);
        seat.setText(passage);

        // Always show the typist's symbol so they can be identified on screen.
        // Append ◌ when burnt out so the state is visible without hiding identity.
        passage += theTypist.getSymbol();
        seat.setText(passage);
        if (race.getBurnoutstate())
        {
            passage += "◌";
            seat.setText(passage);
            spacesAfter--; // symbol + ◌ together take two characters
        }

        if (!race.getBurnoutstate() && race.getSlidebackposition() > race.getPosition()) {

            String s = "◍";
            passage += s;
            seat.setText(passage);
            spacesAfter--;
        }

        passage += multiplePrint('○', spacesAfter);
        passage += "⚑";
        passage += " ";
        seat.setText(passage);

        g.gridx = 1;
        g.weightx = 0.3;
        String info = theTypist.getName()
                + " (WPM: " + race.getWPM() + ")"
                + " (Accuracy: " + race.getAccuracy() +")";
        JTextArea typistInfo  = typiststatus.get(index);
        typistInfo.setEditable(false);
        typistInfo.setLineWrap(true);
        typistInfo.setWrapStyleWord(true);
        typistInfo.setText(info);
        row.add(typistInfo, g);

        return row;
    }

    /**
     * Updating the single typist's lane of the finished race
     * @param race a snippet of the race
     * @param theTypist the typist
     */
    private static void updateBarReplay(RaceHistory race, Typist2 theTypist)
    {
        int index = typists.indexOf(theTypist);
        JTextArea seat = bars.get(index);
        JTextArea typistInfo = typiststatus.get(index);

        int spacesBefore = race.getPosition();
        int spacesAfter  = passageLength - race.getPosition();

        String passage = "|";

        passage += multiplePrint('◉', spacesBefore);
        seat.setText(passage);

        // Always show the typist's symbol so they can be identified on screen.
        // Append ◌ when burnt out so the state is visible without hiding identity.
        passage += theTypist.getSymbol();
        seat.setText(passage);
        if (race.getBurnoutstate())
        {
            passage += "◌";
            seat.setText(passage);
            spacesAfter--; // symbol + ◌ together take two characters
        }

        if (!race.getBurnoutstate() && race.getSlidebackposition() > race.getPosition()) {
            seat.setText(passage);

            String s = "◍";
            passage += s;
            seat.setText(passage);
            spacesAfter--;
        }

        passage += multiplePrint('○', spacesAfter);
        passage += "⚑";
        passage += " ";
        seat.setText(passage);

        String info = theTypist.getName()
                + " (WPM: " + race.getWPM() + ")"
                + " (Accuracy: " + race.getAccuracy() +")";

        //Print name and accuracy
        if (race.getBurnoutstate()) // if burnt out
        {

            info = theTypist.getName()
                + " (WPM: " + race.getWPM() + ")"
                + " (Accuracy: " + race.getAccuracy() +")"
                + " ←  BURNT OUT";
        }
        else if (!race.getBurnoutstate() && race.getSlidebackposition() > race.getPosition()) // if mistypes
        {
           info = theTypist.getName()
                + " (WPM: " + race.getWPM() + ")"
                + " (Accuracy: " + race.getAccuracy() +")"
                + "  ← just mistyped";
        }
        else // neither burnt out nor mistyped
        {
           info = theTypist.getName()
                + " (WPM: " + race.getWPM() + ")"
                + " (Accuracy: " + race.getAccuracy() +")";
        }
        typistInfo.setText(info);
        
        
    }

    /**
     * Creating and updating the lanes for each typist to display full race history over time
     * 
     * @param index the given time the user wants to view from the race
     * @param historyPanel the GUI history page
     */
    public static void ReplaySystem(int index, JPanel historyPanel)
    {
        if (historyPanel.getComponentCount() == 0) //if page is empty
        {
            for (int i = 0; i < typists.size(); i++)
            {
                Typist2 theTypist = typists.get(i);
                //JTextArea bar = bars.get(i);

                RaceHistory race = theTypist.getHistory().get(index);
                
                historyPanel.add(createBarReplay(race, theTypist));
            }
        }
        else // if page is set up already
        {
            for (int i = 0; i < typists.size(); i++)
            {
                Typist2 theTypist = typists.get(i);

                RaceHistory race = theTypist.getHistory().get(index);
                
               updateBarReplay(race, theTypist);
            }
        }

    }

    /**
     * The main method calls startRace() to set up GUI
     */
    public static void main(String[] args)
    {
        startRace();
    }

}