package be.hoffmann.backtaxes.dto.mapper;

import be.hoffmann.backtaxes.dto.response.VariantDetailResponse;
import be.hoffmann.backtaxes.dto.response.VariantResponse;
import be.hoffmann.backtaxes.entity.Variant;

import java.util.List;

/**
 * Mapper pour convertir les entites Variant en DTOs.
 */
public final class VariantMapper {

    private VariantMapper() {
        // Utility class
    }

    public static VariantResponse toResponse(Variant variant) {
        if (variant == null) {
            return null;
        }
        return new VariantResponse(
                variant.getId(),
                variant.getName(),
                variant.getYearStart(),
                variant.getYearEnd(),
                variant.getPowerKw(),
                variant.getFiscalHp(),
                variant.getDisplacementCc(),
                variant.getFuel(),
                variant.getEuroNorm(),
                variant.getCo2Wltp()
        );
    }

    public static VariantDetailResponse toDetailResponse(Variant variant) {
        if (variant == null) {
            return null;
        }
        return new VariantDetailResponse(
                variant.getId(),
                variant.getName(),
                ModelMapper.toResponse(variant.getModel()),
                variant.getYearStart(),
                variant.getYearEnd(),
                variant.getPowerKw(),
                variant.getFiscalHp(),
                variant.getFuel(),
                variant.getEuroNorm(),
                variant.getCo2Wltp(),
                variant.getCo2Nedc(),
                variant.getDisplacementCc(),
                variant.getMmaKg(),
                variant.getHasParticleFilter()
        );
    }

    public static List<VariantResponse> toResponseList(List<Variant> variants) {
        if (variants == null) {
            return List.of();
        }
        return variants.stream()
                .map(VariantMapper::toResponse)
                .toList();
    }
}
