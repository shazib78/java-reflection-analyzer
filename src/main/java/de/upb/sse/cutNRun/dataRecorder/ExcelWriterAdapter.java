package de.upb.sse.cutNRun.dataRecorder;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ExcelWriterAdapter implements ExcelWriterPort {
    private String fileName;
    private String location;
    private boolean isOverwriteFile;
    private String[] headers;
    private Path filePath;

    public ExcelWriterAdapter(String fileName, boolean overwriteFile) {
        this.fileName = fileName;
        this.location = "src/main/java/de/upb/sse/cutNRun/results";
        isOverwriteFile = overwriteFile;
        filePath = Path.of(location + java.io.File.separator + fileName + ".xlsx");
    }

    @Override
    public void setHeaders(String... headers) {
        this.headers = headers;
    }

    @Override
    public void saveData(Map<String, Object[]> data) {
        if (isOverwriteFile || !Files.exists(filePath)) {
            writeExcelFile(data, true);
        } else {
            try {
                Map<String, Object[]> oldData = readExcelData();
                oldData.putAll(data);
                writeExcelFile(oldData, false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void writeExcelFile(Map<String, Object[]> data, boolean isCreateHeader) {
        //Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet(fileName);

        //Prepare data to be written as an Object[]
        /*Map<String, Object[]> data = new TreeMap<String, Object[]>();
        data.put("1", new Object[]{"ID", "NAME", "LASTNAME"});
        data.put("2", new Object[]{1, "Amit", "Shukla"});
        data.put("3", new Object[]{2, "Lokesh", "Gupta"});
        data.put("4", new Object[]{3, "John", "Adwards"});
        data.put("5", new Object[]{4, "Brian", "Schultz"});*/
        if(isCreateHeader) {
            createHeaderRow(sheet);
        }

        //Iterate over data and write to sheet
        Set<String> keyset = data.keySet();
        int rownum = 1;
        for (String key : keyset) {

            Row row = sheet.createRow(rownum++);
            Object[] objArr = data.get(key);
            int cellnum = 0;
            for (Object obj : objArr) {
                Cell cell = row.createCell(cellnum++);
                /*if (obj instanceof String)
                    cell.setCellValue((String) obj);
                else if (obj instanceof Integer)
                    cell.setCellValue((Integer) obj);*/
                cell.setCellValue(String.valueOf(obj));
            }
        }

        //Write the workbook in file system
        try {
            FileOutputStream out = new FileOutputStream(new File(location + java.io.File.separator
                                                                         + fileName + ".xlsx"));
            workbook.write(out);
            out.close();
            System.out.println(fileName + ".xlsx written successfully on disk.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createHeaderRow(XSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        int cellnum = 0;
        for (Object obj : headers) {
            Cell cell = headerRow.createCell(cellnum++);
            cell.setCellValue((String) obj);
        }
    }

    public Map<String, Object[]> readExcelData() throws IOException {
        FileInputStream file = new FileInputStream(new File(location + java.io.File.separator
                                                                    + fileName + ".xlsx"));
        //Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(file);

        //Get first/desired sheet from the workbook
        XSSFSheet sheet = workbook.getSheetAt(0);

        //Iterate through each rows one by one
        Iterator<Row> rowIterator = sheet.iterator();
        Map<String, Object[]> data = new LinkedHashMap<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            //For each row, iterate through all the columns
            Iterator<Cell> cellIterator = row.cellIterator();
            Object[] rowData = new Object[headers.length];
            int cellIndex = 0;
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                //Check the cell type and format accordingly
                switch (cell.getCellType()) {
                    case NUMERIC:
                        rowData[cellIndex] = cell.getNumericCellValue();
                        break;
                    case STRING:
                        rowData[cellIndex] = cell.getStringCellValue();
                        break;
                }
                cellIndex++;
            }
            System.out.println("");
            data.put((String) rowData[0], rowData);
        }
        file.close();
        return data;
    }

    public Path getFilePath() {
        return filePath;
    }

    @Override
    public boolean isJarWritten(String jarName) throws IOException {
        if(Files.exists(filePath)){
            Map<String, Object[]> oldData = readExcelData();
            return oldData.keySet().contains(jarName);
        }
        return false;
    }


    public List<Object[]> readExcelDataAsIs() throws IOException {
        FileInputStream file = new FileInputStream(new File(location + java.io.File.separator
                                                                    + fileName + ".xlsx"));
        //Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(file);

        //Get first/desired sheet from the workbook
        XSSFSheet sheet = workbook.getSheetAt(0);

        //Iterate through each rows one by one
        Iterator<Row> rowIterator = sheet.iterator();
        List<Object[]> data = new ArrayList<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            //For each row, iterate through all the columns
            Iterator<Cell> cellIterator = row.cellIterator();
            Object[] rowData = new Object[headers.length];
            int cellIndex = 0;
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                //Check the cell type and format accordingly
                switch (cell.getCellType()) {
                    case NUMERIC:
                        rowData[cellIndex] = cell.getNumericCellValue();
                        break;
                    case STRING:
                        rowData[cellIndex] = cell.getStringCellValue();
                        break;
                }
                cellIndex++;
            }
            System.out.println("");
            data.add(rowData);
        }
        file.close();
        return data;
    }
}
