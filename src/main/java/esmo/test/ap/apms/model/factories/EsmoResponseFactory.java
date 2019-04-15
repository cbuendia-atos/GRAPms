/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esmo.test.ap.apms.model.factories;

import esmo.test.ap.apms.model.enums.TypeEnum;
import esmo.test.ap.apms.model.pojo.AttributeSet;
import esmo.test.ap.apms.model.pojo.AttributeSetStatus;
import esmo.test.ap.apms.model.pojo.AttributeType;
import esmo.test.ap.apms.model.pojo.MinEduResponse;
import esmo.test.ap.apms.utils.TranslitarateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author nikos
 */
public class EsmoResponseFactory {

    public static AttributeSet buildErrorResponse(String issuer, String recipient, String smSessionId) {
        List<AttributeType> attributes = new ArrayList<>();
        String id = UUID.randomUUID().toString();
        Map< String, String> metadataProperties = new HashMap();
        AttributeSetStatus atrSetStatus = new AttributeSetStatus();
        atrSetStatus.setCode(AttributeSetStatus.CodeEnum.ERROR);
        AttributeType[] attrArray = new AttributeType[attributes.size()];
        return new AttributeSet(id, TypeEnum.Response, issuer, recipient,
                (AttributeType[]) attributes.toArray(attrArray), metadataProperties, smSessionId, "low", null, null, atrSetStatus);

    }

    public static AttributeSet buildFromMinEduResponse(MinEduResponse resp, String issuer, String recipient, String smSessionId) {

        if (resp.isSuccess()) {
            String id = UUID.randomUUID().toString();
            List<AttributeType> attributes = new ArrayList<>();
            String academicIdenitfier = resp.getResult().getInspectionResult().getAcademicId();
            String eduPersonAffiliation = TranslitarateUtils.convertGreektoLatin(resp.getResult().getInspectionResult().getDepartmentName());
            String eduPersonAffiliationGR = resp.getResult().getInspectionResult().getDepartmentName();
            String primaryAffiliation = TranslitarateUtils.convertGreektoLatin(resp.getResult().getInspectionResult().getDepartmentName());
            String primaryAffiliationGR = resp.getResult().getInspectionResult().getDepartmentName();
            String email = "";
            String schacExpiryDate = "";
            String mobile = "";
            String eduPersonPrincipalName = "";
            String eduPersonPrincipalNamePrior = "";
            String displayName = resp.getResult().getInspectionResult().getLatinFirstName() + " " + resp.getResult().getInspectionResult().getLatinLastName();
            String sn = resp.getResult().getInspectionResult().getLatinLastName();
            String givenName = resp.getResult().getInspectionResult().getLatinLastName();

            attributes.add(new AttributeType("academicIdenitfier", "academicIdenitfier", "UTF-8", "en", false, (String[]) Arrays.asList(academicIdenitfier).toArray()));
            attributes.add(new AttributeType("https://software.internet2.edu/eduperson/internet2-mace-dir-eduperson-201602.html#eduPersonAffiliation", "eduPersonAffiliation", "UTF-8", "en", false, (String[]) Arrays.asList(eduPersonAffiliation, eduPersonAffiliationGR).toArray()));
            attributes.add(new AttributeType("https://software.internet2.edu/eduperson/internet2-mace-dir-eduperson-201602.html#eduPersonPrimaryAffiliation", "primaryAffiliation", "UTF-8", "en", false, (String[]) Arrays.asList(primaryAffiliation, primaryAffiliationGR).toArray()));
            // TODO complete attribute set???
            attributes.add(new AttributeType("https://wiki.refeds.org/download/attachments/1606048/schac-20150413-1.5.0.schema.txt?version=1&modificationDate=1429044813839&api=v2#schacHomeOrganization", "schacHomeOrganization", "UTF-8", "en", false, (String[]) Arrays.asList(primaryAffiliation).toArray()));
            attributes.add(new AttributeType("https://software.internet2.edu/eduperson/internet2-mace-dir-eduperson-201602.html#mail", "mail", "UTF-8", "en", false, (String[]) Arrays.asList(email).toArray()));
            attributes.add(new AttributeType("https://wiki.refeds.org/download/attachments/1606048/schac-20150413-1.5.0.schema.txt?version=1&modificationDate=1429044813839&api=v2#schacExpiryDate", "schacExpiryDate", "UTF-8", "en", false, (String[]) Arrays.asList(schacExpiryDate).toArray()));
            attributes.add(new AttributeType("https://software.internet2.edu/eduperson/internet2-mace-dir-eduperson-201602.html#mobile", "mobile", "UTF-8", "en", false, (String[]) Arrays.asList(mobile).toArray()));
            attributes.add(new AttributeType("https://software.internet2.edu/eduperson/internet2-mace-dir-eduperson-201602.html#eduPersonPrincipalName", "eduPersonPrincipalName", "UTF-8", "en", false, (String[]) Arrays.asList(eduPersonPrincipalName).toArray()));
            attributes.add(new AttributeType("https://software.internet2.edu/eduperson/internet2-mace-dir-eduperson-201602.html#eduPersonPrincipalNamePrior", "eduPersonPrincipalNamePrior", "UTF-8", "en", false, (String[]) Arrays.asList(eduPersonPrincipalNamePrior).toArray()));
            attributes.add(new AttributeType("https://software.internet2.edu/eduperson/internet2-mace-dir-eduperson-201602.html#mail", "displayName", "UTF-8", "en", false, (String[]) Arrays.asList(displayName).toArray()));
            attributes.add(new AttributeType("https://software.internet2.edu/eduperson/internet2-mace-dir-eduperson-201602.html#sn", "sn", "UTF-8", "en", false, (String[]) Arrays.asList(sn).toArray()));
            attributes.add(new AttributeType("https://software.internet2.edu/eduperson/internet2-mace-dir-eduperson-201602.html#givenName ", "givenName", "UTF-8", "en", false, (String[]) Arrays.asList(givenName).toArray()));

            Map< String, String> metadataProperties = new HashMap();
            AttributeSetStatus atrSetStatus = new AttributeSetStatus();
            atrSetStatus.setCode(AttributeSetStatus.CodeEnum.OK);

            AttributeType[] attrArray = new AttributeType[attributes.size()];
            return new AttributeSet(id, TypeEnum.Response, issuer, recipient,
                    (AttributeType[]) attributes.toArray(attrArray), metadataProperties, smSessionId, "low", null, null, atrSetStatus);

        }
        return null;
    }

}
