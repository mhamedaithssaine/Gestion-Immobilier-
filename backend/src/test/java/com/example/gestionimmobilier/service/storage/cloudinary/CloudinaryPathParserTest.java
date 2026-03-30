package com.example.gestionimmobilier.service.storage.cloudinary;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CloudinaryPathParserTest {

    @Test
    void parse_imageUpload_mandatPath_withPdfExtension() {
        String url = "https://res.cloudinary.com/dh8tta9bj/image/upload/v1774639898/mandats/bbd26906-4c05-4804-8213-f0fb570c6138/x89boxrjuuh3k0zsfcsa.pdf";
        Optional<ParsedDeliveryUrl> p = CloudinaryPathParser.parse(url);
        assertThat(p).isPresent();
        assertThat(p.get().resourceTypeFromUrl()).isEqualTo("image");
        assertThat(p.get().publicId()).isEqualTo("mandats/bbd26906-4c05-4804-8213-f0fb570c6138/x89boxrjuuh3k0zsfcsa");
        assertThat(p.get().format()).isEqualTo("pdf");
        assertThat(p.get().version()).isEqualTo("1774639898");
    }

    @Test
    void parse_imageUpload_mandatPath_noExtension() {
        String url = "https://res.cloudinary.com/dh8tta9bj/image/upload/v1774639898/mandats/bbd26906-4c05-4804-8213-f0fb570c6138/x89boxrjuuh3k";
        Optional<ParsedDeliveryUrl> p = CloudinaryPathParser.parse(url);
        assertThat(p).isPresent();
        assertThat(p.get().resourceTypeFromUrl()).isEqualTo("image");
        assertThat(p.get().publicId()).isEqualTo("mandats/bbd26906-4c05-4804-8213-f0fb570c6138/x89boxrjuuh3k");
        assertThat(p.get().format()).isNull();
        assertThat(p.get().version()).isEqualTo("1774639898");
    }

    @Test
    void parse_rawUpload_withPdfExtensionInPath() {
        String url = "https://res.cloudinary.com/demo/raw/upload/v123/folder/doc.pdf";
        Optional<ParsedDeliveryUrl> p = CloudinaryPathParser.parse(url);
        assertThat(p).isPresent();
        assertThat(p.get().resourceTypeFromUrl()).isEqualTo("raw");
        assertThat(p.get().publicId()).isEqualTo("folder/doc.pdf");
        assertThat(p.get().format()).isNull();
    }

    @Test
    void parse_signedSegment_skipped() {
        String url = "https://res.cloudinary.com/demo/image/upload/s--abc--/v99/sample/file.png";
        Optional<ParsedDeliveryUrl> p = CloudinaryPathParser.parse(url);
        assertThat(p).isPresent();
        assertThat(p.get().version()).isEqualTo("99");
        assertThat(p.get().publicId()).isEqualTo("sample/file");
        assertThat(p.get().format()).isEqualTo("png");
    }
}
