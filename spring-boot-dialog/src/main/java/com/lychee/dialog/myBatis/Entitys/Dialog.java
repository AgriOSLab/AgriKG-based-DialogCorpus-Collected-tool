package com.lychee.dialog.myBatis.Entitys;

public class Dialog {
    private int record_id;
    private int turn_id;
    private String question;
    private String question_entity;
    private String answer;
    private String answer_entities;

    public String getQuestion_entity() {
        return question_entity;
    }

    public void setQuestion_entity(String question_entity) {
        this.question_entity = question_entity;
    }

    public String getAnswer_entities() {
        return answer_entities;
    }

    public void setAnswer_entities(String answer_entities) {
        this.answer_entities = answer_entities;
    }

    private String intention;
    private String triples;
    private String labels;
    private String submitted_person;

    public int getRecord_id() {
        return record_id;
    }

    public void setRecord_id(int record_id) {
        this.record_id = record_id;
    }

    public int getTurn_id() {
        return turn_id;
    }

    public void setTurn_id(int turn_id) {
        this.turn_id = turn_id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getIntention() {
        return intention;
    }

    public void setIntention(String intention) {
        this.intention = intention;
    }

    public String getTriples() {
        return triples;
    }

    public void setTriples(String triples) {
        this.triples = triples;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getSubmitted_person() {
        return submitted_person;
    }

    public void setSubmitted_person(String submitted_person) {
        this.submitted_person = submitted_person;
    }
}
