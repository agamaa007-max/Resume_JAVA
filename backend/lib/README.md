# External JAR Dependencies

This directory contains external library JARs required for the BlindHire Applicant Tracking System.
Because BlindHire is built with pure Core Java (without Maven or Gradle), all external dependencies are managed here in `lib/`.

## Libraries Included

| Library | Version | Purpose |
|---|---|---|
| `mysql-connector-j.jar` | 8.3.0 | MySQL JDBC Driver for database persistence |
| `gson.jar` | 2.10.1 | Google Gson for JSON serialization & parsing |
| `pdfbox.jar` | 2.0.30 | Apache PDFBox for extracting text from PDF resumes |
| `fontbox.jar` | 2.0.30 | FontBox (required by PDFBox for font decoding) |
| `commons-logging.jar` | 1.2 | Logging abstraction required by PDFBox |
| `poi.jar` | 5.2.5 | Apache POI Core for Microsoft document processing |
| `poi-ooxml.jar` | 5.2.5 | Apache POI OOXML for .docx resume extraction |
| `poi-ooxml-lite.jar` | 5.2.5 | Lightweight XML schemas for DOCX models |
| `xmlbeans.jar` | 5.2.0 | XML processing engine used by Apache POI |
| `commons-compress.jar` | 1.26.0 | Compression utilities used by Apache POI |
| `commons-io.jar` | 2.15.1 | IO helper utilities used by Apache POI |
| `log4j-api.jar` | 2.22.1 | Logging interface used by Apache POI |

## How to Download or Re-download Dependencies

If any JAR is missing, you can easily download all required dependencies using the automated script:

```bat
scripts\download-libs.bat
```

Or manually via PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\download-libs.ps1
```

## Classpath Configuration

When compiling or running the project manually from the project root:

**Compilation:**
```bat
javac -d backend/bin -cp "backend/lib/*" backend/src/com/blindhire/**/*.java
```

**Running:**
```bat
java -cp "backend/bin;backend/lib/*" com.blindhire.Main
```
