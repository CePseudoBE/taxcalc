package be.hoffmann.backtaxes.dto.mapper;

import be.hoffmann.backtaxes.dto.response.ModelResponse;
import be.hoffmann.backtaxes.entity.Model;

import java.util.List;

/**
 * Mapper pour convertir les entites Model en DTOs.
 */
public final class ModelMapper {

    private ModelMapper() {
        // Utility class
    }

    public static ModelResponse toResponse(Model model) {
        if (model == null) {
            return null;
        }
        return new ModelResponse(
                model.getId(),
                model.getName(),
                BrandMapper.toResponse(model.getBrand())
        );
    }

    public static List<ModelResponse> toResponseList(List<Model> models) {
        if (models == null) {
            return List.of();
        }
        return models.stream()
                .map(ModelMapper::toResponse)
                .toList();
    }
}
