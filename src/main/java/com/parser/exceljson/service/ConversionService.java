package com.parser.exceljson.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.parser.exceljson.model.ConversionResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Service
public class ConversionService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConversionResponse convertExcelToJson(MultipartFile file) throws IOException {
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        ObjectNode resultJson = objectMapper.createObjectNode();
        ObjectNode schemaJson = objectMapper.createObjectNode();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();

            List<Map<String, Object>> sheetData = new ArrayList<>();
            Map<String, Set<String>> columnValues = new HashMap<>();
            List<String> headers = new ArrayList<>();

            // Process header row
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                    Cell cell = headerRow.getCell(j);
                    if (cell != null) {
                        String header = getCellValueAsString(cell);
                        headers.add(header);
                        columnValues.put(header, new HashSet<>());
                    }
                }
            }

            // Process data rows
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    Map<String, Object> rowData = new HashMap<>();

                    for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
                        Cell cell = row.getCell(colIndex);
                        String header = headers.get(colIndex);
                        Object value = null;

                        if (cell != null) {
                            value = getCellValue(cell);
                            if (value != null && value instanceof String) {
                                columnValues.get(header).add((String) value);
                            }
                        }

                        rowData.put(header, value);
                    }

                    sheetData.add(rowData);
                }
            }

            // Create schema for this sheet
            ObjectNode sheetSchema = objectMapper.createObjectNode();
            for (String header : headers) {
                ObjectNode fieldSchema = objectMapper.createObjectNode();
                Set<String> values = columnValues.get(header);

                // Determine if field is mandatory
                boolean isMandatory = true;
                for (Map<String, Object> row : sheetData) {
                    Object value = row.get(header);
                    if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                        isMandatory = false;
                        break;
                    }
                }

                // Infer field type
                String fieldType = inferFieldType(sheetData, header);

                fieldSchema.put("type", fieldType);
                fieldSchema.put("mandatory", isMandatory);

                sheetSchema.set(header, fieldSchema);
            }

            // Add sheet data and schema to result
            ArrayNode sheetDataNode = objectMapper.valueToTree(sheetData);
            resultJson.set(sheetName, sheetDataNode);
            schemaJson.set(sheetName, sheetSchema);
        }

        workbook.close();
        return new ConversionResponse(resultJson, schemaJson, null);
    }

    public byte[] convertJsonToExcel(JsonNode jsonData, Map<String, List<String>> format) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        // Process each sheet in the JSON data
        Iterator<Map.Entry<String, JsonNode>> sheetIterator = jsonData.fields();
        while (sheetIterator.hasNext()) {
            Map.Entry<String, JsonNode> sheetEntry = sheetIterator.next();
            String sheetName = sheetEntry.getKey();
            JsonNode sheetData = sheetEntry.getValue();

            if (sheetData.isArray() && sheetData.size() > 0) {
                Sheet sheet = workbook.createSheet(sheetName);

                // Get column headers
                List<String> headers;
                if (format != null && format.containsKey(sheetName)) {
                    headers = format.get(sheetName);
                } else {
                    headers = new ArrayList<>();
                    JsonNode firstRow = sheetData.get(0);
                    Iterator<String> fieldNames = firstRow.fieldNames();
                    while (fieldNames.hasNext()) {
                        headers.add(fieldNames.next());
                    }
                }

                // Create header row
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers.get(i));
                }

                // Create data rows
                for (int rowIndex = 0; rowIndex < sheetData.size(); rowIndex++) {
                    JsonNode rowData = sheetData.get(rowIndex);
                    Row row = sheet.createRow(rowIndex + 1);

                    for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
                        String header = headers.get(colIndex);
                        Cell cell = row.createCell(colIndex);

                        if (rowData.has(header)) {
                            JsonNode value = rowData.get(header);
                            setCellValue(cell, value);
                        }
                    }
                }

                // Auto-size columns
                for (int i = 0; i < headers.size(); i++) {
                    sheet.autoSizeColumn(i);
                }
            }
        }

        // Write workbook to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double value = cell.getNumericCellValue();
                    // Check if it's an integer
                    if (value == Math.floor(value)) {
                        return (long) value;
                    }
                    return value;
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private void setCellValue(Cell cell, JsonNode value) {
        if (value.isNull()) {
            cell.setBlank();
        } else if (value.isTextual()) {
            cell.setCellValue(value.asText());
        } else if (value.isNumber()) {
            cell.setCellValue(value.asDouble());
        } else if (value.isBoolean()) {
            cell.setCellValue(value.asBoolean());
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private String inferFieldType(List<Map<String, Object>> data, String fieldName) {
        List<Object> values = new ArrayList<>();
        for (Map<String, Object> row : data) {
            Object value = row.get(fieldName);
            if (value != null) {
                values.add(value);
            }
        }

        if (values.isEmpty()) {
            return "string";
        }

        // Check if all values are numbers
        boolean allNumbers = values.stream().allMatch(v -> v instanceof Number);
        if (allNumbers) {
            return "number";
        }

        // Check if all values are booleans
        boolean allBooleans = values.stream().allMatch(v -> v instanceof Boolean ||
                (v instanceof String && ("true".equalsIgnoreCase((String) v) || "false".equalsIgnoreCase((String) v))));
        if (allBooleans) {
            return "boolean";
        }

        // Check if all values are dates
        boolean allDates = values.stream().allMatch(v -> v instanceof Date);
        if (allDates) {
            return "date";
        }

        // Default to string
        return "string";
    }
}
