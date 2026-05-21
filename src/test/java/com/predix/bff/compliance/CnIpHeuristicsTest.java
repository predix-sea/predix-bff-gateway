package com.predix.bff.compliance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CnIpHeuristicsTest {

    @Test
    void detectsKnownCnTestIp() {
        assertThat(CnIpHeuristics.resolveCountry("203.0.113.1")).contains("CN");
    }

    @Test
    void unknownIpReturnsEmpty() {
        assertThat(CnIpHeuristics.resolveCountry("8.8.8.8")).isEmpty();
    }
}
