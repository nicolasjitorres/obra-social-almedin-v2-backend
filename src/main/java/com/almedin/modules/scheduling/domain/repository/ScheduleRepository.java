package com.almedin.modules.scheduling.domain.repository;

import com.almedin.modules.scheduling.domain.model.Schedule;
import com.almedin.modules.shared.domain.enums.DayOfWeek;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository {

    List<Schedule> listAll();
    Optional<Schedule> findById(Long id);
    List<Schedule> findBySpecialistId(Long specialistId);
    List<Schedule> findBySpecialistIdAndDayOfWeek(Long specialistId, DayOfWeek dayOfWeek);
    void persist(Schedule schedule);
    void delete(Schedule schedule);
}