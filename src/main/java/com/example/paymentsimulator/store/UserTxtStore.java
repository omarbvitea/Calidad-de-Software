package com.example.paymentsimulator.store;

import com.example.paymentsimulator.domain.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;

public class UserTxtStore {

    private final Path file;

    public UserTxtStore(String filePath) {
        this.file = Paths.get(filePath);
    }

    public Optional<User> findById(String userId) {
        try {
            if (!Files.exists(file)) return Optional.empty();
            return Files.readAllLines(file).stream()
                    .map(String::trim)
                    .filter(line -> !line.isBlank() && !line.startsWith("#"))
                    .map(this::parseUser)
                    .filter(u -> u.id().equals(userId))
                    .findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private User parseUser(String line) {
        // userId,name,email,commissionRate,creditLimit,currentConsumption
        String[] parts = line.split(",");
        String id = parts[0].trim();
        String name = parts[1].trim();
        String email = parts[2].trim();

        BigDecimal commissionRate = parts[3].isBlank() ? null : new BigDecimal(parts[3].trim());
        BigDecimal creditLimit = new BigDecimal(parts[4].trim());
        BigDecimal currentConsumption = new BigDecimal(parts[5].trim());

        return new User(id, name, email, commissionRate, creditLimit, currentConsumption);
    }
}
