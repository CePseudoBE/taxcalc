package be.hoffmann.backtaxes.dto.mapper;

import be.hoffmann.backtaxes.dto.response.BrandResponse;
import be.hoffmann.backtaxes.entity.Brand;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir les entites Brand en DTOs.
 */
public final class BrandMapper {

    private BrandMapper() {
        // Utility class
    }

    public static BrandResponse toResponse(Brand brand) {
        if (brand == null) {
            return null;
        }
        return new BrandResponse(brand.getId(), brand.getName());
    }

    public static List<BrandResponse> toResponseList(List<Brand> brands) {
        if (brands == null) {
            return List.of();
        }
        return brands.stream()
                .map(BrandMapper::toResponse)
                .collect(Collectors.toList());
    }
}
