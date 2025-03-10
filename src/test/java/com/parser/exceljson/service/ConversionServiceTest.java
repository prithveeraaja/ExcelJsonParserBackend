package com.parser.exceljson.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.parser.exceljson.model.ConversionResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConversionServiceTest {

	private ConversionService conversionService;
	private ObjectMapper objectMapper;

	@BeforeEach
	public void setUp() {
		conversionService = new ConversionService();
		objectMapper = new ObjectMapper();
	}

	@Test
	public void testExcelToJsonConversion() throws IOException {
		// Create a test Excel file
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Sheet1");

		// Create header row
		Row headerRow = sheet.createRow(0);
		headerRow.createCell(0).setCellValue("Name");
		headerRow.createCell(1).setCellValue("Age");
		headerRow.createCell(2).setCellValue("Email");

		// Create data rows
		Row dataRow1 = sheet.createRow(1);
		dataRow1.createCell(0).setCellValue("John Doe");
		dataRow1.createCell(1).setCellValue(30);
		dataRow1.createCell(2).setCellValue("john@example.com");

		Row dataRow2 = sheet.createRow(2);
		dataRow2.createCell(0).setCellValue("Jane Smith");
		dataRow2.createCell(1).setCellValue(25);
		dataRow2.createCell(2).setCellValue("jane@example.com");

		// Convert workbook to byte array
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		workbook.write(bos);
		byte[] bytes = bos.toByteArray();
		workbook.close();

		// Create mock MultipartFile
		MultipartFile file = new MockMultipartFile("test.xlsx", "test.xlsx",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);

		// Test conversion
		ConversionResponse response = conversionService.convertExcelToJson(file);

		// Assertions
		assertNotNull(response);
		assertNotNull(response.getJson());
		assertNotNull(response.getSchema());
		assertNull(response.getError());

		JsonNode json = response.getJson();
		assertTrue(json.has("Sheet1"));
		assertTrue(json.get("Sheet1").isArray());
		assertEquals(2, json.get("Sheet1").size());

		JsonNode schema = response.getSchema();
		assertTrue(schema.has("Sheet1"));
		assertTrue(schema.get("Sheet1").has("Name"));
		assertTrue(schema.get("Sheet1").has("Age"));
		assertTrue(schema.get("Sheet1").has("Email"));

		// Check field types
		assertEquals("string", schema.get("Sheet1").get("Name").get("type").asText());
		assertEquals("number", schema.get("Sheet1").get("Age").get("type").asText());
		assertEquals("string", schema.get("Sheet1").get("Email").get("type").asText());

		// Check mandatory fields
		assertTrue(schema.get("Sheet1").get("Name").get("mandatory").asBoolean());
		assertTrue(schema.get("Sheet1").get("Age").get("mandatory").asBoolean());
		assertTrue(schema.get("Sheet1").get("Email").get("mandatory").asBoolean());
	}

	@Test
	public void testJsonToExcelConversion() throws IOException {
		// Create test JSON data
		ObjectNode jsonData = objectMapper.createObjectNode();
		ObjectNode sheet1 = objectMapper.createObjectNode();

		// Create array of rows
		ObjectNode row1 = objectMapper.createObjectNode();
		row1.put("Name", "John Doe");
		row1.put("Age", 30);
		row1.put("Email", "john@example.com");

		ObjectNode row2 = objectMapper.createObjectNode();
		row2.put("Name", "Jane Smith");
		row2.put("Age", 25);
		row2.put("Email", "jane@example.com");

		jsonData.set("Sheet1", objectMapper.createArrayNode().add(row1).add(row2));

		// Create format specification
		Map<String, List<String>> format = new HashMap<>();
		format.put("Sheet1", Arrays.asList("Name", "Age", "Email"));

		// Test conversion
		byte[] excelBytes = conversionService.convertJsonToExcel(jsonData, format);

		// Assertions
		assertNotNull(excelBytes);
		assertTrue(excelBytes.length > 0);

		// Verify Excel content
		Workbook workbook = WorkbookFactory.create(new java.io.ByteArrayInputStream(excelBytes));
		assertEquals(1, workbook.getNumberOfSheets());

		Sheet sheet = workbook.getSheetAt(0);
		assertEquals("Sheet1", sheet.getSheetName());

		// Check header row
		Row headerRow = sheet.getRow(0);
		assertEquals("Name", headerRow.getCell(0).getStringCellValue());
		assertEquals("Age", headerRow.getCell(1).getStringCellValue());
		assertEquals("Email", headerRow.getCell(2).getStringCellValue());

		// Check data rows
		Row dataRow1 = sheet.getRow(1);
		assertEquals("John Doe", dataRow1.getCell(0).getStringCellValue());
		assertEquals(30, (int) dataRow1.getCell(1).getNumericCellValue());
		assertEquals("john@example.com", dataRow1.getCell(2).getStringCellValue());

		Row dataRow2 = sheet.getRow(2);
		assertEquals("Jane Smith", dataRow2.getCell(0).getStringCellValue());
		assertEquals(25, (int) dataRow2.getCell(1).getNumericCellValue());
		assertEquals("jane@example.com", dataRow2.getCell(2).getStringCellValue());

		workbook.close();
	}
}
