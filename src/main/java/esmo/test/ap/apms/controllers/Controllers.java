/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esmo.test.ap.apms.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esmo.test.ap.apms.MemCacheConfig;
import esmo.test.ap.apms.model.exceptions.ReCaptchaInvalidException;
import esmo.test.ap.apms.model.factories.EsmoResponseFactory;
import esmo.test.ap.apms.model.pojo.AmkaForm;
import esmo.test.ap.apms.model.pojo.AttributeSet;
import esmo.test.ap.apms.model.pojo.AttributeType;
import esmo.test.ap.apms.model.pojo.EntityMetadata;
import esmo.test.ap.apms.model.pojo.MinEduResponse;
import esmo.test.ap.apms.model.pojo.MinEduResponse.InspectionResult;
import esmo.test.ap.apms.model.pojo.SessionMngrResponse;
import esmo.test.ap.apms.model.pojo.UpdateDataRequest;
import esmo.test.ap.apms.service.EsmoMetadataService;
import esmo.test.ap.apms.service.HttpSignatureService;
import esmo.test.ap.apms.service.ICaptchaService;
import esmo.test.ap.apms.service.KeyStoreService;
import esmo.test.ap.apms.service.MinEduService;
import esmo.test.ap.apms.service.NetworkService;
import esmo.test.ap.apms.service.ParameterService;
import esmo.test.ap.apms.service.UniversityDataService;
import esmo.test.ap.apms.service.impl.HttpSignatureServiceImpl;
import esmo.test.ap.apms.service.impl.NetworkServiceImpl;
import esmo.test.ap.apms.utils.DateParsingUtils;
import esmo.test.ap.apms.utils.StringDistance;
import esmo.test.ap.apms.utils.TranslitarateUtils;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author nikos
 */
@Controller
public class Controllers {

    private final NetworkService netServ;
    private final ParameterService paramServ;
    private final EsmoMetadataService metadataServ;
    private final KeyStoreService keyServ;

    private final static Logger LOG = LoggerFactory.getLogger(Controllers.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ICaptchaService captchaService;

    private MinEduService minEduServ;

    private UniversityDataService univServ;

    @Autowired
    public Controllers(ParameterService paramServ, KeyStoreService keyServ,
            EsmoMetadataService metadataServ, MinEduService minEduServ,
            UniversityDataService univServ) throws InvalidKeySpecException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        this.paramServ = paramServ;
        this.metadataServ = metadataServ;
        this.keyServ = keyServ;
        Key signingKey = this.keyServ.getSigningKey();
        String fingerPrint = this.keyServ.getFingerPrint();//"7a9ba747ab5ac50e640a07d90611ce612b7bde775457f2e57b804517a87c813b";
        HttpSignatureService httpSigServ = new HttpSignatureServiceImpl(fingerPrint, signingKey);
        this.netServ = new NetworkServiceImpl(httpSigServ);
        this.minEduServ = minEduServ;
        this.univServ = univServ;
    }

    @RequestMapping(value = "/ap/query", method = {RequestMethod.POST, RequestMethod.GET})
    public String queryAp(@RequestParam(value = "msToken", required = true) String msToken, Model model, RedirectAttributes redirectAttrs) throws KeyStoreException {
        String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");

        Cache memCache = this.cacheManager.getCache(MemCacheConfig.AP_SESSION);

        List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
        requestParams.add(new NameValuePair("token", msToken));
        ObjectMapper mapper = new ObjectMapper();
        try {
            SessionMngrResponse resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/validateToken", requestParams, 1), SessionMngrResponse.class);
            if (resp.getCode().toString().equals("OK") && StringUtils.isEmpty(resp.getError())) {
                String esmoSessionId = resp.getSessionData().getSessionId();
                String apMsSessionId = UUID.randomUUID().toString();
                redirectAttrs.addFlashAttribute("sessionId", esmoSessionId);

                //calls SM, “/sm/getSessionData” to get the session object that must contain the variables apRequest, apMetadata
                requestParams.clear();
                requestParams.add(new NameValuePair("sessionId", esmoSessionId));
                resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/getSessionData", requestParams, 1), SessionMngrResponse.class);

                String apRequest = (String) resp.getSessionData().getSessionVariables().get("apRequest");

                if (apRequest == null) {
                    LOG.error("no apRequest found in session " + esmoSessionId);
                    model.addAttribute("error", "No AP request attributes found in the Session! Please restart the process");
                    redirectAttrs.addFlashAttribute("errorMsg", "No AP request attributes found in the Session! Please restart the process");
                    return "redirect:/grap/authfail";
                } else {

                    EntityMetadata apMsMetadata = metadataServ.getMetadata();

                    AttributeSet parsedApRequest = mapper.readValue(apRequest, AttributeSet.class);
                    List<AttributeType> matchingRequestedAttributes = Arrays.stream(parsedApRequest.getAttributes()).filter(attribute -> {
                        return Arrays.asList(apMsMetadata.getClaims()).contains(attribute.getFriendlyName());
                    }).collect(Collectors.toList());

                    if (matchingRequestedAttributes.size() > 0) {
                        memCache.put(apMsSessionId, mapper.writeValueAsString(resp)); //save the whole ESMO session in cache
                        AmkaForm amkaForm = new AmkaForm();
                        amkaForm.setSessionId(apMsSessionId);
                        model.addAttribute("amka", amkaForm);
                        model.addAttribute("universities", this.univServ.getCodes().get());
                        return "amka";
                    }
                    LOG.error("Error, no supported attributes were found in the request");
                    Arrays.stream(parsedApRequest.getAttributes()).forEach(attr -> {
                        LOG.error(attr.getFriendlyName());
                    });
                    redirectAttrs.addFlashAttribute("errorMsg", "no supported attributes were found in the request");

                }
            } else {
                model.addAttribute("error", "Error validating token! " + resp.getError());
                LOG.error("something wring with the SM session!");
                LOG.error(resp.getError());
                LOG.error(resp.getCode().toString());
                redirectAttrs.addFlashAttribute("errorMsg", "Error validating token! " + resp.getError());
            }

        } catch (IOException ex) {
            LOG.info(ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            LOG.info(ex.getMessage());
        }
        return "redirect:/grap/authfail";
    }

    @RequestMapping(value = "/ap/forward", method = {RequestMethod.POST})
    public String forwardToACM(@ModelAttribute("amka") @Valid AmkaForm amkaForm, BindingResult bindingResult,
            Model model, HttpServletRequest request, RedirectAttributes redirectAttrs) throws KeyStoreException, IOException, NoSuchAlgorithmException {

        String acmName = paramServ.getParam("ACM_NAME");
        String apMsName = paramServ.getParam("AP_MS_NAME");
        List<NameValuePair> requestParams = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        Cache memCache = this.cacheManager.getCache(MemCacheConfig.AP_SESSION);
        String apmsSession = (String) memCache.get(amkaForm.getSessionId()).get();
        SessionMngrResponse resp = mapper.readValue(apmsSession, SessionMngrResponse.class);
        String esmoSessionId = resp.getSessionData().getSessionId();
        redirectAttrs.addFlashAttribute("sessionId", amkaForm.getSessionId());

        // get matching attributes
        EntityMetadata apMsMetadata = metadataServ.getMetadata();
        String apRequest = (String) resp.getSessionData().getSessionVariables().get("apRequest");
        AttributeSet parsedApRequest = mapper.readValue(apRequest, AttributeSet.class);
        List<AttributeType> matchingRequestedAttributes = Arrays.stream(parsedApRequest.getAttributes()).filter(attribute -> {
            return Arrays.asList(apMsMetadata.getClaims()).contains(attribute.getFriendlyName());
        }).collect(Collectors.toList());

        //debug
        matchingRequestedAttributes.stream().forEach(att -> {
            LOG.info("Matching attributes are" + att.getFriendlyName() + att.getValues()[0]);
        });

        String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
        String authenticationSet = (String) resp.getSessionData().getSessionVariables().get("authenticationSet");
//        String userUniversitySelectionId = (String) resp.getSessionData().getSessionVariables().get("selectedUniversityId");
        String userUniversitySelectionId = amkaForm.code;
        // ""{"id":"id","type":"Response","issuer":"issuer","recipient":"recipient","attributes":[{"name":"http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName","friendlyName":"FamilyName","encoding":"UTF-8","language":"N/A","isMandatory":true,"values":["cph8"]},{"name":"http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName","friendlyName":"FirstName","encoding":"UTF-8","language":"N/A","isMandatory":true,"values":["cph8"]},{"name":"http://eidas.europa.eu/attributes/naturalperson/DateOfBirth","friendlyName":"DateOfBirth","encoding":"UTF-8","language":"N/A","isMandatory":true,"values":["1966-01-01"]},{"name":"http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier","friendlyName":"PersonIdentifier","encoding":"UTF-8","language":"N/A","isMandatory":true,"values":["CA/CA/Cph123456"]}],"properties":{"NameID":"CA/CA/Cph123456","levelOfAssurance":"http://eidas.europa.eu/LoA/low"},"inResponseTo":null,"loa":"low","notBefore":null,"notAfter":null,"status":{"code":"OK","subcode":null,"message":null}}""

        //check form validity
        if (bindingResult.hasErrors()) {
            model.addAttribute("universities", this.univServ.getCodes().get());
            return "amka";
        }

        // check existence of eIDAS attributes
        AttributeSet eIDASAttributes = mapper.readValue(authenticationSet, AttributeSet.class);
        AttributeType dateOfBirth = new AttributeType();
        AttributeType eidasGivenName = new AttributeType();
        AttributeType eidasFamilyName = new AttributeType();
        AttributeType personalIdentifier = new AttributeType();
        Arrays.asList(eIDASAttributes.getAttributes()).stream().forEach(aType -> {
            if (aType.getFriendlyName().equals("DateOfBirth")) {
                dateOfBirth.setValues(aType.getValues());
            }
            if (aType.getFriendlyName().equals("FamilyName")) {
                eidasFamilyName.setValues(aType.getValues());
            }
            if (aType.getFriendlyName().equals("FirstName")) {
                eidasGivenName.setValues(aType.getValues());
            }
            if (aType.getFriendlyName().equals("PersonIdentifier")) {
                personalIdentifier.setValues(aType.getValues());
            }
        });

        //for testing captcha validity is skiped for test user
        if (!(personalIdentifier.getValues()[0].contains("ERMIS-11076669")
                || personalIdentifier.getValues()[0].contains("36f6b7d3-d25a-42f5-b591-7e488ec140d5")
                || personalIdentifier.getValues()[0].contains("05068907693")
                || personalIdentifier.getValues()[0].contains("99999142H")) //
                || personalIdentifier.getValues()[0].contains("60001019906")
                || eidasGivenName.getValues()[0].toLowerCase().contains("Triin")
                || eidasFamilyName.getValues()[0].toLowerCase().contains("Puusaar")
                || eidasGivenName.getValues()[0].contains("Mario Ferdinando")
                || eidasFamilyName.getValues()[0].contains("Faiella")) {
            //check captcha validity
            try {
                String capResponse = request.getParameter("g-recaptcha-response");
                this.captchaService.processResponse(capResponse);
            } catch (ReCaptchaInvalidException e) {
                LOG.error(e.getMessage());
                return "amka";
            }
        }

        // get the ap response
        String attributSetString = null;

        //fake users
        if ((eidasGivenName.getValues()[0].equals("ΑΝΔΡΕΑΣ, ANDREAS") && eidasFamilyName.getValues()[0].equals("ΠΕΤΡΟΥ, PETROU"))
                || personalIdentifier.getValues()[0].contains("ERMIS-11076669")
                || personalIdentifier.getValues()[0].contains("36f6b7d3-d25a-42f5-b591-7e488ec140d5")
                || personalIdentifier.getValues()[0].contains("05068907693")
                || personalIdentifier.getValues()[0].contains("99999142H")
                || personalIdentifier.getValues()[0].contains("60001019906")
                || personalIdentifier.getValues()[0].contains("IT/NO/SIEL19H38BL5VS")
                || eidasGivenName.getValues()[0].toLowerCase().contains("Triin")
                || eidasFamilyName.getValues()[0].toLowerCase().contains("Puusaar")
                || eidasGivenName.getValues()[0].toLowerCase().contains("Mario Ferdinando")
                || eidasFamilyName.getValues()[0].toLowerCase().contains("Faiella")
                || !StringUtils.isEmpty(this.paramServ.getParam("TESTING"))) {

            if (personalIdentifier.getValues()[0].contains("IT/NO/SIEL19H38BL5VS")) {
                LOG.info("Found test Italian NO user" + eidasGivenName.getValues()[0] + " " + eidasFamilyName.getValues()[0] + " " + personalIdentifier.getValues()[0]);
                AttributeSet result = EsmoResponseFactory.buildFakeItalianResponse("issuer", "recipient", esmoSessionId, matchingRequestedAttributes);
                attributSetString = mapper.writeValueAsString(result);
            } else {
                AttributeSet result = EsmoResponseFactory.buildFakeResponse("issuer", "recipient", esmoSessionId, matchingRequestedAttributes);
                attributSetString = mapper.writeValueAsString(result);
                LOG.info("Found test user " + eidasGivenName.getValues()[0] + " " + eidasFamilyName.getValues()[0] + " " + personalIdentifier.getValues()[0]);
            }

        } else {
            if ((dateOfBirth.getValues() == null || eidasGivenName.getValues() == null || eidasFamilyName.getValues() == null)
                    && !personalIdentifier.getValues()[0].contains("ES")) {
                LOG.error("Missing mandatory eIDAS attributes");
                AttributeSet result = EsmoResponseFactory.buildErrorResponse(apMsName, acmName, resp.getSessionData().getSessionId());
                attributSetString = mapper.writeValueAsString(result);
            } else {
                //fetch AP response from minEdu API
                try {

                    String studentAcademicId = amkaForm.getAcademicId();
                    if (dateOfBirth.getValues() != null && dateOfBirth.getValues()[0] != null) {
                        // check name and date of birth from amka!
                        Date amkaDate = DateParsingUtils.parseAmkaDate(amkaForm.amkaNumber);
                        Date eidasDate = DateParsingUtils.parseEidasDate(dateOfBirth.getValues()[0]);
                        if (amkaDate.compareTo(eidasDate) == 0) {  //compare the dates, amka and eidas
                            if (StringUtils.isEmpty(studentAcademicId)) { // if user didn't present their academic id
                                //get academicId from amka
                                Optional<String> academicId = minEduServ.getAcademicIdFromAMKA(amkaForm.getAmkaNumber(), userUniversitySelectionId, esmoSessionId);
                                if (!academicId.isPresent()) {
                                    LOG.error("ERROR: No matching AcademicId found for amka " + amkaForm.getAmkaNumber());
                                    redirectAttrs.addFlashAttribute("errorMsg", "No matching AcademicId found");
                                    return "redirect:/authfail";
                                }
                                studentAcademicId = academicId.get();
                            }
                            //get academic attributes from minEdu
                            Optional<MinEduResponse> minEduResp = minEduServ.getInspectioResponseByAcademicId(studentAcademicId, userUniversitySelectionId, esmoSessionId);
                            if (minEduResp.isPresent()) {
                                InspectionResult inspResult = minEduResp.get().getResult().getInspectionResult();
                                //check minedu name and eIDAS names
                                if (StringDistance.areSimilar(TranslitarateUtils.getLatinFromMixed(eidasGivenName.getValues()[0]), inspResult.getLatinFirstName())
                                        && StringDistance.areSimilar(TranslitarateUtils.getLatinFromMixed(eidasFamilyName.getValues()[0]), inspResult.getLatinLastName())) {
//                            MinEduResponse response = new MinEduResponse("success", inspResult.get());

                                    MinEduResponse response = new MinEduResponse(minEduResp.get().getResult(), minEduResp.get().getServiceCallID(), minEduResp.get().getCode(),
                                            minEduResp.get().isSuccess(), minEduResp.get().getTimestamp());

                                    AttributeSet result = EsmoResponseFactory.buildFromMinEduResponse(response, apMsName, acmName, resp.getSessionData().getSessionId(), matchingRequestedAttributes);
                                    attributSetString = mapper.writeValueAsString(result);
                                } else {
                                    LOG.error("eidas name and amka names do not match!!");
                                    LOG.error(" eidasName " + eidasGivenName.getValues()[0] + " -- " + inspResult.getLatinFirstName());
                                    LOG.error(" eidasFamilyName " + eidasFamilyName.getValues()[0] + " --  " + inspResult.getLatinLastName());
                                    AttributeSet result = EsmoResponseFactory.buildErrorResponse(apMsName, acmName, resp.getSessionData().getSessionId());
                                    attributSetString = mapper.writeValueAsString(result);
                                }
                            } else {
                                LOG.error("ERROR: getting academic info for " + studentAcademicId);
                                redirectAttrs.addFlashAttribute("errorMsg", "ERROR: getting academic info for " + studentAcademicId);
                                return "redirect:/grap/authfail";
                            }
                        } else {
                            LOG.error("ERROR: mismatch between amka date and eIDAS date");
                            redirectAttrs.addFlashAttribute("errorMsg", "Academic Attributes do not match eIDAS attributes!");
                            return "redirect:/grap/authfail";
                        }

                    } else {
                        if (StringUtils.isEmpty(studentAcademicId)) { // if user didn't present their academic id
                            //get academicId from amka
                            Optional<String> academicId = minEduServ.getAcademicIdFromAMKA(amkaForm.getAmkaNumber(), userUniversitySelectionId, esmoSessionId);
                            if (!academicId.isPresent()) {
                                LOG.error("ERROR: No matching AcademicId found for amka " + amkaForm.getAmkaNumber());
                                redirectAttrs.addFlashAttribute("errorMsg", "No matching AcademicId found");
                                return "redirect:/grap/authfail";
                            }
                            studentAcademicId = academicId.get();
                        }
                        //get academic attributes from minEdu
                        Optional<MinEduResponse> minEduResp = minEduServ.getInspectioResponseByAcademicId(studentAcademicId, userUniversitySelectionId, esmoSessionId);
                        if (minEduResp.isPresent()) {
                            InspectionResult inspResult = minEduResp.get().getResult().getInspectionResult();
                            //check minedu name and eIDAS names
                            if (StringDistance.areSimilar(TranslitarateUtils.getLatinFromMixed(eidasGivenName.getValues()[0]), inspResult.getLatinFirstName())
                                    && StringDistance.areSimilar(TranslitarateUtils.getLatinFromMixed(eidasFamilyName.getValues()[0]), inspResult.getLatinLastName())) {
//                            MinEduResponse response = new MinEduResponse("success", inspResult.get());

                                MinEduResponse response = new MinEduResponse(minEduResp.get().getResult(), minEduResp.get().getServiceCallID(), minEduResp.get().getCode(),
                                        minEduResp.get().isSuccess(), minEduResp.get().getTimestamp());

                                AttributeSet result = EsmoResponseFactory.buildFromMinEduResponse(response, apMsName, acmName, resp.getSessionData().getSessionId(), matchingRequestedAttributes);
                                attributSetString = mapper.writeValueAsString(result);
                            } else {
                                LOG.error("eidas name and amka names do not match!!");
                                LOG.error(" eidasName " + eidasGivenName.getValues()[0] + " -- " + inspResult.getLatinFirstName());
                                LOG.error(" eidasFamilyName " + eidasFamilyName.getValues()[0] + " --  " + inspResult.getLatinLastName());
                                AttributeSet result = EsmoResponseFactory.buildErrorResponse(apMsName, acmName, resp.getSessionData().getSessionId());
                                attributSetString = mapper.writeValueAsString(result);
                            }
                        } else {
                            LOG.error("ERROR: getting academic info for " + studentAcademicId);
                            redirectAttrs.addFlashAttribute("errorMsg", "ERROR: getting academic info for " + studentAcademicId);
                            return "redirect:/grap/authfail";
                        }

                    }

                } catch (ParseException e) {
                    LOG.error("could not parse date " + amkaForm.amkaNumber + " " + dateOfBirth.getValues()[0]);
                    LOG.error(e.getMessage());
                    AttributeSet result = EsmoResponseFactory.buildErrorResponse(apMsName, acmName, resp.getSessionData().getSessionId());
                    attributSetString = mapper.writeValueAsString(result);
                }
            }
        }

        requestParams.clear();
        requestParams.add(new NameValuePair("dataObject", attributSetString));
        requestParams.add(new NameValuePair("variableName", "dsResponse"));
        requestParams.add(new NameValuePair("sessionId", esmoSessionId));
        UpdateDataRequest updateReq = new UpdateDataRequest(esmoSessionId, "dsResponse", attributSetString);
        LOG.info("will send " + attributSetString);
        resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json", 1), SessionMngrResponse.class);

        if (!resp.getCode().toString().equals("OK")) {
            LOG.error("ERROR: " + resp.getError());
            redirectAttrs.addFlashAttribute("errorMsg", "Error communicating with the ESMO Network");
            return "redirect:/grap/authfail";
        }

        // store the ap metadata
        requestParams.clear();
        if (metadataServ != null && metadataServ.getMetadata() != null) {
            updateReq = new UpdateDataRequest(esmoSessionId, "dsMetadata", mapper.writeValueAsString(metadataServ.getMetadata()));
            resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json", 1), SessionMngrResponse.class);
            if (!resp.getCode().toString().equals("OK")) {
                LOG.error("ERROR: " + resp.getError());
                redirectAttrs.addFlashAttribute("errorMsg", "Error communicating with the ESMO Network");
                return "redirect:/grap/authfail";
            }
        }
        //generate jwt and redirect to acm
        requestParams.clear();
        requestParams.add(new NameValuePair("sessionId", esmoSessionId));
        requestParams.add(new NameValuePair("sender", paramServ.getParam("REDIRECT_JWT_SENDER"))); //[TODO] add correct sender "IdPms001"
        requestParams.add(new NameValuePair("receiver", paramServ.getParam("REDIRECT_JWT_RECEIVER"))); //"ACMms001"
        resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/generateToken", requestParams, 1), SessionMngrResponse.class);
        if (!resp.getCode().toString().equals("NEW")) {
            LOG.error("ERROR: " + resp.getError());
            redirectAttrs.addFlashAttribute("errorMsg", "Could not generate redirection token");
            return "redirect:/authfail";
        } else {
            String msToken = resp.getAdditionalData();
            //IdP calls, post  /acm/response
            String acmUrl = paramServ.getParam("ACM_URL");
            model.addAttribute("msToken", msToken);
            model.addAttribute("acmUrl", acmUrl + "/acm/response");
            return "acmRedirect";
        }
    }

    @RequestMapping(value = "/ap/proceedAfterError", method = {RequestMethod.POST, RequestMethod.GET})
    public String recoverAfterFailure(@RequestParam(value = "sessionId", required = true) String apMsSessionId, Model model) throws JsonProcessingException, IOException, NoSuchAlgorithmException {
        String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
        ObjectMapper mapper = new ObjectMapper();
        List<NameValuePair> requestParams = new ArrayList();
        String responseId = UUID.randomUUID().toString();
        Cache memCache = this.cacheManager.getCache(MemCacheConfig.AP_SESSION);

        String apmsSession = null;
        try {
            apmsSession = (String) memCache.get(apMsSessionId).get();
        } catch (Exception e) {
            LOG.error("ERROR: " + e.getMessage());
            return "redirect:/grap/authfail?reason=esmo";
        }
        SessionMngrResponse sessionObject = mapper.readValue(apmsSession, SessionMngrResponse.class);
        String esmoSessionId = sessionObject.getSessionData().getSessionId();
        AttributeSet receivedAttributes = EsmoResponseFactory.buildErrorResponse("APms001", responseId, esmoSessionId);
        String attributSetString = mapper.writeValueAsString(receivedAttributes);
        UpdateDataRequest updateReq = new UpdateDataRequest(esmoSessionId, "dsResponse", attributSetString);
        SessionMngrResponse resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json", 1),
                SessionMngrResponse.class);
        if (!resp.getCode().toString().equals("OK")) {
            LOG.error("ERROR: " + resp.getError());
            return "redirect:/grap/authfail?reason=esmo";
        }
        //IdP Connector generates a new security token to send to the ACM, by calling get “/sm/generateToken”
        requestParams.clear();
        requestParams.add(new NameValuePair("sessionId", esmoSessionId));
        requestParams.add(new NameValuePair("sender", paramServ.getParam("REDIRECT_JWT_SENDER"))); //[TODO] add correct sender "IdPms001"
        requestParams.add(new NameValuePair("receiver", paramServ.getParam("REDIRECT_JWT_RECEIVER"))); //"ACMms001"
        resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/generateToken", requestParams, 1), SessionMngrResponse.class);
        String msToken = resp.getAdditionalData();
        model.addAttribute("msToken", msToken);
        model.addAttribute("acmUrl", paramServ.getParam("ACM_URL"));
        return "acmRedirect";
    }

    @RequestMapping("/authfail")
    public String authorizationFail(@RequestParam(value = "reason", required = false) String reason,
            @CookieValue(value = "localeInfo", required = false) String langCookie,
            Model model) {

        if (reason != null) {
            model.addAttribute("title", "Registration/Login Cancelled");
            switch (reason) {
                case "esmo":
                    model.addAttribute("title", "Non-sucessful authentication");
                    break;
                default:
                    model.addAttribute("errorType", "CANCEL");
                    break;
            }
        }
        return "authfail";
    }

}
