package com.emekachukwulobe.flightbookingservice.service;

import com.emekachukwulobe.flightbookingservice.dto.response.LoginResponse;
import com.emekachukwulobe.flightbookingservice.security.TenantAwareUserDetails;

public interface AuthService {

    LoginResponse getLoginResponse(TenantAwareUserDetails userDetails);

}
