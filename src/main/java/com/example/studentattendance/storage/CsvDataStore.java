package com.example.studentattendance.storage;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class CsvDataStore {
    private static final System.Logger LOGGER = System.getLogger(CsvDataStore.class.getName());
    private static final String DATA_DIRECTORY_NAME = ".studentattendance";

    private CsvDataStore() {
    }

    public static boolean fileExists(String fileName) {
        return Files.exists(resolveDataFile(fileName));
    }

    public static List<CSVRecord> readRecords(String fileName, String... expectedHeaders) {
        Path file = resolveDataFile(fileName);
        if (!Files.exists(file)) {
            return List.of();
        }

        try {
            if (Files.size(file) == 0) {
                return List.of();
            }

            // Detect delimiter: default comma, fallback to semicolon if header line contains ';'
            char delimiter = ',';
            try {
                String firstLine = Files.readAllLines(file, StandardCharsets.UTF_8).get(0);
                if (firstLine.contains(";")) delimiter = ';';
            } catch (IOException e) {
                // ignore, keep default delimiter
            }
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setDelimiter(delimiter)
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .build();
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
                 CSVParser parser = format.parse(reader)) {
                List<String> headers = List.of(expectedHeaders);
                List<String> actualHeaders = parser.getHeaderNames();
                if (!actualHeaders.equals(headers) && !actualHeaders.containsAll(headers)) {
                    throw new IllegalArgumentException(
                            "Unexpected CSV header in " + file + "; expected " + headers + " but found " + actualHeaders);
                }

                List<CSVRecord> records = new ArrayList<>();
                for (CSVRecord record : parser) {
                    if (!record.isConsistent()) {
                        throw new IllegalArgumentException(
                                "Invalid column count in " + file + " at record " + record.getRecordNumber());
                    }
                    records.add(record);
                }
                return List.copyOf(records);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read application data from " + file, e);
        }
    }

    public static void writeRecords(String fileName, List<String> headers, Iterable<Object[]> records) {
        Path file = resolveDataFile(fileName);
        Path temporaryFile = null;
        try {
            Files.createDirectories(file.getParent());
            temporaryFile = Files.createTempFile(file.getParent(), file.getFileName().toString(), ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8);
                 CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
                printer.printRecord(headers);
                for (Object[] record : records) {
                    printer.printRecord(record);
                }
            }

            try {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException cleanupException) {
                    e.addSuppressed(cleanupException);
                }
            }
            throw new UncheckedIOException("Unable to save application data to " + file, e);
        }
    }

    private static Path resolveDataFile(String fileName) {
        Path name = Path.of(fileName);
        if (name.isAbsolute() || name.getNameCount() != 1 || !name.getFileName().toString().equals(fileName)) {
            throw new IllegalArgumentException("Application data file name must be a simple file name.");
        }

        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isBlank()) {
            throw new IllegalStateException("The user home directory is not configured.");
        }

        Path dataDirectory = Path.of(userHome).resolve(DATA_DIRECTORY_NAME);
        Path destination = dataDirectory.resolve(name);
        if (Files.exists(destination)) {
            return destination;
        }

        String workingDirectory = System.getProperty("user.dir");
        if (workingDirectory == null || workingDirectory.isBlank()) {
            throw new IllegalStateException("The application working directory is not configured.");
        }

        Path legacyFile = Path.of(workingDirectory).resolve(name);
        if (Files.isRegularFile(legacyFile)) {
            try {
                Files.createDirectories(dataDirectory);
                Files.copy(legacyFile, destination);
                LOGGER.log(System.Logger.Level.INFO, "Migrated legacy data file to " + destination);
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to migrate application data from " + legacyFile, e);
            }
        }
        return destination;
    }
}
