package com.expenseassistant.ocr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/** Simple receipt clean-up: resize, grayscale, contrast stretch, optional Otsu threshold. */
@Service
public class ImagePreprocessingService {

    private static final int MIN_WIDTH = 1200;
    private static final int MAX_WIDTH = 2500;

    private final boolean thresholdEnabled;

    public ImagePreprocessingService(@Value("${ocr.threshold:false}") boolean thresholdEnabled) {
        this.thresholdEnabled = thresholdEnabled;
    }

    public BufferedImage preprocess(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();

        double scale = 1.0;
        if (width < MIN_WIDTH) {
            scale = (double) MIN_WIDTH / width;
        } else if (width > MAX_WIDTH) {
            scale = (double) MAX_WIDTH / width;
        }
        int newWidth = Math.max(1, (int) Math.round(width * scale));
        int newHeight = Math.max(1, (int) Math.round(height * scale));

        BufferedImage gray = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, newWidth, newHeight);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(source, 0, 0, newWidth, newHeight, null);
        g.dispose();

        stretchContrast(gray);
        if (thresholdEnabled) {
            applyOtsuThreshold(gray);
        }
        return gray;
    }

    private int[] histogram(WritableRaster raster) {
        int[] histogram = new int[256];
        for (int y = 0; y < raster.getHeight(); y++) {
            for (int x = 0; x < raster.getWidth(); x++) {
                histogram[raster.getSample(x, y, 0)]++;
            }
        }
        return histogram;
    }

    private void stretchContrast(BufferedImage gray) {
        WritableRaster raster = gray.getRaster();
        int[] histogram = histogram(raster);
        long total = (long) raster.getWidth() * raster.getHeight();
        int low = percentile(histogram, total * 0.01);
        int high = percentile(histogram, total * 0.99);
        if (high - low < 20) {
            return;
        }
        for (int y = 0; y < raster.getHeight(); y++) {
            for (int x = 0; x < raster.getWidth(); x++) {
                int value = (raster.getSample(x, y, 0) - low) * 255 / (high - low);
                raster.setSample(x, y, 0, Math.min(255, Math.max(0, value)));
            }
        }
    }

    private int percentile(int[] histogram, double target) {
        long cumulative = 0;
        for (int i = 0; i < histogram.length; i++) {
            cumulative += histogram[i];
            if (cumulative >= target) {
                return i;
            }
        }
        return 255;
    }

    private void applyOtsuThreshold(BufferedImage gray) {
        WritableRaster raster = gray.getRaster();
        int[] histogram = histogram(raster);
        long total = (long) raster.getWidth() * raster.getHeight();

        double sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += (double) i * histogram[i];
        }
        double sumBackground = 0;
        long weightBackground = 0;
        double maxVariance = 0;
        int threshold = 127;
        for (int t = 0; t < 256; t++) {
            weightBackground += histogram[t];
            if (weightBackground == 0) {
                continue;
            }
            long weightForeground = total - weightBackground;
            if (weightForeground == 0) {
                break;
            }
            sumBackground += (double) t * histogram[t];
            double meanBackground = sumBackground / weightBackground;
            double meanForeground = (sum - sumBackground) / weightForeground;
            double variance = (double) weightBackground * weightForeground
                    * (meanBackground - meanForeground) * (meanBackground - meanForeground);
            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = t;
            }
        }
        for (int y = 0; y < raster.getHeight(); y++) {
            for (int x = 0; x < raster.getWidth(); x++) {
                raster.setSample(x, y, 0, raster.getSample(x, y, 0) > threshold ? 255 : 0);
            }
        }
    }
}
