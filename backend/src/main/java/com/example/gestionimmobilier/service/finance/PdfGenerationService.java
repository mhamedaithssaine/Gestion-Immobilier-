package com.example.gestionimmobilier.service.finance;

import com.example.gestionimmobilier.dto.finance.QuittancePdfData;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.InternalServerException;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

@Service
public class PdfGenerationService {

    private final TemplateEngine templateEngine;

    public PdfGenerationService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Renders the quittance Thymeleaf template to HTML, then converts to PDF.
     */
    public byte[] generateQuittancePdf(QuittancePdfData data) {
        Context context = new Context(Locale.FRANCE);
        context.setVariable("q", data);
        String html = templateEngine.process("pdf/quittance", context);

        try {
            return htmlToPdf(html);
        } catch (Exception e) {
            throw new InternalServerException(ErrorMessages.QUITTANCE_GENERATION_ECHEC);
        }
    }

    private byte[] htmlToPdf(String html) throws Exception {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        }
    }
}
