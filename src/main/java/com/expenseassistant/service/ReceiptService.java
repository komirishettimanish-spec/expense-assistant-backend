package com.expenseassistant.service;

import com.expenseassistant.ai.GeminiService;
import com.expenseassistant.dto.ExpenseExtractionResponse;
import com.expenseassistant.exception.BadRequestException;
import com.expenseassistant.exception.ReceiptProcessingException;
import com.expenseassistant.ocr.ImagePreprocessingService;
import com.expenseassistant.ocr.OcrService;
import com.expenseassistant.ocr.PdfProcessingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ReceiptService {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    private final OcrService ocrService;
    private final ImagePreprocessingService preprocessingService;
    private final PdfProcessingService pdfProcessingService;
    private final GeminiService geminiService;
    private final Path uploadDir;

    public ReceiptService(OcrService ocrService,
                          ImagePreprocessingService preprocessingService,
                          PdfProcessingService pdfProcessingService,
                          GeminiService geminiService,
                          @Value("${file.upload-dir}") String uploadDir) {
        this.ocrService = ocrService;
        this.preprocessingService = preprocessingService;
        this.pdfProcessingService = pdfProcessingService;
        this.geminiService = geminiService;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    /** Upload -> OCR -> Gemini. Nothing is saved to the expenses table; the user confirms first. */
    public ExpenseExtractionResponse process(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please choose a receipt file to upload");
        }
        String extension = extensionOf(file.getOriginalFilename());
        boolean isPdf = extension.equals("pdf");
        if (!isPdf && !IMAGE_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Only JPG, JPEG, PNG and PDF files are allowed");
        }

        Path saved = store(file, extension);
        String text = isPdf ? pdfProcessingService.extractText(saved) : ocrImage(saved);
        if (text == null || text.isBlank()) {
            throw new ReceiptProcessingException("No text could be read from the receipt. Try a clearer, well-lit image.");
        }
        text = text.strip();

        return geminiService.extractExpense(text).withReceipt(saved.getFileName().toString(), text);
    }

    private String ocrImage(Path imagePath) {
        try {
            BufferedImage image = ImageIO.read(imagePath.toFile());
            if (image == null) {
                throw new ReceiptProcessingException("The file is not a readable image");
            }
            return ocrService.extractText(preprocessingService.preprocess(image));
        } catch (IOException e) {
            throw new ReceiptProcessingException("Could not read the image file");
        }
    }

    private Path store(MultipartFile file, String extension) {
        try {
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(UUID.randomUUID() + "." + extension);
            file.transferTo(target);
            return target;
        } catch (IOException e) {
            throw new ReceiptProcessingException("Could not store the uploaded file");
        }
    }

    private String extensionOf(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
