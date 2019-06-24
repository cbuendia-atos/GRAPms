/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esmo.test.ap.apms.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import esmo.test.ap.apms.MemCacheConfig;
import esmo.test.ap.apms.model.enums.TypeEnum;
import esmo.test.ap.apms.model.factories.AttributeTypeFactory;
import esmo.test.ap.apms.model.pojo.AttributeSet;
import esmo.test.ap.apms.model.pojo.AttributeSetStatus;
import esmo.test.ap.apms.model.pojo.AttributeType;
import esmo.test.ap.apms.model.pojo.EntityMetadata;
import esmo.test.ap.apms.model.pojo.SessionMngrResponse;
import esmo.test.ap.apms.model.pojo.UpdateDataRequest;
import esmo.test.ap.apms.service.EsmoMetadataService;
import esmo.test.ap.apms.service.HttpSignatureService;
import esmo.test.ap.apms.service.KeyStoreService;
import esmo.test.ap.apms.service.NetworkService;
import esmo.test.ap.apms.service.ParameterService;
import esmo.test.ap.apms.service.impl.HttpSignatureServiceImpl;
import esmo.test.ap.apms.service.impl.NetworkServiceImpl;
import esmo.test.ap.apms.service.impl.SamlUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml2.core.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author nikos
 */
@Controller
public class SamlControllers {

    private final static Logger LOG = LoggerFactory.getLogger(SamlControllers.class);

    @Autowired
    private CacheManager cacheManager;

    private final NetworkService netServ;
    private final ParameterService paramServ;
    private final EsmoMetadataService metadataServ;
    private final KeyStoreService keyServ;

    public SamlControllers(ParameterService paramServ, KeyStoreService keyServ,
            EsmoMetadataService metadataServ) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedEncodingException, InvalidKeySpecException, IOException {
        this.paramServ = paramServ;
        this.metadataServ = metadataServ;
        this.keyServ = keyServ;
        Key signingKey = this.keyServ.getSigningKey();
        String fingerPrint = this.keyServ.getFingerPrint();
        HttpSignatureService httpSigServ = new HttpSignatureServiceImpl(fingerPrint, signingKey);
        this.netServ = new NetworkServiceImpl(httpSigServ);
    }

    @RequestMapping(value = "/saml/ap/query", method = {RequestMethod.POST, RequestMethod.GET})
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

                //calls SM, “/sm/getSessionData” to get the session object that must contain the variables apRequest, apMetadata
                requestParams.clear();
                requestParams.add(new NameValuePair("sessionId", esmoSessionId));
                resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/getSessionData", requestParams, 1), SessionMngrResponse.class);

                String apRequest = (String) resp.getSessionData().getSessionVariables().get("apRequest");

                if (apRequest == null) {
                    LOG.error("no apRequest found in session " + esmoSessionId);
                    model.addAttribute("error", "No AP request attributes found in the Session! Please restart the process");
                    redirectAttrs.addFlashAttribute("errorMsg", "No AP request attributes found in the Session! Please restart the process");
                    return "redirect:/authfail";
                } else {

                    EntityMetadata apMsMetadata = metadataServ.getMetadata();

                    AttributeSet parsedApRequest = mapper.readValue(apRequest, AttributeSet.class);
                    List<AttributeType> matchingRequestedAttributes = Arrays.stream(parsedApRequest.getAttributes()).filter(attribute -> {
                        return Arrays.asList(apMsMetadata.getClaims()).contains(attribute.getFriendlyName());
                    }).collect(Collectors.toList());

                    if (matchingRequestedAttributes.size() > 0) {
                        memCache.put(apMsSessionId, mapper.writeValueAsString(resp)); //save the whole ESMO session in cache
                        return "redirect:/grap/saml/login?session=" + apMsSessionId;
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
        return "redirect:/authfail";
    }

    @RequestMapping("/saml/ap/samlSuccess")
    public String samlSuccessResponseSession(@RequestParam(value = "session", required = true) String apSession,
            Authentication authentication, Model model, RedirectAttributes redirectAttrs) throws IOException, NoSuchAlgorithmException, KeyStoreException {
        authentication.getDetails();
        SAMLCredential credentials = (SAMLCredential) authentication.getCredentials();
        List<String> attributes = Arrays.asList(
                "schacHomeOrganization", "eduPersonTargetedID", "schGrAcPersonID",
                "uid", "schacGender", "schacYearOfBirth", "schacDateOfBirth",
                "schacCountryOfCitizenship", "schGrAcPersonSSN", "schacPersonalUniqueID",
                "eduPersonOrgDN", "mail", "eduPersonAffiliation", "eduPersonAffiliation",
                "eduPersonScopedAffiliation", "eduPersonPrimaryAffiliation", "givenName", "givenName_en",
                "givenName_el", "sn", "sn_el", "sn_en", "cn_en", "cn_el", "displayName", "schacPersonalPosition",
                "schacPersonalUniqueCode", "schGrAcEnrollment", "schGrAcInscription", "schGrAcPersonalLinkageID",
                "eduPersonEntitlement",
                "schGrAcPersonID", "ou", "dc", "schGrAcEnrollment",
                "eduPersonPrimaryAffiliation", "sn;lang-el",
                "mailLocalAddress", "sn;lang-en",
                "cn;lang-el", "eduPersonOrgDN", "schGrAcPersonID",
                "schacPersonalUniqueCode", "schacYearOfBirth",
                "schacPersonalPosition", "cn;lang-en",
                "schGrAcInscription", "eduPersonAffiliation",
                "schacCountryOfCitizenship", "schacPersonalUniqueID",
                "schacGender", "eduPersonScopedAffiliation",
                "schacHomeOrganization", "departmentNumber",
                "eduPersonEntitlement", "givenName;lang-en",
                "sn", "schGrAcPersonalLinkageID", "givenName;lang-el",
                "displayName", "mobile", "givenName", "cn"
        );

        String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
        List<NameValuePair> requestParams = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        Cache memCache = this.cacheManager.getCache(MemCacheConfig.AP_SESSION);
        String session = memCache.get(apSession, String.class);
        SessionMngrResponse resp = mapper.readValue(session, SessionMngrResponse.class);
        String esmoSessionId = resp.getSessionData().getSessionId();

        List<Attribute> returnedAttributes
                = attributes.stream()
                        .map(attrName -> {
                            return SamlUtils.getByFriendlyName(credentials.getAttributes(), attrName);
                        })
                        .filter(attr -> {
                            return attr.isPresent();
                        })
                        .map(attr -> {
                            return attr.get();
                        }).collect(Collectors.toList());

        AttributeType[] result = AttributeTypeFactory.makeFromSamlAttribute(returnedAttributes);
        AttributeSetStatus atrSetStatus = new AttributeSetStatus();
        Map< String, String> metadataProperties = new HashMap();
        atrSetStatus.setCode(AttributeSetStatus.CodeEnum.OK);
        AttributeSet attrSet = new AttributeSet("id", TypeEnum.Response, "APms001", "ACMms001", result, metadataProperties, esmoSessionId, "low", null, null, atrSetStatus);

        String attributSetString = attributSetString = mapper.writeValueAsString(attrSet);
        requestParams.clear();
        requestParams.add(new NameValuePair("dataObject", attributSetString));
        requestParams.add(new NameValuePair("variableName", "dsResponse"));
        requestParams.add(new NameValuePair("sessionId", esmoSessionId));
        UpdateDataRequest updateReq = new UpdateDataRequest(esmoSessionId, "dsResponse", attributSetString);
        resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json", 1), SessionMngrResponse.class);

        if (!resp.getCode().toString().equals("OK")) {
            LOG.error("ERROR: " + resp.getError());
            redirectAttrs.addFlashAttribute("errorMsg", "Error communicating with the ESMO Network");
            return "redirect:/authfail";
        }

        // store the ap metadata
        requestParams.clear();
        if (metadataServ != null && metadataServ.getMetadata() != null) {
            updateReq = new UpdateDataRequest(esmoSessionId, "dsMetadata", mapper.writeValueAsString(metadataServ.getMetadata()));
            resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json", 1), SessionMngrResponse.class);
            if (!resp.getCode().toString().equals("OK")) {
                LOG.error("ERROR: " + resp.getError());
                redirectAttrs.addFlashAttribute("errorMsg", "Error communicating with the ESMO Network");
                return "redirect:/authfail";
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

}
