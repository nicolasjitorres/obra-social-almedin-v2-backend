package com.almedin.modules.affiliates.application.mapper;

import com.almedin.modules.affiliates.application.dto.AffiliateRequest;
import com.almedin.modules.affiliates.application.dto.AffiliateResponse;
import com.almedin.modules.affiliates.application.dto.UpdateAffiliateProfileRequest;
import com.almedin.modules.affiliates.domain.model.Affiliate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface AffiliateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "password", ignore = true)
    Affiliate toEntity(AffiliateRequest request);

    AffiliateResponse toResponse(Affiliate affiliate);

    List<AffiliateResponse> toResponseList(List<Affiliate> affiliates);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntityFromRequest(AffiliateRequest request, @MappingTarget Affiliate affiliate);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "dni", ignore = true)
    @Mapping(target = "healthInsuranceCode", ignore = true)
    void updateSelfEntityFromRequest(UpdateAffiliateProfileRequest request, @MappingTarget Affiliate affiliate);
}