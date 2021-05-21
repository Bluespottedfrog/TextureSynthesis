import java.awt.Color;
import java.awt.image.BufferedImage;

public class TextureSynthesis {

    int rows, cols;
    BufferedImage originalImage;
    BufferedImage[][] patchArray;

    int fullBlockSize;
    int resultBlockSize;
    int overlap;

    public TextureSynthesis(BufferedImage originalImage, int blockSize) {
        this.originalImage = originalImage;
        rows = 10;
        cols = 10;
        patchArray = new BufferedImage[rows][cols];
        overlap = blockSize / 6;
        fullBlockSize = blockSize;
        resultBlockSize = blockSize - overlap;
    }

    public BufferedImage generateNoFill() {
        BufferedImage result = new BufferedImage(resultBlockSize * rows, resultBlockSize * cols, originalImage.getType());
        fillPatch();

        //Fill with everything
        for (int j = 0; j < rows; j++) {
            //Width - num of cols
            for (int i = 0; i < cols; i++) {
                BufferedImage tile = patchArray[j][i];
                for (int x = 0; x < resultBlockSize; x++) {
                    for (int y = 0; y < resultBlockSize; y++) {
                        result.setRGB(x + resultBlockSize * i, y + resultBlockSize* j, tile.getRGB(x, y));
                    }
                }
            }
        }

        return result;
    }

    public BufferedImage generateTexture() {
        BufferedImage result = generateNoFill();

        //Fill horizontal overlap
        //Height - num of rows
        for (int j = 0; j < rows; j++) {
            //Width - num of cols
            for (int i = 1; i < cols; i++) {
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
        }

        //Fill vertical overlap
        //Height - num of rows
        for (int j = 1; j < rows; j++) {
            //Width - num of cols
            for (int i = 0; i < cols; i++) {
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

        return result;
    }

    private void fillPatch() {
        int sampleSize = 100;

        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                BufferedImage leftBlock = (i > 0) ? patchArray[j][i - 1] : null;
                BufferedImage aboveBlock = (j > 0) ? patchArray[j - 1][i] : null;

                patchArray[j][i] = matchBlock(leftBlock, aboveBlock, sampleSize);
            }
        }
    }

    private BufferedImage matchBlock(BufferedImage leftBlock, BufferedImage aboveBlock, int sampleSize) {
        BufferedImage[] sampleBlocks = new BufferedImage[sampleSize];
        double[] errorLeft = new double[sampleSize];
        double[] errorAbove = new double[sampleSize];
        double[] errorAvg = new double[sampleSize];

        // Fill an array of length sampleSize samples of blocks that could match the genesis
        for (int i = 0; i < sampleSize; i++) {
            Block block = new Block(originalImage, fullBlockSize);
            sampleBlocks[i] = block.generateBlock();

            // Compare horizontal overlap color difference
            if (leftBlock != null) {
                BufferedImage leftBlockOverlap = leftBlock.getSubimage(resultBlockSize, 0, overlap, fullBlockSize);
                BufferedImage sampleBlockOverlap = sampleBlocks[i].getSubimage(0, 0, overlap, fullBlockSize);

                errorLeft[i] = calculateTotalError(leftBlockOverlap, sampleBlockOverlap);
            }

            // Compare vertical overlap color difference
            if (aboveBlock != null) {
                BufferedImage aboveBlockOverlap = aboveBlock.getSubimage(0, resultBlockSize, fullBlockSize, overlap);
                BufferedImage sampleBlockOverlap = sampleBlocks[i].getSubimage(0, 0, fullBlockSize, overlap);

                errorAbove[i] = calculateTotalError(aboveBlockOverlap, sampleBlockOverlap);
            }
        }

        if (leftBlock != null && aboveBlock != null) {
            for (int i = 0; i < errorAvg.length; i++) {
                errorAvg[i] = (errorLeft[i] + errorAbove[i]) / 2;
            }
        }

        int index = 0;
        //Find the block that is the best match based on error
        for (int i = 0; i < sampleBlocks.length; i++) {
            if (leftBlock != null && aboveBlock == null) {
                if (errorLeft[i] < errorLeft[index]) {
                    index = i;
                }
            } else if (leftBlock == null && aboveBlock != null) {
                if (errorAbove[i] < errorAbove[index]) {
                    index = i;
                }
            } else {
                if (errorAvg[i] < errorAvg[index]) {
                    index = i;
                }
            }
        }

        return sampleBlocks[index];
    }

    private double calculateError(int x, int y, int x2, int y2, BufferedImage genesis, BufferedImage sample) {
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

    private double[][] getErrorSurface(BufferedImage b1, BufferedImage b2) {
        int height = Math.min(b1.getHeight(), b2.getHeight());
        int width = Math.min(b1.getWidth(), b2.getWidth());

        double[][] errorSurface = new double[height][width];

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                errorSurface[j][i] = calculateError(i, j, i, j, b1, b2);
            }
        }

        return errorSurface;
    }

    private double calculateTotalError(BufferedImage b1, BufferedImage b2) {
        double[][] errorSurface = getErrorSurface(b1, b2);
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

        double[][] errorSurface = getErrorSurface(b1, b2);

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

        double[][] errorSurface = getErrorSurface(b1, b2);

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
