package final_project;

/**
 *
 * @author Leena Abu Shmais && Raghad AlDubayyan Sliding Puzzle is one of the
 * oldest and most well known puzzles of all time. It challenges the player to
 * slide pieces along certain routes to establish a certain end-configuration.
 * the puzzle is usually divided into 12 blocks, with 1 empty place to allow
 * movement, only those blocks adjacent to the empty space can be moved, and the
 * goal is to form a complete picture or a certain combination.
 */
import java.awt.*; //point class to give fixed position coordinates to the buttons
import java.awt.event.*; //event listener for buttons
import java.awt.image.*; //
import java.io.*; //IOException handeling
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO; //to input and read image path stream. 
import javax.swing.*;
import sun.audio.*; //to play aa sound effect (TA-RA!) after player completes the puzzle 
import javax.swing.filechooser.FileNameExtensionFilter; //to prompt user to choose a file from directory

//define a designated class MyButton which will be used to style the buttons to the puzzle. All buttons will be instantiated from this class
class MyButton extends JButton {

    private boolean isEmptyBtn; //empty button is the last button with no picture, only those buttons adjacent to it will move.

    //define default constructor which will explicitly invoke its parent's constructor
    public MyButton() {

        super();

    }

    //define a parametarized constructor which initializes a type image, the one which will appear on each button 
    public MyButton(Image image) {

        super(new ImageIcon(image));

    }

    //setter
    public void setEmptyBtn() {

        isEmptyBtn = true;
    }

    //boolean method to check if current button is the empty aka last button.
    public boolean isEmptyBtn() {

        return isEmptyBtn;
    }
}
//gggg
//puzzle class
public class SlidingPuzzle extends JFrame {

    private JPanel panel;
    //buffered image class offers more flexability in manipulating images 
    private BufferedImage original; //original image
    private BufferedImage resized;// resized to fit puzzle screen 
    private Image image;
    private MyButton EmptyBtn;
    private JButton UploadNewImage;
    private JButton ShuffleAgain;
    private JButton Restore;
    private JButton Solve;
    private int width, height; //those will be for the resized image 
    private JFrame hint;

    //ambiguous list problem: both awt and util packages include list classes, util.list must be specified
    private List<MyButton> buttons; //list of buttons to utalize Collections class's methods: swap and shuffle
    private List<Point> correctSolution; //stores coordinates of the correct position of each button, the solution to the puzzle

    private final int NUMBER_OF_BUTTONS = 12; //puzzle divided into 12 blocks.
    private final int IMAGE_WIDTH = 300; //the desired width of the image to be scaled to fit puzzle screen 
    private String imagePath = "src/resources/42_cat.jpeg"; //default puzzle image, user can change 
    private ImageIcon hintImage = new ImageIcon("src/resources/42_cat.jpeg"); //how the image supposed to look like after solving the puzzle
    private JLabel label = new JLabel(hintImage); //hint image added to label added to another frame

    class Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == UploadNewImage) {
                //allow user to upload an image from OS directory
                JFileChooser jfc = new JFileChooser();
                //add a filter user can select: show only images
                jfc.addChoosableFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg"));
                int check = jfc.showOpenDialog(jfc);
                //if user clicks ok, retrieve selected file's path, store it in String variable imagePath
                if (check == JFileChooser.APPROVE_OPTION) {
                    File f = jfc.getSelectedFile();
                    imagePath = f.getPath();
                    //update hint widnow to show the new selected image.
                    ImageIcon hintImage2 = new ImageIcon(imagePath);
                    //scale selected image to fit label's dimentions. Casting is used to convert Image type to ImageIcon type. 
                    label.setIcon(new ImageIcon(hintImage2.getImage().getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_DEFAULT)));
                    //invoke updateButtons method which will discard the set of old buttons and add a new set
                    ClickAction act = new ClickAction();
                    act.updateButtons();
                    //invoke initiate puzzle method to draw new image on the new set of buttons, and shuffle 
                    initPuzzle();

                }

            } else if (e.getSource() == ShuffleAgain) {
                initPuzzle();
            } else if (e.getSource() == Restore) {
                //restores the default image back
                imagePath = "src/resources/42_cat.jpeg";
                ImageIcon hintImage = new ImageIcon("src/resources/42_cat.jpeg");
                label.setIcon(new ImageIcon(hintImage.getImage().getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_DEFAULT)));
                initPuzzle();
            } else if (e.getSource() == Solve) {

            }
        }
    }

    //constructor
    public SlidingPuzzle() {
        //create control buttons and bind to listeners 
        UploadNewImage = new JButton("Upload New Image");
        UploadNewImage.addActionListener(new Listener());
        ShuffleAgain = new JButton("Shuffle");
        ShuffleAgain.addActionListener(new Listener());
        Restore = new JButton("Restore");
        Restore.addActionListener(new Listener());
        Solve = new JButton("Solve");
        Solve.addActionListener(new Listener());

        //create 2 panels, one for adding the puzzle buttons, one for control buttons. set  grid layout with 4 rows 3 columns to maintain uniformity
        panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.gray));
        panel.setLayout(new GridLayout(4, 3, 0, 0));
        JPanel panelbtn = new JPanel();
        panelbtn.add(UploadNewImage);
        panelbtn.add(ShuffleAgain);
        panelbtn.add(Restore);
        panelbtn.add(Solve);
        add(panelbtn, BorderLayout.SOUTH);
        initPuzzle();

        //create a second frame: hint frame
        hint = new JFrame();
        hint.add(label);
        hint.pack(); //sizes frame to all its components fit
        hint.setTitle("Hint");
        hint.setVisible(true);

    }

    //initializing the puzzle
    public final void initPuzzle() {
        //create an array of points that will define each button's position by rows and columns
        Point[] points = new Point[12];
        //triple for loop, first to fill the array, second and third to set j, k values. where j=rows (loops 4 times = 4 rows),  k=columns(loops 3 times = 3 cols)
        /*
        the below code is a shortcut for the following:
        new Point(0, 0)
        new Point(0, 1)
        new Point(0, 2)
        new Point(1, 0)
        new Point(1, 1)
        new Point(1, 2)
        new Point(2, 0)
        new Point(2, 1)
        new Point(2, 2)
        new Point(3, 0)
        new Point(3, 1)
        new Point(3, 2)
         */
        correctSolution = new ArrayList<>();
        for (int i = 0; i < points.length; i++) {
            for (int j = 1; j <= 3; j++) {
                for (int k = 1; k <= 2; k++) {
                    points[i] = new Point(j, k);
                    correctSolution.add(points[i]);
                }
            }

        }

        //create ArrayList storing the correct placements of all buttons, i.e stores the above Point values.
        //loop over Points array and add each point to the ArrayList 
        //create another ArrayList, which will be the playable set of buttons user will try to solve. 
        buttons = new ArrayList<>();

        //loadImage() method throws an exception coz it uses ImageIO method, hence must be wrapped in a try catch block.  
        try {
            original = loadImage();
            int h = getNewHeight(original.getWidth(), original.getHeight());
            resized = resizeImage(original, IMAGE_WIDTH, h,
                    BufferedImage.TYPE_INT_ARGB);

        } catch (Exception ex) {
            System.out.println("Operation Failed.");
        }

        //initialize height and width with resized image's. 
        width = resized.getWidth();
        height = resized.getHeight();

        //add panel which contains the buttons to the frame, centered. 
        add(panel, BorderLayout.CENTER);

        //loop over each row and culomn of buttons, use CropImageFilter and FilteredImageSource classes to crop the resized image into rectangles
        //the formulas used mold the cropped image into the rows and culomns of buttons
        for (int i = 0; i < 4; i++) {

            for (int j = 0; j < 3; j++) {

                image = createImage(new FilteredImageSource(resized.getSource(),
                        new CropImageFilter(j * width / 3, i * height / 4,
                                (width / 3), height / 4)));

                //instantiate MyButton class using the parametarized constructor (image)
                MyButton button = new MyButton(image); //cropped images are now added on the buttons

                //buttons are identified by their position (points) client property (exact row and col), this is to check if we have the correct order of buttons on screen
                button.putClientProperty("position", new Point(i, j));

                //on the position: 3rd row, 4th col make a space for the empty button. 
                if (i == 3 && j == 2) {
                    EmptyBtn = new MyButton();
                    EmptyBtn.setBorderPainted(false);
                    EmptyBtn.setContentAreaFilled(false);
                    EmptyBtn.setEmptyBtn();
                    EmptyBtn.putClientProperty("position", new Point(i, j)); //make sure it's in its right position
                } else {
                    buttons.add(button); //populate screen with image buttons everywhere else. 
                }
            }
        }
        ClickAction act = new ClickAction(); //define the click action to be triggered when clicking each button.
        act.updateButtons();
        Collections.shuffle(buttons); //random shuffle buttons
        buttons.add(EmptyBtn); //add the empty button to the button ArrayList 

        //loop over buttons,
        for (int i = 0; i < NUMBER_OF_BUTTONS; i++) {

            MyButton btn = buttons.get(i); //add each button to btn, a grouped button variable
            panel.add(btn); //add the group of buttons to the pannel and style 
            btn.setBorder(BorderFactory.createLineBorder(Color.gray));
            btn.addActionListener(new ClickAction());
        }

    }

    //this method is to define the new  desired height for out image. 
    private int getNewHeight(int w, int h) {

        double ratio = IMAGE_WIDTH / (double) w;
        int newHeight = (int) (h * ratio);
        return newHeight;
    }

    //this method is to read input: a stream that contains an image path, this path is default and defined above but changes if user select their own image.
    //it throws an IO Exception.
    private BufferedImage loadImage() throws IOException {

        FileInputStream fis = new FileInputStream(new File(imagePath));
        BufferedImage defaultImage = ImageIO.read(fis);

        return defaultImage;

    }

    //this method is to resize the image to fit into puzzle screen, to have the desired height and width to be cropped later to fit buttons. 
    private BufferedImage resizeImage(BufferedImage originalImage, int width,
            int height, int type) {

        BufferedImage resizedImage = new BufferedImage(width, height, type);
        //get original image dimentions and draw image with the new desired dimentions onto a new buffered image object. 
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
    }

    //Actions to be triggered when buttons are clicked. 
    //Goal: only the two adjacent buttons to the empty button can be clicked to swap places with it
    //any other button is unclicknle unless its adjacent to the empty button.
    //How to implement: get the empty button and the clicked buttons' indexes and swap them. 
    private class ClickAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            checkButton(e);
            try {
                //with each click the program will keep on checking user's solution by comparing it to the correct one.
                //whenver user hits the jackpot, a message will be displayed.
                //statement is surrounded by catch block error because the method throws an IO excpetion
                checkSolution();
            } catch (Exception ex) {
                System.out.print("Operation Failed");
            }
        }

        //loop over the buttons, for each button in buttons, check if it's the empty one, if yes, store its index in variable .
        private void checkButton(ActionEvent e) {

            int emtybtnIdx = 0;

            for (MyButton button : buttons) {
                if (button.isEmptyBtn()) {
                    emtybtnIdx = buttons.indexOf(button);
                }
            }

            JButton button = (JButton) e.getSource();//get source of the clicked button, store it in an object
            int clkdbtnIdx = buttons.indexOf(button); //get the index of said object 

            if ((clkdbtnIdx - 1 == emtybtnIdx) || (clkdbtnIdx + 1 == emtybtnIdx) //if the indexes are adjacent horizontally ot vertically. swap
                    || (clkdbtnIdx - 3 == emtybtnIdx) || (clkdbtnIdx + 3 == emtybtnIdx)) {
                Collections.swap(buttons, clkdbtnIdx, emtybtnIdx);
                updateButtons();
            }
        }

        //maps list of buttons to panel
        // (1) removed all components
        // (2) loop through the buttons list, add the reordered buttons back to the panel..
        // (3) validate() method implements the new layout.
        public void updateButtons() {

            panel.removeAll();

            for (JComponent btn : buttons) {

                panel.add(btn);
            }

            panel.validate();
        }
    }

    //check user's solution and compare it to the correct set of buttons.
    //compare the 2 lists: list of points of the correctly ordered buttons (correctSolution) with userSolution list containing the one the user ordered. 
    //compareList method converts the lists into string and compares using contentEquals()
    // display a message dialog whenever they're equal.
    private void checkSolution() throws Exception {

        List<Point> userSolution = new ArrayList<>();

        for (JComponent btn : buttons) {
            userSolution.add((Point) btn.getClientProperty("position"));
        }

        if (compareList(correctSolution, userSolution)) {
            JOptionPane.showMessageDialog(panel, "Finished",
                    "You Did It!", JOptionPane.INFORMATION_MESSAGE);
            String soundEffect = "src/resources/Ta Da-SoundBible.com-1884170640.wav";
            InputStream in = new FileInputStream(soundEffect);
            AudioStream audioStream = new AudioStream(in);

            // play the audio clip with the audioplayer class
            AudioPlayer.player.start(audioStream);
        }
    }

    public static boolean compareList(List ls1, List ls2) {

        return ls1.toString().contentEquals(ls2.toString());
    }

    //main method. 
    public static void main(String[] args) {
        SlidingPuzzle puzzle = new SlidingPuzzle();
        puzzle.pack();
        puzzle.setTitle("Puzzle");
        puzzle.setResizable(false);
        puzzle.setLocationRelativeTo(null);
        puzzle.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        puzzle.setVisible(true);

    }
}
