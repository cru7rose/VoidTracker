package com.example.order_service.service;

import com.example.order_service.entity.AssetEntity;
import com.example.order_service.repository.AssetRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AssetService.class);

    private final AssetRepository assetRepository;

    @Transactional
    public void linkAssets(UUID parentId, List<UUID> childIds) {
        log.info("Linking assets {} to parent {}", childIds, parentId);
        AssetEntity parent = assetRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Parent asset not found: " + parentId));

        List<AssetEntity> children = assetRepository.findAllById(childIds);
        if (children.size() != childIds.size()) {
            throw new EntityNotFoundException("One or more child assets not found");
        }

        for (AssetEntity child : children) {
            child.setParent(parent);
        }

        // No need to save parent explicitly if cascade is set, but saving children
        // updates the relationship
        assetRepository.saveAll(children);
    }

    @Transactional(readOnly = true)
    public AssetEntity getAssetWithHierarchy(UUID assetId) {
        AssetEntity asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found: " + assetId));

        // Force initialization of children
        asset.getChildren().size();

        return asset;
    }
}
