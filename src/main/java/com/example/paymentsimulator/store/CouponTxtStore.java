package com.example.paymentsimulator.store;

import com.example.paymentsimulator.domain.Coupon;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;

public class CouponTxtStore {

    private final Path file;

    public CouponTxtStore(String filePath) {
        this.file = Paths.get(filePath);
    }

    public Optional<Coupon> findByCode(String code) {
        try {
            if (!Files.exists(file)) return Optional.empty();
            return Files.readAllLines(file).stream()
                    .map(String::trim)
                    .filter(line -> !line.isBlank() && !line.startsWith("#"))
                    .map(this::parseCoupon)
                    .filter(c -> c.code().equals(code))
                    .findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public boolean incrementUse(String code) {
        try {
            if (!Files.exists(file)) return false;
            List<String> lines = Files.readAllLines(file);
            boolean updated = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isBlank() || line.startsWith("#")) continue;

                Coupon c = parseCoupon(line);
                if (c.code().equals(code)) {
                    if (c.usedTotal() >= c.maxUsesTotal()) return false;

                    String newLine = String.format("%s,%d,%d,%s,%s",
                            c.code(),
                            c.maxUsesTotal(),
                            c.usedTotal() + 1,
                            c.minAmountRequired(),
                            c.discountAmount()
                    );
                    lines.set(i, newLine);
                    updated = true;
                    break;
                }
            }

            if (updated) {
                Files.write(file, lines, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            }
            return updated;

        } catch (IOException e) {
            return false;
        }
    }

    private Coupon parseCoupon(String line) {
        // code,maxUsesTotal,usedTotal,minAmountRequired,discountAmount
        String[] p = line.split(",");
        String code = p[0].trim();
        int maxUses = Integer.parseInt(p[1].trim());
        int used = Integer.parseInt(p[2].trim());
        BigDecimal min = new BigDecimal(p[3].trim());
        BigDecimal disc = new BigDecimal(p[4].trim());
        return new Coupon(code, min, disc, maxUses, used);
    }
}
