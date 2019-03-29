/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esmo.test.ap.apms.model.pojo;

import javax.validation.constraints.NotBlank;

/**
 *
 * @author nikos
 */
public class Amka {

    @NotBlank(message="AMKA must not be empty")
    public String amkaNumber;
    public String academicId;
    public String sessionId;

    public Amka() {
    }

    public Amka(String amka, String academicId, String sessionId
    ) {
        this.amkaNumber = amka;
        this.academicId = academicId;
        this.sessionId = sessionId;
    }

    public String getAmkaNumber() {
        return amkaNumber;
    }

    public void setAmkaNumber(String amkaNumber) {
        this.amkaNumber = amkaNumber;
    }

    public String getAcademicId() {
        return academicId;
    }

    public void setAcademicId(String academicId) {
        this.academicId = academicId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

}
