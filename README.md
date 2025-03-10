# Smart Excel<>JSON Parser

This application converts Excel files to JSON format and vice versa. It includes a user interface, supports mandatory/optional fields, and follows best coding practices.

## Features
- Excel to JSON conversion
- JSON to Excel conversion
- Schema generation for field types and mandatory fields
- Supports multi-sheet Excel files
- Includes error handling and basic validations
-  Includes unit tests

## Prerequisites
Ensure you have the following installed:
- **Java 17**
- **IntelliJ IDEA** (recommended IDE)
- **Maven**
- **Docker** (optional for containerization)

## Setup Instructions
1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd excel-json-parser
   ```

2. **Build the Project**
   ```bash
   ./mvnw clean install
   ```

3. **Run the Application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the Application**
    - Navigate to: [http://localhost:8080](http://localhost:8080)

## API Endpoints

### 1. **Excel to JSON Conversion**
- **URL:** `/api/convert/excel-to-json`
- **Method:** `POST`
- **Body:** Multipart file upload (`.xlsx` file)

### 2. **JSON to Excel Conversion**
- **URL:** `/api/convert/json-to-excel`
- **Method:** `POST`
- **Body:** JSON data with format specification

## Sample Requests
**Excel to JSON Request:**
```bash
curl -F "file=@sample.xlsx" http://localhost:8080/api/convert/excel-to-json
```

**JSON to Excel Request:**
```bash
curl -X POST -H "Content-Type: application/json" \
-d '{"Sheet1":[{"Name":"John Doe","Age":30,"Email":"john@example.com"}]}' \
http://localhost:8080/api/convert/json-to-excel
```

## Docker Instructions (Optional)
1. **Build Docker Image**
   ```bash
   docker build -t excel-json-parser .
   ```

2. **Run the Container**
   ```bash
   docker run -p 8080:8080 excel-json-parser
   ```

## Running Tests
```bash
./mvnw test
```

## Backend Folder Structure
```bash
src
├── main
│   ├── java
│   │   └── com
│   │       └── parser
│   │           ├── controller
│   │           │   └── ConversionController.java
│   │           ├── service
│   │           │   └── ConversionService.java
│   │           ├── model
│   │           │   └── ConversionResponse.java
│   │           └── ExcelJsonApplication.java
│   └── resources
│       ├── application.properties
│       └── templates
├── test
│   ├── java
│   │   └── com
│   │       └── parser
│   │           └── service
│   │               └── ConversionServiceTest.java
│   └── resources
├── .gitignore
├── README.md
├── pom.xml
└── .env
```


📝 Author

Prithviraj Shinde

Email : prithvirajshinde157@gmail.com
