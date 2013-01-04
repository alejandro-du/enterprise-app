package enterpriseapp.ui.validator;

import com.vaadin.data.validator.AbstractStringValidator;

public class LongValidator extends AbstractStringValidator {
	
	private static final long serialVersionUID = 1L;

	public LongValidator(String errorMessage) {
		super(errorMessage);
	}

	@Override
	protected boolean isValidString(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (Exception e) {
            return false;
        }
	}

}
