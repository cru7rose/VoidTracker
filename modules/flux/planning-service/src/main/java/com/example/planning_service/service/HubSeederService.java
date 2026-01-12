package com.example.planning_service.service;

import com.example.planning_service.entity.HubEntity;
import com.example.planning_service.repository.HubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubSeederService implements CommandLineRunner {

    private final HubRepository hubRepository;
    private static final String EXCEL_PATH = "/app/DANXILS_HUBS.xlsx";

    @Override
    public void run(String... args) {
        if (hubRepository.count() > 0) {
            log.info("Hubs already seeded. Skipping migration.");
            return;
        }

        log.info("Starting Hub Migration from: {}", EXCEL_PATH);
        try (InputStream is = new FileInputStream(EXCEL_PATH);
                Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0); // Assume first sheet
            List<HubEntity> hubs = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Skip header

                // Safe extraction assuming columns: Alias, Name, Street, HouseNo, Postal, City,
                // Country
                // Order based on ConsolidationHub model observation or standard expectation.
                // If this fails, we will refine column mapping.

                String alias = getCellValue(row, 0);
                String name = getCellValue(row, 1);

                if (alias.isEmpty() || name.isEmpty())
                    continue;

                HubEntity hub = HubEntity.builder()
                        .hubCode(alias)
                        .name(name)
                        .street(getCellValue(row, 2))
                        .houseNumber(getCellValue(row, 3))
                        .postalCode(getCellValue(row, 4))
                        .city(getCellValue(row, 5))
                        .country(getCellValue(row, 6))
                        .isActive(true)
                        .build();

                hubs.add(hub);
            }

            hubRepository.saveAll(hubs);
            log.info("Successfully migrated {} hubs.", hubs.size());

        } catch (FileNotFoundException e) {
            log.warn("Migration File not found at {}. Skipping. Please ensure file exists for migration.", EXCEL_PATH);
        } catch (Exception e) {
            log.error("Failed to migrate Hubs", e);
        }
    }

    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null)
            return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> "";
        };
    }
}
