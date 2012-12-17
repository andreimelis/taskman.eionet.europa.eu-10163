package taskman.ticket10163;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.ValidationResult;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class PhoneUtil {

	static Logger logger = LogManager.getLogger(PhoneUtil.class.getName());

	/**
	 * returns true if the telephoneNumber/facsimileTelephoneNumber is definitely invalid and should be deleted
	 */
	public static boolean isDefinitelyInvalid(String phoneNumber) {
		if (removeSymbols(phoneNumber).length() < 6) {
			return true;
		}
		return false;
	}

	public static String removeSymbols(String phoneNumber) {
		// remove optional 0 between braces
		String result = phoneNumber.replaceAll("\\(0\\)", "");
		// remove letters, whitespace, dashes, braces
		result = result.replaceAll("[\\p{Alpha}\\s\\-\\(\\)]", "");
		return result;
	}

	public static boolean hasCountryCode(String phoneNumber, String countryCode) {
		// remove leading zeroes and leading plus
		String result = PhoneUtil.removeCountryCodeSymbols(phoneNumber);
		return result.startsWith(countryCode);
	}

	public static String removeCountryCodeSymbols(String phoneNumber) {
		// remove leading zeroes and leading plus
		return phoneNumber.replaceAll("^(0)*\\+*", "");
	}

	public static String getFormattedNumber(String telephoneNumber, String email) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		String defaultRegion = "ZZ";
		if (email != null) {
			defaultRegion = CountryUtil.getCountryCode(email);
		}
		try {
			PhoneNumber numberObj = phoneUtil.parse(telephoneNumber, defaultRegion);
			if (phoneUtil.isValidNumber(numberObj)) {
				return phoneUtil.format(numberObj, PhoneNumberFormat.INTERNATIONAL);
			}
		} catch (NumberParseException e) {
		}

		// if not valid, try some workarounds
		String result = null;
		if (!telephoneNumber.startsWith("+") && !telephoneNumber.startsWith("00")) {
			result = getFormattedNumber("+" + telephoneNumber, email);
		} else if (telephoneNumber.startsWith("00")) {
			result = getFormattedNumber("+" + telephoneNumber.substring(2), email);
		}

		if (result == null) {
			// try less strict validation
			try {
				PhoneNumber numberObj = phoneUtil.parse(telephoneNumber, defaultRegion);
				if (ValidationResult.IS_POSSIBLE == phoneUtil.isPossibleNumberWithReason(numberObj)) {
					return phoneUtil.format(numberObj, PhoneNumberFormat.INTERNATIONAL);
				}
			} catch (NumberParseException e) {
			}
		}

		return result;
	}
}
