package com.project.covidguard.web.services;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface VerificationService {

    public String registerUUIDAndGetToken(String uuid) throws IOException;
}
