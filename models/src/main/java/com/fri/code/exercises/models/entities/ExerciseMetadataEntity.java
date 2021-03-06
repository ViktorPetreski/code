package com.fri.code.exercises.models.entities;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "exercise_metadata")
@NamedQueries(
        value = {
                @NamedQuery(name = "ExerciseMetadataEntity.getAll", query = "SELECT ex FROM ExerciseMetadataEntity ex"),
                @NamedQuery(name = "ExerciseMetadataEntity.getExercisesForSubject", query = "SELECT ex FROM ExerciseMetadataEntity ex WHERE ex.subjectID = ?1")
        }
)
public class ExerciseMetadataEntity {

//    TODO: Connect subject with exercise
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "content")
    private String content;

    @Column(name = "description")
    private String description;

    @Column(name = "subjectID")
    private Integer subjectID;

    @ElementCollection
    private List<String> inputs;

    @Column(name = "isFinished")
    private Boolean isFinished;

    public List<String> getInputs() {
        return inputs;
    }

    public void setInputs(List<String> inputs) {
        this.inputs = inputs;
    }

    public Integer getSubjectID() {

        return subjectID;
    }

    public void setSubjectID(Integer subjectID) {
        this.subjectID = subjectID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Boolean getFinished() {
        return isFinished;
    }

    public void setFinished(Boolean finished) {
        isFinished = finished;
    }
}
