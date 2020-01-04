package com.fri.code.exercises.lib;

import java.util.List;

public class ExerciseMetadata {
    private Integer exerciseID;
    private String content;
    private String description;
    private Integer subjectID;
    private Boolean isFinished;

    private List<InputMetadata> inputs;

    public List<InputMetadata> getInputs() {
        return inputs;
    }

    public void setInputs(List<InputMetadata> inputs) {
        this.inputs = inputs;
    }

    public Integer getExerciseID() {
        return exerciseID;
    }

    public void setExerciseID(Integer exerciseID) {
        this.exerciseID = exerciseID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(Integer subjectID) {
        this.subjectID = subjectID;
    }

    public Boolean getFinished() {
        return isFinished;
    }

    public void setFinished(Boolean finished) {
        isFinished = finished;
    }
}
