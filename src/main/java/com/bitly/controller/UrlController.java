package com.bitly.controller;

import com.bitly.dto.CreateUrlRequest;
import com.bitly.dto.ErrorResponse;
import com.bitly.dto.UrlResponse;
import com.bitly.service.QrCodeService;
import com.bitly.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for URL shortening operations.
 * Provides endpoints for creating, retrieving, and managing shortened URLs.
 */
@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@Tag(name = "URL Shortener", description = "Endpoints for creating and managing shortened URLs")
public class UrlController {

    private final UrlService urlService;
    private final QrCodeService qrCodeService;

    @PostMapping
    @Operation(summary = "Create a shortened URL",
            description = "Generates a short code for the provided URL. Optionally set an expiration date or custom alias.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Short URL created successfully",
                    content = @Content(schema = @Schema(implementation = UrlResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Custom alias already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UrlResponse> createShortUrl(
            @Valid @RequestBody CreateUrlRequest request) {
        UrlResponse response = urlService.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Get URL details and analytics",
            description = "Returns the full details and click analytics for a shortened URL")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL details retrieved"),
            @ApiResponse(responseCode = "404", description = "Short code not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UrlResponse> getUrlStats(
            @Parameter(description = "The short code to look up", example = "aB3dE7f")
            @PathVariable String shortCode) {
        return ResponseEntity.ok(urlService.getUrlStats(shortCode));
    }

    @GetMapping
    @Operation(summary = "List all shortened URLs",
            description = "Returns all URL mappings sorted by creation date (newest first)")
    public ResponseEntity<List<UrlResponse>> getAllUrls() {
        return ResponseEntity.ok(urlService.getAllUrls());
    }

    @DeleteMapping("/{shortCode}")
    @Operation(summary = "Delete a shortened URL",
            description = "Permanently removes a URL mapping by its short code")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "URL deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    public ResponseEntity<Void> deleteUrl(
            @Parameter(description = "The short code to delete")
            @PathVariable String shortCode) {
        urlService.deleteUrl(shortCode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{shortCode}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Generate QR code for shortened URL",
            description = "Returns a PNG image of the QR code that links to the shortened URL")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR code generated",
                    content = @Content(mediaType = "image/png")),
            @ApiResponse(responseCode = "404", description = "Short code not found"),
            @ApiResponse(responseCode = "500", description = "QR code generation failed")
    })
    public ResponseEntity<byte[]> generateQrCode(
            @Parameter(description = "The short code to generate a QR code for")
            @PathVariable String shortCode,
            @Parameter(description = "QR code width in pixels (default 300)")
            @RequestParam(defaultValue = "300") int width,
            @Parameter(description = "QR code height in pixels (default 300)")
            @RequestParam(defaultValue = "300") int height) throws Exception {

        // Verify the short code exists
        UrlResponse urlStats = urlService.getUrlStats(shortCode);
        byte[] qrCode = qrCodeService.generateQrCode(urlStats.getShortUrl(), width, height);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCode);
    }
}
