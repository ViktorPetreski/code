package com.fri.code.exercises.models.converters;

import com.fri.code.exercises.lib.ExerciseMetadata;
import com.fri.code.exercises.models.entities.ExerciseMetadataEntity;

public class ExerciseMetadataConverter {
    public static ExerciseMetadata toDto(ExerciseMetadataEntity entity) {
        ExerciseMetadata dto = new ExerciseMetadata();
        dto.setContent(entity.getContent());
        dto.setDescription(entity.getDescription());
        dto.setExerciseID(entity.getId());
        dto.setSubjectID(entity.getSubjectID());
        return dto;
    }

    public static ExerciseMetadataEntity toEntity(ExerciseMetadata exerciseMetadata) {
        ExerciseMetadataEntity entity = new ExerciseMetadataEntity();
        entity.setContent(exerciseMetadata.getContent());
        entity.setDescription(exerciseMetadata.getDescription());
        entity.setId(exerciseMetadata.getExerciseID());
        entity.setSubjectID(exerciseMetadata.getSubjectID());
        return entity;
    }
}
