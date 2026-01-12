package com.example.order_service.service;

import com.example.order_service.entity.ChainOfCustodyEntity;
import com.example.order_service.repository.ChainOfCustodyRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

@Service
public class ChainOfCustodyService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ChainOfCustodyService.class);

    private final ChainOfCustodyRepository repository;

    public ChainOfCustodyService(ChainOfCustodyRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void recordEvent(UUID orderId, String action, String actor, String data) {
        log.info("Recording Chain of Custody event: {} for order {}", action, orderId);

        ChainOfCustodyEntity lastBlock = repository.findTopByOrderIdOrderByTimestampDesc(orderId)
                .orElse(null);

        String previousHash = (lastBlock != null) ? lastBlock.getDataHash() : "0"; // Genesis block has prevHash "0"
        Instant now = Instant.now();

        String dataToHash = action + actor + now.toString() + data;
        String dataHash = calculateSha256(dataToHash);

        // In a real blockchain, we'd hash the previous hash + data hash combined,
        // but here we just store the data hash and link to previous.
        // Let's make the "Block Hash" be the dataHash for simplicity in this MVP,
        // or strictly: BlockHash = SHA256(PrevHash + DataHash + Nonce).
        // For this MVP, we use dataHash as the identifier.

        ChainOfCustodyEntity newBlock = ChainOfCustodyEntity.builder()
                .orderId(orderId)
                .action(action)
                .actor(actor)
                .timestamp(now)
                .previousHash(previousHash)
                .dataHash(dataHash)
                .build();

        repository.save(newBlock);
    }

    private String calculateSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
