/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esmo.test.ap.apms.service;



import esmo.test.ap.apms.model.pojo.MSConfigurationResponse.MicroService;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

/**
 *
 * @author nikos
 */
public interface MSConfigurationService {

    public MicroService[] getConfigurationJSON();

    public Optional<String> getMsIDfromRSAFingerprint(String rsaFingerPrint) throws IOException;

    public Optional<PublicKey> getPublicKeyFromFingerPrint(String rsaFingerPrint) throws InvalidKeyException, IOException, NoSuchAlgorithmException, InvalidKeySpecException;

    public String getMsEndpointByIdAndApiCall(String msId, String apiCall);

}
