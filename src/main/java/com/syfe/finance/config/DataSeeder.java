package com.syfe.finance.config;

import com.syfe.finance.entity.Category;
import com.syfe.finance.entity.TransactionType;
import com.syfe.finance.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        // Check if default categories already exist
        if (categoryRepository.findByUserIsNull().isEmpty()) {
            log.info("Seeding default categories...");
            seedDefaultCategories();
            log.info("Default categories seeded successfully.");
        } else {
            log.info("Default categories already exist. Skipping seeding.");
        }
    }

    private void seedDefaultCategories() {
        List<Category> defaultCategories = List.of(
            // INCOME category
            Category.builder()
                .name("Salary")
                .type(TransactionType.INCOME)
                .user(null)
                .build(),
            
            // EXPENSE categories
            Category.builder()
                .name("Food")
                .type(TransactionType.EXPENSE)
                .user(null)
                .build(),
            
            Category.builder()
                .name("Rent")
                .type(TransactionType.EXPENSE)
                .user(null)
                .build(),
            
            Category.builder()
                .name("Transportation")
                .type(TransactionType.EXPENSE)
                .user(null)
                .build(),
            
            Category.builder()
                .name("Entertainment")
                .type(TransactionType.EXPENSE)
                .user(null)
                .build(),
            
            Category.builder()
                .name("Healthcare")
                .type(TransactionType.EXPENSE)
                .user(null)
                .build(),
            
            Category.builder()
                .name("Utilities")
                .type(TransactionType.EXPENSE)
                .user(null)
                .build()
        );

        categoryRepository.saveAll(defaultCategories);
    }
}
