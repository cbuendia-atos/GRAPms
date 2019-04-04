package esmo.test.ap.apms.model.pojo;

public class InspectionResult {
	
	private String academicId;
	private String amka;
	private String residenceLocation;
	private String universityLocation;
	private String studentshipType;
	private String greekFirstName;
	private String greekLastName;
	private String latinFirstName;
	private String latinLastName;
	private String departmentName;
	private Integer entryYear;
	private Integer currentSemester;
	private String postGraduateProgram;
	private String pasoValidity;
	private String pasoExpirationDate;
	private String submissionDate;
	private String applicationStatus;
	private String cancellationDate;
	private String cancellationReason;
	private String erasmus;
	private String studentNumber;
	private String photoUrl;
	private Boolean webServiceSuccess;
	private String validationError;
	
	public InspectionResult() {
		
	}

	public String getAcademicId() {
		return academicId;
	}

	public void setAcademicId(String academicId) {
		this.academicId = academicId;
	}

	public String getAmka() {
		return amka;
	}

	public void setAmka(String amka) {
		this.amka = amka;
	}

	public String getResidenceLocation() {
		return residenceLocation;
	}

	public void setResidenceLocation(String residenceLocation) {
		this.residenceLocation = residenceLocation;
	}

	public String getUniversityLocation() {
		return universityLocation;
	}

	public void setUniversityLocation(String universityLocation) {
		this.universityLocation = universityLocation;
	}

	public String getStudentshipType() {
		return studentshipType;
	}

	public void setStudentshipType(String studentshipType) {
		this.studentshipType = studentshipType;
	}

	public String getGreekFirstName() {
		return greekFirstName;
	}

	public void setGreekFirstName(String greekFirstName) {
		this.greekFirstName = greekFirstName;
	}

	public String getGreekLastName() {
		return greekLastName;
	}

	public void setGreekLastName(String greekLastName) {
		this.greekLastName = greekLastName;
	}

	public String getLatinFirstName() {
		return latinFirstName;
	}

	public void setLatinFirstName(String latinFirstName) {
		this.latinFirstName = latinFirstName;
	}

	public String getLatinLastName() {
		return latinLastName;
	}

	public void setLatinLastName(String latinLastName) {
		this.latinLastName = latinLastName;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public Integer getEntryYear() {
		return entryYear;
	}

	public void setEntryYear(Integer entryYear) {
		this.entryYear = entryYear;
	}

	public Integer getCurrentSemester() {
		return currentSemester;
	}

	public void setCurrentSemester(Integer currentSemester) {
		this.currentSemester = currentSemester;
	}

	public String getPostGraduateProgram() {
		return postGraduateProgram;
	}

	public void setPostGraduateProgram(String postGraduateProgram) {
		this.postGraduateProgram = postGraduateProgram;
	}

	public String getPasoValidity() {
		return pasoValidity;
	}

	public void setPasoValidity(String pasoValidity) {
		this.pasoValidity = pasoValidity;
	}

	public String getPasoExpirationDate() {
		return pasoExpirationDate;
	}

	public void setPasoExpirationDate(String pasoExpirationDate) {
		this.pasoExpirationDate = pasoExpirationDate;
	}

	public String getSubmissionDate() {
		return submissionDate;
	}

	public void setSubmissionDate(String submissionDate) {
		this.submissionDate = submissionDate;
	}

	public String getApplicationStatus() {
		return applicationStatus;
	}

	public void setApplicationStatus(String applicationStatus) {
		this.applicationStatus = applicationStatus;
	}

	public String getCancellationDate() {
		return cancellationDate;
	}

	public void setCancellationDate(String cancellationDate) {
		this.cancellationDate = cancellationDate;
	}

	public String getCancellationReason() {
		return cancellationReason;
	}

	public void setCancellationReason(String cancellationReason) {
		this.cancellationReason = cancellationReason;
	}

	public String getErasmus() {
		return erasmus;
	}

	public void setErasmus(String erasmus) {
		this.erasmus = erasmus;
	}

	public String getStudentNumber() {
		return studentNumber;
	}

	public void setStudentNumber(String studentNumber) {
		this.studentNumber = studentNumber;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public Boolean getWebServiceSuccess() {
		return webServiceSuccess;
	}

	public void setWebServiceSuccess(Boolean webServiceSuccess) {
		this.webServiceSuccess = webServiceSuccess;
	}

	public String getValidationError() {
		return validationError;
	}

	public void setValidationError(String validationError) {
		this.validationError = validationError;
	}

	@Override
	public String toString() {
		return "InspectionResult{" +
               "academicId='" + academicId + '\'' +
               ", amka='" + amka + '\'' +
               ", residenceLocation='" + residenceLocation + '\'' +
               ", universityLocation='" + universityLocation + '\'' +
               ", studentshipType='" + studentshipType + '\'' +
               ", greekFirstName='" + greekFirstName + '\'' +
               ", greekLastName='" + greekLastName + '\'' +
               ", latinFirstName='" + latinFirstName + '\'' +
               ", latinLastName='" + latinLastName + '\'' +
               ", departmentName='" + departmentName + '\'' +
               ", entryYear='" + entryYear + '\'' +
               ", currentSemester='" + currentSemester + '\'' +
               ", postGraduateProgram='" + postGraduateProgram + '\'' +
               ", pasoValidity='" + pasoValidity + '\'' +
               ", pasoExpirationDate='" + pasoExpirationDate + '\'' +
               ", submissionDate='" + submissionDate + '\'' +
               ", applicationStatus='" + applicationStatus + '\'' +
               ", cancellationDate='" + cancellationDate + '\'' +
               ", cancellationReason='" + cancellationReason + '\'' +
               ", erasmus='" + erasmus + '\'' +
               ", studentNumber='" + studentNumber + '\'' +
               //", photoUrl='" + photoUrl + '\'' +
               ", webServiceSuccess='" + webServiceSuccess + '\'' +
               ", validationError='" + validationError + '\'' +
               '}';
	}

}
