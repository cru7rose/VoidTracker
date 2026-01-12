package com.example.order_service.controller;

import com.example.order_service.entity.AssetEntity;
import com.example.order_service.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Tag(name = "Asset Management", description = "API for managing asset hierarchies")
public class AssetController {

    private final AssetService assetService;

    @PostMapping("/{parentId}/link")
    @Operation(summary = "Link child assets to a parent asset")
    public ResponseEntity<Void> linkAssets(@PathVariable UUID parentId, @RequestBody List<UUID> childIds) {
        assetService.linkAssets(parentId, childIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/hierarchy")
    @Operation(summary = "Get asset with its children")
    public ResponseEntity<AssetEntity> getAssetHierarchy(@PathVariable UUID id) {
        return ResponseEntity.ok(assetService.getAssetWithHierarchy(id));
    }
}
