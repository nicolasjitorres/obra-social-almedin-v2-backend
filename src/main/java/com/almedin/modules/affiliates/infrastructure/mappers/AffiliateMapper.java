package com.almedin.modules.affiliates.infrastructure.mappers;

import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.affiliates.infrastructure.web.dto.AffiliateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "jakarta")
public interface AffiliateMapper {
    AffiliateDTO toDTO(Affiliate affiliate);
    Affiliate toEntity(AffiliateDTO dto);
    List<AffiliateDTO> toDTOList(List<Affiliate> affiliates);
    void updateEntityFromDTO(AffiliateDTO dto, @MappingTarget Affiliate entity);
}