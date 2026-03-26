package com.inventory.inventory_system.util;

import com.inventory.inventory_system.dto.LowStockLogDTO;
import com.inventory.inventory_system.entity.LowStockLog;

import java.util.List;
import java.util.stream.Collectors;

public class LowStockLogMapper {

    // Convert LowStockLog entity to LowStockLogDTO
    public static LowStockLogDTO toDTO(LowStockLog log) {
        if (log == null) return null;
        
        return new LowStockLogDTO(
            log.getId(),
            log.getProductId(),
            log.getProductName(),
            log.getCategory(),
            log.getCurrentQuantity(),
            log.getReorderLevel(),
            log.getPrice(),
            log.getStatus(),
            log.getDetectedAt(),
            log.getResolvedAt(),
            log.getResolvedBy(),
            log.getNotes(),
            log.isResolved()
        );
    }

    // Convert LowStockLogDTO to LowStockLog entity
    public static LowStockLog toEntity(LowStockLogDTO dto) {
        if (dto == null) return null;
        
        LowStockLog log = new LowStockLog();
        log.setId(dto.getId());
        log.setProductId(dto.getProductId());
        log.setProductName(dto.getProductName());
        log.setCategory(dto.getCategory());
        log.setCurrentQuantity(dto.getCurrentQuantity());
        log.setReorderLevel(dto.getReorderLevel());
        log.setPrice(dto.getPrice());
        log.setStatus(dto.getStatus());
        log.setDetectedAt(dto.getDetectedAt());
        log.setResolvedAt(dto.getResolvedAt());
        log.setResolvedBy(dto.getResolvedBy());
        log.setNotes(dto.getNotes());
        log.setResolved(dto.isResolved());
        
        return log;
    }

    // Convert list of LowStockLog entities to LowStockLogDTOs
    public static List<LowStockLogDTO> toDTOList(List<LowStockLog> logs) {
        return logs.stream()
                .map(LowStockLogMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Convert list of LowStockLogDTOs to LowStockLog entities
    public static List<LowStockLog> toEntityList(List<LowStockLogDTO> dtos) {
        return dtos.stream()
                .map(LowStockLogMapper::toEntity)
                .collect(Collectors.toList());
    }
}