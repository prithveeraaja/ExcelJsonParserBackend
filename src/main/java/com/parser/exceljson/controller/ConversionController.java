package com.parser.exceljson.controller;

import com.parser.exceljson.model.ConversionResponse;
import com.parser.exceljson.model.JsonToExcelRequest;
import com.parser.exceljson.service.ConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ConversionController {

    private final ConversionService conversionService;

    @Autowired
    public ConversionController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @PostMapping("/excel-to-json")
    public ResponseEntity<ConversionResponse> excelToJson(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ConversionResponse(null, null, "File is empty"));
            }

            // Validate file extension
            String fileName = file.getOriginalFilename();
            if (fileName == null || !(fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
                return ResponseEntity.badRequest().body(
                        new ConversionResponse(null, null, "Invalid file format. Only .xlsx and .xls files are supported.")
                );
            }

            ConversionResponse response = conversionService.convertExcelToJson(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ConversionResponse(null, null, "Error processing file: " + e.getMessage())
            );
        }
    }

    @PostMapping("/json-to-excel")
    public ResponseEntity<Resource> jsonToExcel(@RequestBody JsonToExcelRequest request) {
        try {
            byte[] excelBytes = conversionService.convertJsonToExcel(request.getJson(), request.getFormat());

            ByteArrayResource resource = new ByteArrayResource(excelBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelBytes.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
