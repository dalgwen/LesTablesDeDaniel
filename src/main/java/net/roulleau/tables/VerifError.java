package net.roulleau.tables;


public class VerifError extends Exception {
	
	private String messageKey;
	private Object[] args;
	
	private static final long serialVersionUID = -5210629427828121465L;

	public VerifError(String key, Object... args) {
		this.args = args;
		this.messageKey=key;
	}

	public String getMessage() {
		return this.messageKey;
	}
	
	public String getMessageKey() {
		return this.messageKey;
	}
	
	public Object[] getArgs() {
		return this.args;
	}
}