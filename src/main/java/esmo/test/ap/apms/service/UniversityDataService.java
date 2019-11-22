/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esmo.test.ap.apms.service;

import esmo.test.ap.apms.model.pojo.UniversityData;
import java.util.Optional;

/**
 *
 * @author nikos
 */
public interface UniversityDataService {
    
    
    public Optional<UniversityData[]> getCodes();
    
}
