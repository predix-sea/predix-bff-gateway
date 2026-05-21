package com.predix.bff.compliance;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class IpExtractorTest {

    @Test
    void prefersCfConnectingIp() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("CF-Connecting-IP", "1.1.1.1");
        req.addHeader("X-Forwarded-For", "2.2.2.2");
        assertThat(IpExtractor.extractClientIp(req)).isEqualTo("1.1.1.1");
    }

    @Test
    void parsesXForwardedForFirstHop() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Forwarded-For", "3.3.3.3, 4.4.4.4");
        assertThat(IpExtractor.extractClientIp(req)).isEqualTo("3.3.3.3");
    }
}
