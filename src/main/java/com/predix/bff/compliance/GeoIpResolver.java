package com.predix.bff.compliance;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.predix.bff.config.PredixProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;

@Component
public class GeoIpResolver {

    private static final Logger log = LoggerFactory.getLogger(GeoIpResolver.class);
    private final PredixProperties.ComplianceProperties compliance;
    private DatabaseReader reader;

    public GeoIpResolver(PredixProperties properties) {
        this.compliance = properties.compliance();
    }

    @PostConstruct
    void init() {
        String path = compliance.geoipDatabasePath();
        if (path != null && !path.isBlank()) {
            try {
                reader = new DatabaseReader.Builder(new File(path)).build();
                log.info("GeoIP database loaded from {}", path);
            } catch (IOException e) {
                log.warn("Failed to load GeoIP database, falling back to heuristics: {}", e.getMessage());
            }
        }
    }

    public Optional<String> resolveCountry(String ip) {
        if (ip == null || ip.isBlank() || isPrivate(ip)) {
            return Optional.empty();
        }
        if (reader != null) {
            try {
                CountryResponse response = reader.country(InetAddress.getByName(ip));
                return Optional.ofNullable(response.getCountry().getIsoCode());
            } catch (IOException | GeoIp2Exception e) {
                log.debug("GeoIP lookup failed for {}: {}", ip, e.getMessage());
            }
        }
        return CnIpHeuristics.resolveCountry(ip);
    }

    private boolean isPrivate(String ip) {
        return ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("127.")
                || ip.equals("::1") || ip.startsWith("172.16.") || ip.startsWith("172.17.");
    }
}
