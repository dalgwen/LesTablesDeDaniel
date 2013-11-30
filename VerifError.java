public class VerifError extends Exception {
	
	private String message;
	private static final long serialVersionUID = -5210629427828121465L;

	public VerifError(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}
}