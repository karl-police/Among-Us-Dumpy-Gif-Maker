import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.filechooser.FileFilter;
import java.awt.Color;

public class sus extends Application {

    // Put in the directory where you extracted this
    public static String dir = "[DIRECTORY]";
    public static String wdir = "fs/";

    // Enter the input file name here
    // public String input = dotSlash + "input.png";

    // Choose if you want to dither here. It'll most likely screw it up but if you
    // have a non-flag/sprite image, this'll probably improve things.
    public static boolean dither = false;

    // Hex color array
    public static String[] HEXES;
    
    // MAIN
    public static void main(String[] args) throws Exception {
    	Application.launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
    	// Pass args to here
    	String[] args = Arrays.copyOf(getParameters().getRaw().toArray(), getParameters().getRaw().toArray().length, String[].class);

        String dotSlash = "./";
        boolean windows = isWindows();
        if (windows) {
            dotSlash = ".\\";
        }

        String input = "";
        String extraoutput = "";
        boolean needFile = true;

        int ty = 9; // width value

        if (args.length > 0) {
            if (args[0] != null) {
                try {
                    ty = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.err.println("Not a number!");
                }
            }
            if (args.length >= 2 && args[1] != null) {
                input = args[1];
                needFile = false;
            }
            if (args.length == 3 && args[2] != null) {
                extraoutput = args[2];
                needFile = false;
            }
        }

        if (needFile) {
            input = pickFile();
        }

        // Sets up color palette
        SetupColors();

        // Gets BG and input file
        BufferedImage bg = ImageIO.read(new File(dotSlash + "black.png"));
        BufferedImage r = ImageIO.read(new File(input));

        // Calculates size from height
        double txd = (double) r.getWidth() / (double) r.getHeight();
        int tx = (int) Math.round((double) ty * txd * 0.862);

        // Prepares source image
        BufferedImage image = Dither
                .floydSteinbergDithering(toBufferedImage(r.getScaledInstance(tx, ty, Image.SCALE_SMOOTH)), false);

        // Actually makes the frames
        BufferedImage[] frames = new BufferedImage[6];

        // Sets up BG
        int pad = 10;
        int ix = (tx * 74) + (pad * 2);
        int iy = (ty * 63) + (pad * 2);

        // Plots crewmates
        for (int index = 0; index < frames.length; index++) {
            // bg
            frames[index] = toBufferedImage(bg.getScaledInstance(ix, iy, Image.SCALE_SMOOTH));

            // counts. One for iterating across frames and the other for the line reset
            int count = index;
            int count2 = index;

            // iterates through pixels
            for (int y = 0; y < ty; y++) {
                for (int x = 0; x < tx; x++) {

                    // Grabs appropriate pixel frame
                    BufferedImage pixel = ImageIO.read(new File(wdir + count + "-"
                            + Integer.toHexString(image.getRGB(x, y)).substring(2).toUpperCase() + ".png"));
                    // overlays it
                    frames[index] = overlayImages(frames[index], pixel, (x * 74) + pad, (y * 63) + pad);

                    // Handles animating
                    count++;
                    if (count == 6) {
                        count = 0;
                    }
                }
                // Handles line resets
                count2--;
                if (count2 == -1) {
                    count2 = 5;
                }
                count = count2;
            }
            // Writes finished frames
            ImageIO.write(frames[index], "PNG", new File(dotSlash + "F_" + index + extraoutput + ".png"));

            // Gives an idea of progress
            System.out.println(index);
        }
        // Sets output file name
        String output = dotSlash + "dumpy" + extraoutput + ".gif";

        // Combines frames into final GIF
        System.out.println("Converting....");
        // runCmd("convert -delay 1x20 " + dotSlash + "F_*.png -loop 0 " + output);
        boolean win = isWindows();
        if (win) {
            runCmd("magick convert *\\ -delay 1x20 .\\F_* -loop 0 " + output);
            runCmd("del .\\F_*.png");
        } else {
            runCmd("convert -delay 1x20 ./F_* -loop 0 " + output);
            runCmd("rm ./F_*");
        }

        // Resizes if need be
        BufferedImage resize = ImageIO.read(new File(output));
        if (resize.getHeight() > 1000 || resize.getWidth() > 1000) {
            runCmd("convert " + output + " -resize 1000x1000 " + output);
        }
        System.out.println("Done.");
    }

    
    // Picks file
    public static String pickFile() throws Exception {
    	FileChooser fileChooser = new FileChooser();
    	//fileChooser.setTitle("Title");
    	
    	// Set file extension filters
    	fileChooser.getExtensionFilters().addAll(
    		new FileChooser.ExtensionFilter("Image file", "*.jpeg", "*.jpg", "*.bmp", "*.tiff", "*.tif", "*.png")
    	);
    	
    	File selectedFile = fileChooser.showOpenDialog(null);

        //jfc.addChoosableFileFilter(new ImageFilter());
        //jfc.setAcceptAllFileFilterUsed(false);

        if (selectedFile != null) {
        	String i = selectedFile.getAbsolutePath();
            System.out.println(i);
            return i;
        } else {
            System.exit(0);
            return "";
        }
    }
    

    // Sets up color palette from colors.png.
    public static void SetupColors() throws Exception {
        String dotSlash = "./";
        boolean windows = isWindows();
        if (windows) {
            dotSlash = ".\\";
        }
        BufferedImage c = ImageIO.read(new File(dotSlash + "colors.png"));
        HEXES = new String[24];
        for (int i = 0; i < HEXES.length; i++) {
            try {
                HEXES[i] = Integer.toHexString(c.getRGB(i, 0)).substring(2).toUpperCase();
            } catch (Exception e) {
                System.out.println(i);
            }
            // System.out.println(HEXES[i]);
        }
    }

    // BufferedImage converter from https://stackoverflow.com/a/13605411
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    // BufferedImage overlayer from
    // http://blog.icodejava.com/482/java-how-to-overlay-one-image-over-another-using-graphics2d-tutorial/
    public static BufferedImage overlayImages(BufferedImage bgImage,

            BufferedImage fgImage, int locateX, int locateY) {

        /**
         * Doing some preliminary validations. Foreground image height cannot be greater
         * than background image height. Foreground image width cannot be greater than
         * background image width.
         *
         * returning a null value if such condition exists.
         */
        if (fgImage.getHeight() > bgImage.getHeight() || fgImage.getWidth() > fgImage.getWidth()) {
            JOptionPane.showMessageDialog(null, "Foreground Image Is Bigger In One or Both Dimensions"
                    + "nCannot proceed with overlay." + "nn Please use smaller Image for foreground");
            return null;
        }

        /** Create a Graphics from the background image **/
        Graphics2D g = bgImage.createGraphics();
        /** Set Antialias Rendering **/
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        /**
         * Draw background image at location (0,0) You can change the (x,y) value as
         * required
         */
        g.drawImage(bgImage, 0, 0, null);

        /**
         * Draw foreground image at location (0,0) Change (x,y) value as required.
         */
        g.drawImage(fgImage, locateX, locateY, null);

        g.dispose();
        return bgImage;
    }

    public static boolean isWindows() throws Exception {
        String OS = System.getProperty("os.name").toLowerCase();
        boolean isWindows = false;
        if (OS.startsWith("windows")) {
            isWindows = true;
        }
        return isWindows;
    }

    // convenient way to handle Windows commands.
    public static void runCmd(String cmd) throws Exception {
        boolean win = isWindows();
        if (win) {
            // execute windows command
            new ProcessBuilder("cmd", "/c", cmd).inheritIO().start().waitFor();
        } else {
            // execute *nix command
            new ProcessBuilder("sh", "-c", cmd).inheritIO().start().waitFor();
        }
    }
}

// This is an example of Floyd-Steinberg dithering lifted from
// https://gist.github.com/naikrovek/643a9799171d20820cb9.
// It can be enabled and disabled in the main class.
class Dither {
    static class C3 {
        int r, g, b;

        public C3(int c) {
            Color color = new Color(c);
            r = color.getRed();
            g = color.getGreen();
            b = color.getBlue();
        }

        public C3(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public C3 add(C3 o) {
            return new C3(r + o.r, g + o.g, b + o.b);
        }

        public int clamp(int c) {
            return Math.max(0, Math.min(255, c));
        }

        public int diff(C3 o) {
            int Rdiff = o.r - r;
            int Gdiff = o.g - g;
            int Bdiff = o.b - b;
            int distanceSquared = Rdiff * Rdiff + Gdiff * Gdiff + Bdiff * Bdiff;
            return distanceSquared;
        }

        public C3 mul(double d) {
            return new C3((int) (d * r), (int) (d * g), (int) (d * b));
        }

        public C3 sub(C3 o) {
            return new C3(r - o.r, g - o.g, b - o.b);
        }

        public Color toColor() {
            return new Color(clamp(r), clamp(g), clamp(b));
        }

        public int toRGB() {
            return toColor().getRGB();
        }
    }

    private static C3 findClosestPaletteColor(C3 c, C3[] palette) {
        C3 closest = palette[0];

        for (C3 n : palette) {
            if (n.diff(c) < closest.diff(c)) {
                closest = n;
            }
        }

        return closest;
    }

    public static BufferedImage floydSteinbergDithering(BufferedImage img, boolean dither) {

        C3[] palette = null;
        palette = new C3[sus.HEXES.length];
        for (int i = 0; i < palette.length; i++) {
            Color c = hex2Rgb(sus.HEXES[i]);
            palette[i] = new C3(c.getRed(), c.getGreen(), c.getBlue());
        }

        int w = img.getWidth();
        int h = img.getHeight();

        C3[][] d = new C3[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                d[y][x] = new C3(img.getRGB(x, y));
            }
        }

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {

                C3 oldColor = d[y][x];
                C3 newColor = findClosestPaletteColor(oldColor, palette);
                img.setRGB(x, y, newColor.toColor().getRGB());
                if (dither) {
                    C3 err = oldColor.sub(newColor);

                    if (x + 1 < w) {
                        d[y][x + 1] = d[y][x + 1].add(err.mul(7. / 16));
                    }

                    if (x - 1 >= 0 && y + 1 < h) {
                        d[y + 1][x - 1] = d[y + 1][x - 1].add(err.mul(3. / 16));
                    }

                    if (y + 1 < h) {
                        d[y + 1][x] = d[y + 1][x].add(err.mul(5. / 16));
                    }

                    if (x + 1 < w && y + 1 < h) {
                        d[y + 1][x + 1] = d[y + 1][x + 1].add(err.mul(1. / 16));
                    }
                }
            }
        }

        return img;
    }

    public static Color hex2Rgb(String colorStr) {
        return new Color(Integer.valueOf(colorStr.substring(0, 2), 16), Integer.valueOf(colorStr.substring(2, 4), 16),
                Integer.valueOf(colorStr.substring(4, 6), 16));
    }
}

class ImageFilter extends FileFilter {
    public final static String JPEG = "jpeg";
    public final static String JPG = "jpg";
    public final static String BMP = "bmp";
    public final static String TIFF = "tiff";
    public final static String TIF = "tif";
    public final static String PNG = "png";

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals(TIFF) || extension.equals(TIF) || extension.equals(BMP) || extension.equals(JPEG)
                    || extension.equals(JPG) || extension.equals(PNG)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Image file";
    }

    String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
}
