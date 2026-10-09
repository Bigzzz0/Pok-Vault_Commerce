package com.pokevault.modules.web.service;

import com.pokevault.modules.catalog.dto.UserProfileResponse;
import com.pokevault.modules.web.dto.RegisterRequest;

public interface RegistrationService {

    UserProfileResponse registerCustomer(RegisterRequest request);
}
