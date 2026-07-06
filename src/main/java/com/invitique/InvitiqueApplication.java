package com.invitique;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
public class InvitiqueApplication {
    public static void main(String[] args) {
        loadEnv();
        SpringApplication.run(InvitiqueApplication.class, args);
    }

    private static void loadEnv() {
        System.out.println("=== SYSTEM STARTUP DIAGNOSTICS ===");
        
        // 1. Load local .env if it exists
        try {
            if (Files.exists(Paths.get(".env"))) {
                System.out.println("-> Detected local .env file. Loading keys...");
                List<String> lines = Files.readAllLines(Paths.get(".env"));
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int eqIdx = line.indexOf('=');
                    if (eqIdx > 0) {
                        String key = line.substring(0, eqIdx).trim();
                        String value = line.substring(eqIdx + 1).trim();
                        // Remove surrounding quotes if any
                        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        } else if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        }
                        System.setProperty(key, value);
                    }
                }
            } else {
                System.out.println("-> No local .env file detected in current directory (" + Paths.get(".").toAbsolutePath() + ")");
            }
        } catch (IOException e) {
            System.err.println("Failed to load .env file: " + e.getMessage());
        }

        // 2. Parse DATABASE_URL if present (Render/Heroku automatic database binding)
        // Only parse if SPRING_DATASOURCE_URL is not already explicitly set in env/properties
        String springDsUrlCheck = System.getenv("SPRING_DATASOURCE_URL");
        if (springDsUrlCheck == null) springDsUrlCheck = System.getProperty("SPRING_DATASOURCE_URL");

        String databaseUrl = null;
        if (springDsUrlCheck == null) {
            databaseUrl = System.getenv("DATABASE_URL");
            if (databaseUrl == null) {
                databaseUrl = System.getProperty("DATABASE_URL");
            }
        } else {
            System.out.println("-> SPRING_DATASOURCE_URL is explicitly set. Skipping DATABASE_URL parsing.");
        }

        if (databaseUrl != null) {
            System.out.println("-> Detected DATABASE_URL environment variable!");
            if (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://")) {
                try {
                    // Scheme format: postgres[ql]://username:password@host:port/database
                    String scheme = databaseUrl.startsWith("postgresql://") ? "postgresql://" : "postgres://";
                    String cleanUrl = databaseUrl.substring(scheme.length());
                    
                    int atIdx = cleanUrl.indexOf('@');
                    if (atIdx > 0) {
                        String credentials = cleanUrl.substring(0, atIdx);
                        String hostAndDb = cleanUrl.substring(atIdx + 1);
                        
                        String username = "";
                        String password = "";
                        int colonIdx = credentials.indexOf(':');
                        if (colonIdx > 0) {
                            username = credentials.substring(0, colonIdx);
                            password = credentials.substring(colonIdx + 1);
                        } else {
                            username = credentials;
                        }
                        
                        int slashIdx = hostAndDb.indexOf('/');
                        String host = "";
                        String dbName = "";
                        if (slashIdx > 0) {
                            host = hostAndDb.substring(0, slashIdx);
                            dbName = hostAndDb.substring(slashIdx + 1);
                        } else {
                            host = hostAndDb;
                        }
                        
                        // Build correct JDBC URL
                        String jdbcUrl = "jdbc:postgresql://" + host + "/" + dbName;
                        
                        System.setProperty("SPRING_DATASOURCE_URL", jdbcUrl);
                        System.setProperty("SPRING_DATASOURCE_USERNAME", username);
                        System.setProperty("SPRING_DATASOURCE_PASSWORD", password);
                        
                        System.out.println("-> Successfully parsed DATABASE_URL into JDBC format!");
                        System.out.println("   JDBC URL: " + jdbcUrl);
                        System.out.println("   Username: " + username);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to parse DATABASE_URL: " + e.getMessage());
                }
            } else {
                System.out.println("   DATABASE_URL does not start with postgres:// scheme. Skipping parsing.");
            }
        } else {
            System.out.println("-> No DATABASE_URL environment variable found.");
        }

        // 3. Log state of manual SPRING_DATASOURCE variables
        String springDsUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (springDsUrl == null) springDsUrl = System.getProperty("SPRING_DATASOURCE_URL");
        
        String springDsUser = System.getenv("SPRING_DATASOURCE_USERNAME");
        if (springDsUser == null) springDsUser = System.getProperty("SPRING_DATASOURCE_USERNAME");

        System.out.println("-> Final resolved datasource properties in memory:");
        System.out.println("   url: " + (springDsUrl != null ? springDsUrl : "NULL (falling back to application.yml default)"));
        System.out.println("   username: " + (springDsUser != null ? springDsUser : "NULL (falling back to application.yml default)"));
        System.out.println("==================================");
    }
}
