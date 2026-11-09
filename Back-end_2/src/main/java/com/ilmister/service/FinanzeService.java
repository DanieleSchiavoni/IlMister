package com.ilmister.service;

import com.ilmister.dto.FinanzeResponse;

public interface FinanzeService {

    FinanzeResponse finanze(Long carrieraId, Long utenteId);
}
