package com.venim;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static alg.Matrix.inverse;

public class Main {
    static File JPG_LOCATION = new File("C:\\Users\\admin\\Desktop\\Product Images");

    public static void main(String[] args) throws IOException {
        for (File input : getFiles(JPG_LOCATION, ".jpg", true)) {

            String parent = input.getParentFile().getParentFile().getAbsolutePath();
            String newParent = input.getParentFile().getParentFile().getParentFile().getAbsolutePath() + "\\smallImageTile";

            File output = new File(input.getAbsolutePath().replace(parent, newParent));
            copy(input, output.toPath());
        }

        for (File input : getFiles(JPG_LOCATION, ".jpg", false)) {
            String parent = input.getParentFile().getParentFile().getAbsolutePath();
            String newParent = input.getParentFile().getParentFile().getParentFile().getAbsolutePath()
                    + "\\cooked";

            File output = new File(input.getAbsolutePath().replace(parent, newParent).replace(".jpg","_mini.jpg"));
            output.getParentFile().mkdirs(); // Create missing parent directories
            output.createNewFile(); // Create missing file

            BufferedImage img = ImageIO.read(input);
            WritableRaster src = img.getRaster();
            double oldWidth = img.getWidth();
            double oldHeight = img.getHeight();
            double newWidth = 150;
            double newHeight = 150;
            img = new BufferedImage((int) newWidth, (int) newHeight, img.getType());
            WritableRaster tgt = img.getRaster();

            if (img.getType() != BufferedImage.TYPE_3BYTE_BGR) {
                throw new UnsupportedOperationException("Unknown Type: " + img.getType());
            }

            double scaleX = newWidth / oldWidth; // scaling down will create black areas
            double scaleY = newHeight / oldHeight;
            double[][] scale2D = {{scaleX, 0, 0}, {0, scaleY, 0}, {0, 0, 1}};

            /*
             * Joining all the transformations
             */
            double[][] transformation = inverse(scale2D);

            long start = System.currentTimeMillis();
            for (int tx = 0; tx < tgt.getWidth(); tx++) {
                for (int ty = 0; ty < tgt.getHeight(); ty++) {
                    double[][] tp = {{tx}, {ty}, {1}};

                    double[][] sp = {{0}, {0}, {0}};

                    // Matrix multiplication
                    for (int m = 0; m < transformation.length; m++)
                        for (int n = 0; n < tp.length; n++)
                            for (int k = 0; k < tp[0].length; k++)
                                sp[m][k] += transformation[m][n] * tp[n][k];

                    double sw = sp[2][0];
                    double sx = sp[0][0] / sw;
                    double sy = sp[1][0] / sw;

                    try {
                        double[] tgtPixel;
                        // Bilinear Interpolation
                        if (sx % 1 != 0 || sy % 1 != 0) {
                            double fx = sx % 1.0;
                            double fy = sy % 1.0;

                            int lowX = (int) ((fx != 0) ? (sx < 0 ? (sx - fx - 1) : (sx - fx)) : sx); // floor
                            int highX = (int) ((fx != 0) ? (sx < 0 ? (sx - fx) : (sx - fx + 1)) : sx); // ceil
                            int lowY = (int) ((fy != 0) ? (sy < 0 ? (sy - fy - 1) : (sy - fy)) : sy); // floor
                            int highY = (int) ((fy != 0) ? (sy < 0 ? (sy - fy) : (sy - fy + 1)) : sy); // ceil

                            double[] bilinear = new double[3];

                            for (int x = lowX; x <= highX; x++) {
                                for (int y = lowY; y <= highY; y++) {
                                    double dx = (sx > x) ? (1.0 - fx) : (fx == 0 ? 1 : fx);
                                    double dy = (sy > y) ? (1.0 - fy) : (fy == 0 ? 1 : fy);
                                    double[] pixel = src.getPixel(x, y, (double[]) null);
                                    bilinear[0] += pixel[0] * dx * dy;
                                    bilinear[1] += pixel[1] * dx * dy;
                                    bilinear[2] += pixel[2] * dx * dy;
                                }
                            }
                            bilinear[0] = bilinear[0] < 0 ? 0 : bilinear[0] > 255 ? 255 : bilinear[0];
                            bilinear[1] = bilinear[1] < 0 ? 0 : bilinear[1] > 255 ? 255 : bilinear[1];
                            bilinear[2] = bilinear[2] < 0 ? 0 : bilinear[2] > 255 ? 255 : bilinear[2];

                            tgtPixel = bilinear;
                        } else {
                            tgtPixel = src.getPixel((int) sx, (int) sy, (double[]) null);
                        }

                        clip(tgtPixel);

                        tgt.setPixel(tx, ty, tgtPixel);
                    } catch (IndexOutOfBoundsException e) {
                        tgt.setPixel(tx, ty, new double[3]);
                    }
                }
            }

            ImageIO.write(img, "jpg", output);
        }
    }

    /*
     * Copies files
     */
    public static void copy(File source, Path target) {
        try {
            target.toFile().getParentFile().mkdirs(); // Create missing parent directories
            target.toFile().createNewFile(); // Create the missing file
            Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Picks files of a specific extension
     */
    public static List<File> getFiles(File container, String extension, boolean negate) {
        List<File> result = new ArrayList<>();
        if (container.isFile()) {
            if (!negate == container.getAbsolutePath().endsWith(extension)) // Check extension
                result.add(container);
            return result;
        }

        File[] listOfFiles = container.listFiles();
        for (File file : listOfFiles) {
            result.addAll(getFiles(file, extension, negate));
        }
        return result;
    }

    public static void clip(double[] color) {
        color[0] = color[0] > 255 ? 255 : (color[0] < 0 ? 0 : color[0]);
        color[1] = color[1] > 255 ? 255 : (color[1] < 0 ? 0 : color[1]);
        color[2] = color[2] > 255 ? 255 : (color[2] < 0 ? 0 : color[2]);
    }
}
