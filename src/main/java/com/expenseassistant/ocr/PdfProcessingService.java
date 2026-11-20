package com.expenseassistant.ocr;

import com.expenseassistant.exception.ReceiptProcessingException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

@Service
public class PdfProcessingService {

    private static final int MIN_TEXT_LENGTH = 30;
    private static final float RENDER_DPI = 200f;

    private final OcrService ocrService;
    private final ImagePreprocessingService preprocessingService;
    private final int maxPages;

    public PdfProcessingService(OcrService ocrService,
                                ImagePreprocessingService preprocessingService,
                                @Value("${ocr.pdf.max-pages:3}") int maxPages) {
        this.ocrService = ocrService;
        this.preprocessingService = preprocessingService;
        this.maxPages = maxPages;
    }

    /** Uses the embedded text of digital PDFs; falls back to rendering pages and running OCR for scanned PDFs. */
    public String extractText(Path pdfPath) {
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            int pages = Math.min(document.getNumberOfPages(), maxPages);
            if (pages == 0) {
                throw new ReceiptProcessingException("The PDF has no pages");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(pages);
            String embeddedText = stripper.getText(document);
            if (embeddedText != null && embeddedText.strip().length() >= MIN_TEXT_LENGTH) {
                return embeddedText;
            }

            PDFRenderer renderer = new PDFRenderer(document);
            StringBuilder text = new StringBuilder();
            for (int page = 0; page < pages; page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, RENDER_DPI, ImageType.RGB);
                text.append(ocrService.extractText(preprocessingService.preprocess(image))).append('\n');
            }
            return text.toString();
        } catch (IOException e) {
            throw new ReceiptProcessingException("Could not read the PDF file. It may be corrupted or password protected.");
        }
    }
}
