/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esmo.test.ap.apms;

import esmo.test.ap.apms.service.KeyStoreService;
import esmo.test.ap.apms.service.ParameterService;
import esmo.test.ap.apms.service.impl.KeyStoreServiceImpl;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 *
 * @author nikos
 */
@Profile("test")
@Configuration
public class TestConfig {

    private ParameterService paramServ;
    private KeyStoreService keyServ;

    @Bean
    @Primary
    public ParameterService paramServ() {
        return Mockito.mock(ParameterService.class);
    }

    @Bean
    @Primary
    public KeyStoreService keyStoreService() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        ClassLoader classLoader = getClass().getClassLoader();
        String path = classLoader.getResource("testKeys/keystore.jks").getPath();
        Mockito.when(paramServ().getParam("KEYSTORE_PATH")).thenReturn(path);
        Mockito.when(paramServ().getParam("KEY_PASS")).thenReturn("selfsignedpass");
        Mockito.when(paramServ().getParam("STORE_PASS")).thenReturn("keystorepass");
        Mockito.when(paramServ().getParam("JWT_CERT_ALIAS")).thenReturn("selfsigned");
        Mockito.when(paramServ().getParam("HTTPSIG_CERT_ALIAS")).thenReturn("1");
        Mockito.when(paramServ().getParam("ASYNC_SIGNATURE")).thenReturn("true");
        Mockito.when(paramServ().getParam("SESSION_MANAGER_URL")).thenReturn("http://dss1.aegean.gr:8090");
        Mockito.when(paramServ().getParam("EIDAS_PROPERTIES")).thenReturn("FamilyName,FirstName");
        Mockito.when(paramServ().getParam("ESMO_SUPPORTED_SIG_ALGORITHMS")).thenReturn("RSA");
        Mockito.when(paramServ().getParam("ESMO_SUPPORTED_ENC_ALGORITHMS")).thenReturn("RSA");

//        Mockito.when(paramServ().getParam("CONFIGURATION_MANAGER_URL")).thenReturn("http://5.79.83.118:8080");
        Mockito.when(paramServ().getParam("CONFIGURATION_MANAGER_URL")).thenReturn("http://dss1.aegean.gr:8080");

        Mockito.when(paramServ().getParam("SM_URL")).thenReturn("http://dss1.aegean.gr:8090");
        //ACM_NAME
        Mockito.when(paramServ().getParam("ACM_NAME")).thenReturn("ACMms001");
        Mockito.when(paramServ().getParam("ACM_URL")).thenReturn("http://dss1.aegean.gr:8070");
        Mockito.when(paramServ().getParam("AP_MS_NAME")).thenReturn("APms001");
        Mockito.when(paramServ().getParam("REDIRECT_JWT_SENDER")).thenReturn("APms001");
        Mockito.when(paramServ().getParam("REDIRECT_JWT_RECEIVER")).thenReturn("ACMms001");
        //paramServ.getParam("SUPPORTED_CLAIMS") Env.UNIVERSITIES_CONFIG_PATH=/home/nikos/NetBeansProjects/ESMO-AP-TEST/src/test/resources/testKeys/university_codes.json
        Mockito.when(paramServ().getParam("SUPPORTED_CLAIMS")).thenReturn("eduPersonAffiliation,primaryAffiliation,schacHomeOrganization,mail,schacExpiryDate,mobile,eduPersonPrincipalName,eduPersonPrincipalNamePrior,displayName,sn,givenName");
        Mockito.when(paramServ().getParam("UNIVERSITIES_CONFIG_PATH")).thenReturn("home/nikos/NetBeansProjects/ESMO-AP-TEST/src/test/resources/testKeys/university_codes.json");

        return new KeyStoreServiceImpl(paramServ());
    }
}
