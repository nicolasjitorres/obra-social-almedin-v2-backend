package com.almedin.modules.scheduling.application.mapper;

import com.almedin.modules.scheduling.application.dto.*;
import com.almedin.modules.scheduling.domain.model.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface SchedulingMapper {

    // Schedule
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "specialist", ignore = true)
    Schedule toScheduleEntity(ScheduleRequest request);

    @Mapping(target = "specialistId", source = "specialist.id")
    @Mapping(target = "specialistName", expression = "java(schedule.getSpecialist().getFirstName() + ' ' + schedule.getSpecialist().getLastName())")
    ScheduleResponse toScheduleResponse(Schedule schedule);

    List<ScheduleResponse> toScheduleResponseList(List<Schedule> schedules);

    // Unavailability
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "specialist", ignore = true)
    SpecialistUnavailability toUnavailabilityEntity(UnavailabilityRequest request);

    @Mapping(target = "specialistId", source = "specialist.id")
    @Mapping(target = "specialistName", expression = "java(unavailability.getSpecialist().getFirstName() + ' ' + unavailability.getSpecialist().getLastName())")
    UnavailabilityResponse toUnavailabilityResponse(SpecialistUnavailability unavailability);

    List<UnavailabilityResponse> toUnavailabilityResponseList(List<SpecialistUnavailability> unavailabilities);

    // Appointment
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "affiliate", ignore = true)
    @Mapping(target = "specialist", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "cancelledBy", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "clinicalNotes", ignore = true)
    @Mapping(target = "prescription", ignore = true)
    @Mapping(target = "penaltyApplied", ignore = true)
    @Mapping(target = "reminderSent", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "parentAppointment", ignore = true)
    Appointment toAppointmentEntity(AppointmentRequest request);

    @Mapping(target = "affiliateId", source = "affiliate.id")
    @Mapping(target = "affiliateName", expression = "java(appointment.getAffiliate().getFirstName() + ' ' + appointment.getAffiliate().getLastName())")
    @Mapping(target = "specialistId", source = "specialist.id")
    @Mapping(target = "specialistName", expression = "java(appointment.getSpecialist().getFirstName() + ' ' + appointment.getSpecialist().getLastName())")
    @Mapping(target = "parentAppointmentId", source = "parentAppointment.id")
    AppointmentResponse toAppointmentResponse(Appointment appointment);

    List<AppointmentResponse> toAppointmentResponseList(List<Appointment> appointments);
}