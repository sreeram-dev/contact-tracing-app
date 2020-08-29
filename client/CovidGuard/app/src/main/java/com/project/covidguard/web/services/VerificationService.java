package com.project.covidguard.web.services;

import java.io.IOException;

public interface VerificationService {

    public String registerUUIDAndGetToken(String uuid) throws IOException;
}
