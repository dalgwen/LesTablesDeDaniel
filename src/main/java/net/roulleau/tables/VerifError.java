package net.roulleau.tables;
public class VerifError extends Exception {
	
	private String message;
	private Long randomSeed;
	private static final long serialVersionUID = -5210629427828121465L;
	
	public VerifError(String message, Long randomSeed) {
		this.message = message;
		this.randomSeed = randomSeed;
	}

	public String getMessage() {
		return this.message + "; random seed = " + randomSeed;
	}
}