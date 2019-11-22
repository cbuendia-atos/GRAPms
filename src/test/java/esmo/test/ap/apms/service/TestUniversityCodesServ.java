/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esmo.test.ap.apms.service;

import esmo.test.ap.apms.model.pojo.UniversityData;
import esmo.test.ap.apms.service.impl.UniversityDataServiceImpl;
import java.util.Optional;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author nikos
 */
@RunWith(SpringRunner.class)
public class TestUniversityCodesServ {

    @MockBean
    private ParameterService paramServ;

    @Test
    public void test() {
        System.out.println("will test");
        Mockito.when(this.paramServ.getParam("UNIVERSITIES_CONFIG_PATH")).thenReturn("/home/nikos/NetBeansProjects/ESMO-AP-TEST/src/test/resources/testKeys/university_codes.json");
        UniversityDataService univServ = new UniversityDataServiceImpl(this.paramServ);
        Optional<UniversityData[]> rest = univServ.getCodes();

        assertEquals(rest.isPresent(), true);
        assertEquals(rest.get()[0].getName(),"AGRICULTURAL UNIVERSITY OF ATHENS");

    }

}
