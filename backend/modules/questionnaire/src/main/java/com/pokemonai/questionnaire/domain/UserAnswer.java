package com.pokemonai.questionnaire.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_answers")
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "answer_value", nullable = false)
    private short answerValue;

    @Column(name = "answered_at", nullable = false)
    private OffsetDateTime answeredAt = OffsetDateTime.now();

    protected UserAnswer() {}

    public UserAnswer(UUID userId, Question question, int answerValue) {
        this.userId = userId;
        this.question = question;
        this.answerValue = (short) answerValue;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Question getQuestion() { return question; }
    public int getAnswerValue() { return answerValue; }
    public OffsetDateTime getAnsweredAt() { return answeredAt; }

    public void updateValue(int answerValue) {
        this.answerValue = (short) answerValue;
        this.answeredAt = OffsetDateTime.now();
    }
}
