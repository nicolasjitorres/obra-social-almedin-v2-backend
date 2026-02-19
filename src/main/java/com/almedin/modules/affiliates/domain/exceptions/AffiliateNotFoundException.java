package com.almedin.modules.affiliates.domain.exceptions;

import com.almedin.modules.shared.domain.exceptions.EntityNotFoundException;

public class AffiliateNotFoundException extends EntityNotFoundException {
    public AffiliateNotFoundException(Long id) {
        super("Afiliado no encontrado con ID: " + id);
    }
}