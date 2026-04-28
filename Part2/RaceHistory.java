 /**
 * The RaceHistory Class stores data of one frame of the race
 * Each typist has a history list that stores one frame to display full timeline
 * 
 * @author Audrielle Kaytlyn Myesha
 * @version 1
 */

public class RaceHistory {

    //5 fields that stores a certain data of a frame from the race
    private int position;
    private int slidebackposition;
    private double WPM;
    private double accuracy;
    private boolean burnoutstate;
    

    // Constructor of class Typist
    /**
     * Constructor for objects of class Typist.
     * Creates a new typist with a given symbol, name, and accuracy rating.
     *
     * @param position the typist's given position/progress
     * @param WPM    the typist's WPM
     * @param accuracy the typist's accuracy rating
     * @param burnoutstate the typist's burnout state
     * @param slidebackposition the typist's position when slid back
     */
    public RaceHistory(int position, double WPM, double accuracy, boolean burnoutstate, int slidebackposition)
    {
        this.position = position;
        this.WPM = WPM;
        this.accuracy = accuracy;
        this.burnoutstate = burnoutstate;
        this.slidebackposition = slidebackposition;
    }

    /**
     * Returns the progress/position of the typist
     * @return the position
     */
    public int getPosition()
    {
        return position;
    }

    /**
     * Returns the WPM of the typist
     * @return the WPM
     */
    public double getWPM()
    {
        return WPM;
    }

    /**
     * Returns the accuracy of the typist
     * @return the accuracy
     */
    public double getAccuracy()
    {
        return accuracy;
    }

    /**
     * Returns the burntout state of the typist
     * @return burnout state
     */
    public boolean getBurnoutstate()
    {
        return burnoutstate;
    }

    /**
     * Returns the slide back position for mistype symbol history
     * @return the slide back position
     */
    public int getSlidebackposition()
    {
        return slidebackposition;
    }
}
