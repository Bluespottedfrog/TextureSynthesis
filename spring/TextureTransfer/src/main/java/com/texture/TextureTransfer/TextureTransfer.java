package com.texture.TextureTransfer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;

public class TextureTransfer {
    BufferedImage src;
    BufferedImage target;
    BufferedImage prevResult;
    BufferedImage[][] patchArray;

    int rows, cols;
    int fullBlockSize;
    int resultBlockSize;
    int overlap;

    int totalIterations;

    double alpha; // parameter to determine the tradeoff between texture synth and target correspondence map

    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public TextureTransfer(BufferedImage src, BufferedImage target, int totalIterations) {
        this.src = src;
        this.target = target;
        this.totalIterations = totalIterations;
    }

    public BufferedImage drawPatches(int iteration) {
        fillPatch(iteration);
        BufferedImage result = new BufferedImage(resultBlockSize * cols, resultBlockSize * rows, src.getType());

        //Fill with everything
        for (int j = 0; j < rows; j++) {
            //Width - num of cols
            for (int i = 0; i < cols; i++) {
                BufferedImage tile = patchArray[j][i];
                for (int x = 0; x < resultBlockSize; x++) {
                    for (int y = 0; y < resultBlockSize; y++) {
                        result.setRGB(x + resultBlockSize * i, y + resultBlockSize * j, tile.getRGB(x, y));
                    }
                }
            }
        }

        return result;
    }

    public BufferedImage generateTexture() {
        BufferedImage result = null;
        for (int iteration = 0; iteration < totalIterations; iteration++) {
            alpha = 0.8 * ((double) iteration / (totalIterations - 1)) + 0.1;
            result = drawPatches(iteration);

            for (int j = 0; j < rows; j++) {
                for (int i = 0; i < cols; i++) {
                    if (i > 0) {
                        // horizontal overlap
                        BufferedImage currOverlap = patchArray[j][i].getSubimage(0, 0, overlap, resultBlockSize);
                        BufferedImage leftOverlap = patchArray[j][i - 1].getSubimage(resultBlockSize, 0, overlap, resultBlockSize);
                        double[] costPath = minErrBoundaryVerticalCut(currOverlap, leftOverlap);

                        for (int y = 0; y < resultBlockSize; y++) {
                            for (int x = 0; x < overlap; x++) {
                                int rgb = getOverlapColorVertical(leftOverlap, currOverlap, costPath, y, x);
                                result.setRGB(x + resultBlockSize * i, y + resultBlockSize * j, rgb);
                            }
                        }
                    }

                    if (j > 0) {
                        // vertical overlap
                        BufferedImage currOverlap = patchArray[j][i].getSubimage(0, 0, resultBlockSize, overlap);
                        BufferedImage aboveOverlap = patchArray[j - 1][i].getSubimage(0, resultBlockSize, resultBlockSize, overlap);
                        double[] costPath = minErrBoundaryHorizontalCut(currOverlap, aboveOverlap);

                        for (int y = 0; y < overlap; y++) {
                            for (int x = 0; x < resultBlockSize; x++) {
                                int rgb = getOverlapColorHorizontal(aboveOverlap, currOverlap, costPath, y, x);
                                result.setRGB(x + resultBlockSize * i, y + resultBlockSize * j, rgb);
                            }
                        }
                    }
                }
            }

            prevResult = deepCopy(result);
        }

        return result;
    }

    private void fillPatch(int iteration) {
        int sampleSize = 50;

        // TODO: Try tweaking block size, maybe off image size?
        int denom = (iteration == 0) ? 1 : 3 * iteration;

        fullBlockSize = (27 * totalIterations) / denom;

        overlap = fullBlockSize / 3;
        resultBlockSize = fullBlockSize - overlap;

        rows = (int) Math.ceil((double) target.getHeight() / fullBlockSize);
        cols = (int) Math.ceil((double) target.getWidth() / fullBlockSize);

        patchArray = new BufferedImage[rows][cols];

        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                BufferedImage leftBlock = (i > 0) ? patchArray[j][i - 1] : null;
                BufferedImage aboveBlock = (j > 0) ? patchArray[j - 1][i] : null;

                patchArray[j][i] = generateBlock(leftBlock, aboveBlock, sampleSize, i, j);
            }
        }
    }

    private BufferedImage generateBlock(BufferedImage leftBlock, BufferedImage aboveBlock, int sampleSize, int x, int y) {
        int targetWidth = (x * fullBlockSize + fullBlockSize > target.getWidth()) ? target.getWidth() - x * fullBlockSize : fullBlockSize;
        int targetHeight = (y * fullBlockSize + fullBlockSize > target.getHeight()) ? target.getHeight() - y * fullBlockSize : fullBlockSize;
        BufferedImage targetArea = target.getSubimage(x * fullBlockSize, y * fullBlockSize, targetWidth, targetHeight);
        //double[][] targetCorrespondenceMap = createCorrespondenceMap(targetArea);
        double[][] targetCorrespondenceMap = blurMap(targetArea);

        BufferedImage bestBlock = null;
        double bestErr = Double.MAX_VALUE;

        // Fill an array of length sampleSize samples of blocks that could match the genesis
        for (int i = 0; i < sampleSize; i++) {
            Block block = new Block(src, fullBlockSize);
            BufferedImage testImage = block.getBlock();

            double err = 0.0;
            double errorLeft = 0;
            double errorAbove = 0;

            // Compare horizontal overlap color difference
            if (leftBlock != null) {
                BufferedImage leftBlockOverlap = leftBlock.getSubimage(resultBlockSize, 0, overlap, fullBlockSize);
                BufferedImage sampleBlockOverlap = testImage.getSubimage(0, 0, overlap, fullBlockSize);

                errorLeft = calculateTotalSynthError(leftBlockOverlap, sampleBlockOverlap);
                err = errorLeft;
            }

            // Compare vertical overlap color difference
            if (aboveBlock != null) {
                BufferedImage aboveBlockOverlap = aboveBlock.getSubimage(0, resultBlockSize, fullBlockSize, overlap);
                BufferedImage sampleBlockOverlap = testImage.getSubimage(0, 0, fullBlockSize, overlap);

                errorAbove = calculateTotalSynthError(aboveBlockOverlap, sampleBlockOverlap);
                err = errorAbove;
            }

            if (leftBlock != null && aboveBlock != null) {
                err = (errorLeft + errorAbove) / 2;
            }

            if (prevResult != null && x * resultBlockSize < prevResult.getWidth() && y * resultBlockSize < prevResult.getHeight()) {
                int prevWidth = (x * resultBlockSize + resultBlockSize > prevResult.getWidth()) ? prevResult.getWidth() - x * resultBlockSize : resultBlockSize;
                int prevHeight = (y * resultBlockSize + resultBlockSize > prevResult.getHeight()) ? prevResult.getHeight() - y * resultBlockSize : resultBlockSize;
                BufferedImage prevResultBlock = prevResult.getSubimage(x * resultBlockSize, y * resultBlockSize, prevWidth, prevHeight);
                err += calculateTotalSynthError(testImage, prevResultBlock);
            }

            // Calculate correspondence error with target image
            //double[][] sampleBlockCorrespondenceMap = createCorrespondenceMap(sampleBlocks[i]);
            double[][] sampleBlockCorrespondenceMap = blurMap(testImage);
            double corrErr = calculateCorrespondenceErr(sampleBlockCorrespondenceMap, targetCorrespondenceMap);

            err = alpha * err + (1 - alpha) * corrErr;

            if (err < bestErr) {
                bestErr = err;
                bestBlock = testImage;
            }
        }

        return bestBlock;
    }

    public BufferedImage boxBlur(BufferedImage src) {
        BufferedImage res = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        BufferedImage t1 = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        BufferedImage t2 = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());

        float[] matrix = {
                0.111f, 0.111f, 0.111f,
                0.111f, 0.111f, 0.111f,
                0.111f, 0.111f, 0.111f,
        };
        BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix));
        op.filter(src, t1);
        op.filter(t1, t2);
        op.filter(t1, res);

        return res;
    }

    private double[][] blurMap(BufferedImage src) {
        BufferedImage res = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        BufferedImage temp = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());

        float[] matrix = {
                0.111f, 0.111f, 0.111f,
                0.111f, 0.111f, 0.111f,
                0.111f, 0.111f, 0.111f,
        };

        BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix));
        op.filter(src, temp);
        op.filter(temp, res);

        //res = gaussianBlur(src);
        //res = gaussianBlur(res);

        double[][] result = new double[src.getHeight()][src.getWidth()];

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                Color c = new Color(res.getRGB(x, y));
                result[y][x] = calculateLuminance(c.getRed(), c.getGreen(), c.getBlue());
            }
        }

        return result;
    }

    public BufferedImage gaussianBlur(BufferedImage src) {
        BufferedImage res = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());

        double[] blur = {0.00598, 0.060626, 0.241843, 0.383103, 0.241843, 0.060626, 0.00598};

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 3; x < src.getWidth() - 3; x++) {
                float r = 0, g = 0, b = 0;
                for (int i = 0; i < 7; i++) {
                    int pixel = src.getRGB(x + i - 3, y);
                    b += (pixel & 0xFF) * blur[i];
                    g += ((pixel >> 8) & 0xFF) * blur[i];
                    r += ((pixel >> 16) & 0xFF) * blur[i];
                }
                int p = (int) b + ((int) g << 8) + ((int) r << 16);
                // transpose result!
                res.setRGB(x, y, p);
            }
        }

        for (int x = 0; x < src.getWidth(); x++) {
            for (int y = 3; y < src.getHeight() - 3; y++) {
                float r = 0, g = 0, b = 0;
                for (int i = 0; i < 7; i++) {
                    int pixel = src.getRGB(x, y + i - 3);
                    b += (pixel & 0xFF) * blur[i];
                    g += ((pixel >> 8) & 0xFF) * blur[i];
                    r += ((pixel >> 16) & 0xFF) * blur[i];
                }
                int p = (int) b + ((int) g << 8) + ((int) r << 16);
                // transpose result!
                res.setRGB(x, y, p);
            }
        }


        return res;
    }

    private double calculateLuminance(int r, int g, int b) {
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private double[][] createCorrespondenceMap(BufferedImage src) {
        double[][] result = new double[src.getHeight()][src.getWidth()];

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                Color c = new Color(src.getRGB(x, y));
                result[y][x] = calculateLuminance(c.getRed(), c.getGreen(), c.getBlue());
            }
        }

        return result;
    }

    private double calculateCorrespondenceErr(double[][] m1, double[][] m2) {
        double totalErr = 0;

        int height = Math.min(m1.length, m2.length);
        int width = Math.min(m1[0].length, m2[0].length);

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                totalErr += Math.pow(m1[j][i] - m2[j][i], 2);
            }
        }

        return totalErr;
    }

    private double calculateSynthError(int x, int y, int x2, int y2, BufferedImage genesis, BufferedImage sample) {
        double res;
        Color genC = new Color(genesis.getRGB(x, y));
        Color exC = new Color(sample.getRGB(x2, y2));

        int r1, r2;
        int g1, g2;
        int b1, b2;

        r1 = genC.getRed();
        r2 = exC.getRed();
        g1 = genC.getGreen();
        g2 = exC.getGreen();
        b1 = genC.getBlue();
        b2 = exC.getBlue();

        res = Math.sqrt(Math.pow(r2 - r1, 2) + Math.pow(g2 - g1, 2) + Math.pow(b2 - b1, 2));

        return res;
    }

    private double[][] getSynthErrorSurface(BufferedImage b1, BufferedImage b2) {
        int height = Math.min(b1.getHeight(), b2.getHeight());
        int width = Math.min(b1.getWidth(), b2.getWidth());

        double[][] errorSurface = new double[height][width];

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                errorSurface[j][i] = calculateSynthError(i, j, i, j, b1, b2);
            }
        }

        return errorSurface;
    }

    private double calculateTotalSynthError(BufferedImage b1, BufferedImage b2) {
        double[][] errorSurface = getSynthErrorSurface(b1, b2);
        double sum = 0;

        for (int j = 0; j < errorSurface.length; j++) {
            for (int i = 0; i < errorSurface[0].length; i++) {
                sum += errorSurface[j][i];
            }
        }

        return sum;
    }

    private double[] minErrBoundaryVerticalCut(BufferedImage b1, BufferedImage b2) {
        int height = Math.min(b1.getHeight(), b2.getHeight());
        int width = Math.min(b1.getWidth(), b2.getWidth());

        double[][] errorSurface = getSynthErrorSurface(b1, b2);

        // calculate cumulative error
        for (int j = 1; j < height; j++) {
            for (int i = 0; i < width; i++) {
                double left = (i > 0) ? errorSurface[j - 1][i - 1] : Double.MAX_VALUE;
                double above = errorSurface[j - 1][i];
                double right = (i < width - 1) ? errorSurface[j - 1][i + 1] : Double.MAX_VALUE;

                errorSurface[j][i] = errorSurface[j][i] + Math.min(Math.min(left, above), right);
            }
        }

        // smallest error in the last row will be the cut
        int minIndex = 0;
        for (int i = 1; i < width; i++) {
            if (errorSurface[height - 1][i] < errorSurface[height - 1][minIndex]) {
                minIndex = i;
            }
        }

        double[] path = new double[height];
        path[height - 1] = minIndex;

        for (int j = height - 2; j >= 0; j--) {
            int left = minIndex - 1;
            int right = minIndex + 1;

            if (left >= 0) {
                if (errorSurface[j][left] < errorSurface[j][minIndex]) {
                    minIndex = left;
                }
            }

            if (right < width) {
                if (errorSurface[j][right] < errorSurface[j][minIndex]) {
                    minIndex = right;
                }
            }

            path[j] = minIndex;
        }

        return path;
    }

    private double[] minErrBoundaryHorizontalCut(BufferedImage b1, BufferedImage b2) {
        int height = Math.min(b1.getHeight(), b2.getHeight());
        int width = Math.min(b1.getWidth(), b2.getWidth());

        double[][] errorSurface = getSynthErrorSurface(b1, b2);

        // calculate cumulative error
        for (int i = 1; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double up = (j > 0) ? errorSurface[j - 1][i - 1] : Double.MAX_VALUE;
                double right = errorSurface[j][i - 1];
                double down = (j < height - 1) ? errorSurface[j + 1][i - 1] : Double.MAX_VALUE;

                errorSurface[j][i] = errorSurface[j][i] + Math.min(Math.min(up, right), down);
            }
        }

        // smallest error in the last col will be the cut
        int minIndex = 0;
        for (int j = 1; j < height; j++) {
            if (errorSurface[j][width - 1] < errorSurface[minIndex][width - 1]) {
                minIndex = j;
            }
        }

        double[] path = new double[width];
        path[width - 1] = minIndex;

        for (int i = width - 2; i >= 0; i--) {
            int up = minIndex - 1;
            int down = minIndex + 1;

            if (up >= 0) {
                if (errorSurface[up][i] < errorSurface[minIndex][i]) {
                    minIndex = up;
                }
            }

            if (down < height) {
                if (errorSurface[down][i] < errorSurface[minIndex][i]) {
                    minIndex = down;
                }
            }

            path[i] = minIndex;
        }

        return path;
    }

    private int getOverlapColorVertical(BufferedImage b1, BufferedImage b2, double[] costPath, int y, int x) {
        if (x > costPath[y]) {
            return b2.getRGB(x, y);
        }
        return b1.getRGB(x, y);
    }

    private int getOverlapColorHorizontal(BufferedImage b1, BufferedImage b2, double[] costPath, int y, int x) {
        if (y > costPath[x]) {
            return b2.getRGB(x, y);
        }
        return b1.getRGB(x, y);
    }
}
