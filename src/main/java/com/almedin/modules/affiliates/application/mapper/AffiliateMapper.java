package com.almedin.modules.affiliates.application.mapper;

import com.almedin.modules.affiliates.application.dto.AffiliateRequest;
import com.almedin.modules.affiliates.application.dto.AffiliateResponse;
import com.almedin.modules.affiliates.domain.model.Affiliate;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface AffiliateMapper {

    Affiliate toEntity(AffiliateRequest request);

    AffiliateResponse toResponse(Affiliate affiliate);

    List<AffiliateResponse> toResponseList(List<Affiliate> affiliates);

    void updateEntityFromRequest(AffiliateRequest request, @MappingTarget Affiliate affiliate);
}