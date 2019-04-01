package esmo.test.ap.apms.model.pojo;

public class QueryIdResponse {
	
	private String response;
	private String errorReason;
	private InspectionResult inspectionResult;
	
	public QueryIdResponse() {
		
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getErrorReason() {
		return errorReason;
	}

	public void setErrorReason(String errorReason) {
		this.errorReason = errorReason;
	}

	public InspectionResult getInspectionResult() {
		return inspectionResult;
	}

	public void setInspectionResult(InspectionResult inspectionResult) {
		this.inspectionResult = inspectionResult;
	}

	@Override
	public String toString() {
		return "QueryIdResponse{" +
               "response='" + response + '\'' +
               ", errorReason='" + errorReason + '\'' +
               ", inspectionResult=" + inspectionResult +
               '}';
	}

}
