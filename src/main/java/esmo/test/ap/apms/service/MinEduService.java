/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esmo.test.ap.apms.service;

import esmo.test.ap.apms.model.pojo.MinEduResponse;
import esmo.test.ap.apms.model.pojo.MinEduResponse.InspectionResult;
import java.util.Optional;
import org.springframework.web.client.HttpClientErrorException;

/**
 *
 * @author nikos
 */
public interface MinEduService {
    
    public Optional<String> getAccessToken() throws HttpClientErrorException;
    
    public Optional<InspectionResult> getInspectioResultByAcademicId(String academicId, String selectedUniversityId, String esmoSessionId) throws HttpClientErrorException;
    
    public Optional<String> getAcademicIdFromAMKA(String amkaNumber, String selectedUniversityId,String esmoSessionId) throws HttpClientErrorException;
    
    public Optional<MinEduResponse> getInspectioResponseByAcademicId(String academicId, String selectedUniversityId,String esmoSessionId) throws HttpClientErrorException;
    
    
}
