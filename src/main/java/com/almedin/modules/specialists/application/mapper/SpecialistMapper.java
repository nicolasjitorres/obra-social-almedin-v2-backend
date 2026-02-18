package com.almedin.modules.specialists.application.mapper;

import com.almedin.modules.specialists.application.dto.SpecialistRequest;
import com.almedin.modules.specialists.application.dto.SpecialistResponse;
import com.almedin.modules.specialists.domain.model.Specialist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface SpecialistMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "password", ignore = true)
    Specialist toEntity(SpecialistRequest request);

    SpecialistResponse toResponse(Specialist specialist);

    List<SpecialistResponse> toResponseList(List<Specialist> specialists);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntityFromRequest(SpecialistRequest request, @MappingTarget Specialist specialist);
}