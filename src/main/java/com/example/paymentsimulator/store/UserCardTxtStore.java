package com.example.paymentsimulator.store;

import com.example.paymentsimulator.domain.Bank;
import com.example.paymentsimulator.domain.Card;
import com.example.paymentsimulator.domain.UserCard;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class UserCardTxtStore {

    private final Path file;

    public UserCardTxtStore(String filePath) {
        this.file = Paths.get(filePath);
    }

    public List<UserCard> findCardsByUser(String userId) {
        try {
            if (!Files.exists(file)) return List.of();
            return Files.readAllLines(file).stream()
                    .map(String::trim)
                    .filter(line -> !line.isBlank() && !line.startsWith("#"))
                    .map(this::parseUserCard)
                    .filter(uc -> uc.userId().equals(userId))
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private UserCard parseUserCard(String line) {
        // userId,maskedNumber,bank
        String[] parts = line.split(",");
        String userId = parts[0].trim();
        String maskedNumber = parts[1].trim();
        Bank bank = Bank.valueOf(parts[2].trim().toUpperCase());
        return new UserCard(userId, new Card(maskedNumber, bank));
    }
}
