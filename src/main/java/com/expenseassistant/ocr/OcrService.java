package com.expenseassistant.ocr;

import com.expenseassistant.exception.ReceiptProcessingException;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class OcrService {

    private final String dataPath;
    private final String language;

    public OcrService(@Value("${tesseract.datapath}") String dataPath,
                      @Value("${tesseract.language:eng}") String language) {
        this.dataPath = dataPath;
        this.language = language;
    }

    public String extractText(BufferedImage image) {
        Path tessdata = Path.of(dataPath);
        if (!Files.isDirectory(tessdata)) {
            throw new ReceiptProcessingException("Tesseract data folder not found: " + dataPath
                    + ". Set the TESSERACT_DATA_PATH environment variable to your tessdata folder.");
        }
        if (!Files.exists(tessdata.resolve(language + ".traineddata"))) {
            throw new ReceiptProcessingException("Language file '" + language + ".traineddata' is missing in "
                    + dataPath + ". Download it into the tessdata folder.");
        }

        try {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(dataPath);
            tesseract.setLanguage(language);
            tesseract.setPageSegMode(6);
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            throw new ReceiptProcessingException("OCR failed: " + e.getMessage());
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            throw new ReceiptProcessingException("The Tesseract native library could not be loaded. "
                    + "Use a 64-bit JDK and install the Microsoft Visual C++ Redistributable (x64).");
        }
    }
}
