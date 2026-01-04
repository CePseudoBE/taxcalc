package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.dto.response.TaxCalculationResponse;
import be.hoffmann.backtaxes.entity.SearchEvent;
import be.hoffmann.backtaxes.entity.TaxCalculation;
import be.hoffmann.backtaxes.entity.enums.*;
import be.hoffmann.backtaxes.repository.SearchEventRepository;
import be.hoffmann.backtaxes.repository.TaxCalculationRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Service pour l'enregistrement des evenements analytics.
 * Les appels sont asynchrones pour ne pas impacter la latence.
 */
@Service
public class AnalyticsService {

    private static final String SESSION_COOKIE_NAME = "analytics_session";
    private static final int SESSION_COOKIE_MAX_AGE = 60 * 60 * 24; // 24 heures

    // Precompiled patterns for device detection (case insensitive)
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "mobile|android|iphone", Pattern.CASE_INSENSITIVE);
    private static final Pattern TABLET_PATTERN = Pattern.compile(
            "tablet|ipad", Pattern.CASE_INSENSITIVE);

    // HexFormat for efficient hex encoding (Java 17+)
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    private final SearchEventRepository searchEventRepository;
    private final TaxCalculationRepository taxCalculationRepository;

    public AnalyticsService(
            SearchEventRepository searchEventRepository,
            TaxCalculationRepository taxCalculationRepository) {
        this.searchEventRepository = searchEventRepository;
        this.taxCalculationRepository = taxCalculationRepository;
    }

    /**
     * Recupere ou cree un ID de session analytics.
     * Utilise Stream API pour une recherche efficace des cookies.
     */
    public UUID getOrCreateSessionId(HttpServletRequest request, HttpServletResponse response) {
        // Chercher le cookie existant avec Stream API
        Optional<UUID> existingSession = extractSessionFromCookies(request);
        if (existingSession.isPresent()) {
            return existingSession.get();
        }

        // Creer un nouveau session ID
        UUID sessionId = UUID.randomUUID();

        // Ajouter le cookie
        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, sessionId.toString());
        cookie.setMaxAge(SESSION_COOKIE_MAX_AGE);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return sessionId;
    }

    private Optional<UUID> extractSessionFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> SESSION_COOKIE_NAME.equals(c.getName()))
                .findFirst()
                .flatMap(c -> {
                    try {
                        return Optional.of(UUID.fromString(c.getValue()));
                    } catch (IllegalArgumentException e) {
                        return Optional.empty();
                    }
                });
    }

    /**
     * Enregistre un evenement de recherche (browse/filter).
     */
    @Async("analyticsExecutor")
    @Transactional
    public void logSearch(SearchEventBuilder builder) {
        SearchEvent event = builder.build();
        searchEventRepository.save(event);
    }

    /**
     * Enregistre un evenement de calcul de taxe.
     * Utilise pattern matching pour instanceof (Java 16+).
     */
    @Async("analyticsExecutor")
    @Transactional
    public void logCalculation(
            SearchEvent searchEvent,
            Long variantId,
            Long submissionId,
            Region region,
            TaxCalculationResponse result) {

        TaxCalculation calculation = new TaxCalculation();
        calculation.setSearchEvent(searchEvent);
        calculation.setVariant(null);
        calculation.setRegion(region);
        calculation.setTaxType(result.getTaxType());
        calculation.setCalculatedAmount(result.getAmount());
        calculation.setIsExempt(result.getIsExempt());

        // Extraire les details du breakdown avec pattern matching
        if (result.getBreakdown() != null) {
            if (result.getBreakdown().get("powerKw") instanceof Integer powerKw) {
                calculation.setPowerKw(powerKw);
            }
            if (result.getBreakdown().get("fiscalHp") instanceof Integer fiscalHp) {
                calculation.setCvFiscal(fiscalHp);
            }
            if (result.getBreakdown().get("co2Wltp") instanceof Integer co2) {
                calculation.setCo2Gkm(co2);
            }
            if (result.getBreakdown().get("vehicleAgeYears") instanceof Integer ageYears) {
                calculation.setVehicleAgeMonths(ageYears * 12);
            }
        }

        taxCalculationRepository.save(calculation);
    }

    /**
     * Hash le User-Agent pour anonymisation RGPD.
     * Utilise HexFormat (Java 17+) pour une conversion efficace.
     */
    public String hashUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(userAgent.getBytes(StandardCharsets.UTF_8));
            return HEX_FORMAT.formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Detecte le type d'appareil depuis le User-Agent.
     * Utilise des patterns precompiles pour de meilleures performances.
     */
    public DeviceType detectDeviceType(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        if (MOBILE_PATTERN.matcher(userAgent).find()) {
            return TABLET_PATTERN.matcher(userAgent).find()
                    ? DeviceType.tablet
                    : DeviceType.mobile;
        }
        return DeviceType.desktop;
    }

    /**
     * Extrait la source de trafic depuis le Referer.
     */
    public String extractReferrerSource(String referer) {
        if (referer == null || referer.isBlank()) {
            return "direct";
        }
        String ref = referer.toLowerCase();
        if (ref.contains("google")) return "google";
        if (ref.contains("facebook") || ref.contains("fb.com")) return "facebook";
        if (ref.contains("twitter") || ref.contains("x.com")) return "twitter";
        if (ref.contains("linkedin")) return "linkedin";
        if (ref.contains("instagram")) return "instagram";
        if (ref.contains("bing")) return "bing";
        return "other";
    }

    /**
     * Builder pour creer un SearchEvent.
     */
    public static class SearchEventBuilder {
        private UUID sessionId;
        private Long userId;
        private Long brandId;
        private Long modelId;
        private Long variantId;
        private Region region;
        private FuelType fuelType;
        private Boolean isNewVehicle;
        private LocalDate firstRegistrationDate;
        private SearchType searchType;
        private DeviceType deviceType;
        private String referrerSource;
        private String userAgentHash;
        private String language;

        public SearchEventBuilder sessionId(UUID sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public SearchEventBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public SearchEventBuilder brandId(Long brandId) {
            this.brandId = brandId;
            return this;
        }

        public SearchEventBuilder modelId(Long modelId) {
            this.modelId = modelId;
            return this;
        }

        public SearchEventBuilder variantId(Long variantId) {
            this.variantId = variantId;
            return this;
        }

        public SearchEventBuilder region(Region region) {
            this.region = region;
            return this;
        }

        public SearchEventBuilder fuelType(FuelType fuelType) {
            this.fuelType = fuelType;
            return this;
        }

        public SearchEventBuilder isNewVehicle(Boolean isNewVehicle) {
            this.isNewVehicle = isNewVehicle;
            return this;
        }

        public SearchEventBuilder firstRegistrationDate(LocalDate firstRegistrationDate) {
            this.firstRegistrationDate = firstRegistrationDate;
            return this;
        }

        public SearchEventBuilder searchType(SearchType searchType) {
            this.searchType = searchType;
            return this;
        }

        public SearchEventBuilder deviceType(DeviceType deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public SearchEventBuilder referrerSource(String referrerSource) {
            this.referrerSource = referrerSource;
            return this;
        }

        public SearchEventBuilder userAgentHash(String userAgentHash) {
            this.userAgentHash = userAgentHash;
            return this;
        }

        public SearchEventBuilder language(String language) {
            this.language = language;
            return this;
        }

        public SearchEvent build() {
            SearchEvent event = new SearchEvent();
            event.setSessionId(sessionId);
            event.setRegion(region);
            event.setFuelType(fuelType);
            event.setIsNewVehicle(isNewVehicle);
            event.setFirstRegistrationDate(firstRegistrationDate);
            event.setSearchType(searchType);
            event.setDeviceType(deviceType);
            event.setReferrerSource(referrerSource);
            event.setUserAgentHash(userAgentHash);
            event.setLanguage(language);
            return event;
        }
    }

    public SearchEventBuilder builder() {
        return new SearchEventBuilder();
    }
}
