package app.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.JsonObject;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@JsonIgnoreProperties(value = "new")
public class Quiz extends AbstractPersistable<Long> {
    @NotNull
    private String title;
    
    @Transient
    @Column(length = 4000)
    @Lob
    private String items;
    
    @JsonIgnore
    @OneToMany(mappedBy = "quiz")
    private List<QuizAnswer> quizAnswers;
    
    @NotNull
    private boolean reviewable;
    
    @Transient
    private boolean answered;
    
    private String placeholderAnswers;
    
    private Boolean isOpen;
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    @JsonProperty("items")
    public String getItemsJSON() {
        return unescapeJava(items);
    }
    
    public String getItems() {
        return items;
    }
    
    @JsonProperty("items")
    public void setItems(String items) {
        this.items = items;
    }
    
    public List<QuizAnswer> getQuizAnswers() {
        return quizAnswers;
    }
    
    public void setQuizAnswers(List<QuizAnswer> quizAnswers) {
        this.quizAnswers = quizAnswers;
    }

    public boolean isReviewable() {
        return reviewable;
    }

    public void setReviewable(boolean reviewable) {
        this.reviewable = reviewable;
    }

    @JsonProperty(value = "answered")
    public boolean isAnswered() {
        return answered;
    }

    @JsonIgnore
    public void setAnswered(boolean answered) {
        this.answered = answered;
    }
    
    public void setPlaceholderAnswers(String placeholderAnswers) {
        this.placeholderAnswers = placeholderAnswers;
    }
    
    public String getPlaceholderAnswers() {
        return placeholderAnswers;
    }
    
    public void setIsOpen(Boolean isOpen) {
        this.isOpen = isOpen;
    }
    
    public Boolean getIsOpen() {
        return isOpen;
    }
}