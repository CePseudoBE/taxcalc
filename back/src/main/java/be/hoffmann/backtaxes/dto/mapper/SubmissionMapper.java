package be.hoffmann.backtaxes.dto.mapper;

import be.hoffmann.backtaxes.dto.request.VehicleSubmissionRequest;
import be.hoffmann.backtaxes.dto.response.SubmissionResponse;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.entity.VehicleSubmission;
import be.hoffmann.backtaxes.entity.enums.SubmissionStatus;

import java.util.List;

/**
 * Mapper pour convertir les entites VehicleSubmission en DTOs.
 */
public final class SubmissionMapper {

    private SubmissionMapper() {
        // Utility class
    }

    public static SubmissionResponse toResponse(VehicleSubmission submission) {
        if (submission == null) {
            return null;
        }

        SubmissionResponse.VehicleData vehicleData = new SubmissionResponse.VehicleData(
                submission.getBrandName(),
                submission.getModelName(),
                submission.getVariantName(),
                submission.getYearStart(),
                submission.getYearEnd(),
                submission.getPowerKw(),
                submission.getFiscalHp(),
                submission.getFuel(),
                submission.getEuroNorm(),
                submission.getCo2Wltp(),
                submission.getCo2Nedc(),
                submission.getDisplacementCc(),
                submission.getMmaKg(),
                submission.getHasParticleFilter()
        );

        return new SubmissionResponse(
                submission.getId(),
                submission.getStatus(),
                vehicleData,
                submission.getSubmitter() != null ? submission.getSubmitter().getId() : null,
                submission.getCreatedAt(),
                submission.getReviewer() != null ? submission.getReviewer().getId() : null,
                submission.getReviewedAt(),
                submission.getFeedback(),
                submission.getCreatedVariant() != null ? submission.getCreatedVariant().getId() : null
        );
    }

    public static VehicleSubmission toEntity(VehicleSubmissionRequest request, User submitter) {
        if (request == null) {
            return null;
        }

        VehicleSubmission submission = new VehicleSubmission();
        submission.setSubmitter(submitter);
        submission.setStatus(SubmissionStatus.pending);
        submission.setBrandName(request.getBrandName());
        submission.setModelName(request.getModelName());
        submission.setVariantName(request.getVariantName());
        submission.setYearStart(request.getYearStart());
        submission.setYearEnd(request.getYearEnd());
        submission.setPowerKw(request.getPowerKw());
        submission.setFiscalHp(request.getFiscalHp());
        submission.setFuel(request.getFuel());
        submission.setEuroNorm(request.getEuroNorm());
        submission.setCo2Wltp(request.getCo2Wltp());
        submission.setCo2Nedc(request.getCo2Nedc());
        submission.setDisplacementCc(request.getDisplacementCc());
        submission.setMmaKg(request.getMmaKg());
        submission.setHasParticleFilter(request.getHasParticleFilter());

        return submission;
    }

    public static List<SubmissionResponse> toResponseList(List<VehicleSubmission> submissions) {
        if (submissions == null) {
            return List.of();
        }
        return submissions.stream()
                .map(SubmissionMapper::toResponse)
                .toList();
    }
}
