package taskman.ticket10163;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import junit.framework.TestCase;

import org.apache.commons.codec.binary.Base64;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.ValidationResult;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class Test extends TestCase {

	public Test(String name) {
		super(name);
	}

	public void test1() {
		String phoneNumber = "(++32)(0)53-321 123 ";
		String result = PhoneUtil.removeSymbols(phoneNumber);
		assertEquals("++3253321123", result);
		assertEquals(PhoneUtil.hasCountryCode(result, "32"), true);
		assertEquals(PhoneUtil.hasCountryCode(result, "33"), false);
		assertEquals(PhoneUtil.removeCountryCodeSymbols(result), "3253321123");

		phoneNumber = "xx39 0332 789958";
		result = PhoneUtil.removeSymbols(phoneNumber);
		assertEquals(PhoneUtil.removeCountryCodeSymbols(result), "390332789958");
	}

	public void test2() {
		String email = "Stephan.Braun@ipp.mpg.de";
		assertEquals("DE", CountryUtil.getCountryCode(email));

	}

	public void test3() throws NumberParseException {
		String phoneNumber = "+39 06 5007 2838"; // IT number
		String email = "Gianna.Casazza@isprambiente.it";
		String defaultRegion = CountryUtil.getCountryCode(email);
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber numberObj = phoneUtil.parse(phoneNumber, defaultRegion);
		assertEquals(650072838L, numberObj.getNationalNumber());
		assertEquals(39, numberObj.getCountryCode());
	}

	public void test4() throws NumberParseException {
		String phoneNumber = "+49 202 2492 139"; // DE number
		String email = "jeremy.stephens@environment-agency.gov.uk";
		String defaultRegion = CountryUtil.getCountryCode(email);
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber numberObj = phoneUtil.parse(phoneNumber, defaultRegion);
		assertEquals(2022492139L, numberObj.getNationalNumber());
		assertEquals(49, numberObj.getCountryCode());
	}

	public void test5() throws Exception {
		String phoneNumber = "0574 602532";
		String email = "nativi@pin.unifi.it";
		String defaultRegion = CountryUtil.getCountryCode(email);
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber numberObj = phoneUtil.parse(phoneNumber, defaultRegion);
		assertEquals("+39 0574 602532", phoneUtil.format(numberObj, PhoneNumberFormat.INTERNATIONAL));
	}

	public void test6() throws Exception {
		String phoneNumber = "01242 704872";
		String email = "bpower@blueyonder.co.uk";
		assertEquals("+44 1242 704872", PhoneUtil.getFormattedNumber(phoneNumber, email));
	}

	public void test7() throws Exception {
		String phoneNumber = "+44 (0)20 7944 6142 (";
		String email = "Daryl.Lloyd@dft.gsi.gov.uk";
		assertEquals("+44 20 7944 6142", PhoneUtil.getFormattedNumber(phoneNumber, email));
	}

	public void test8() throws Exception {
		String phoneNumber = "+33 4 72 59 13 20";
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber numberObj = phoneUtil.parse(phoneNumber, "ZZ");
		assertEquals("+33 4 72 59 13 20", phoneUtil.format(numberObj, PhoneNumberFormat.INTERNATIONAL));
	}

	public void test9() throws Exception {
		String phoneNumber = "(++32)(0)53";
		String email = "r.vannevel@vmm.be";
		assertEquals(null, PhoneUtil.getFormattedNumber(phoneNumber, email));
	}

	public void test10() throws Exception {
		String phoneNumber = "00324941266596";
		String email = "rudy.aernoudt@ec.europa.eu";
		assertEquals(null, PhoneUtil.getFormattedNumber(phoneNumber, email));
	}

	public void test11() throws Exception {
		String phoneNumber = "+31 22 075 529";
		// this is not really valid, but we'll keep it
		assertEquals("+31 22075529", PhoneUtil.getFormattedNumber(phoneNumber, null));
	}

	public void test12() throws Exception {
		String phoneNumber = "+41 021 625 2755";
		assertEquals("+41 21 625 27 55", PhoneUtil.getFormattedNumber(phoneNumber, null));
	}

	public void test13() throws Exception {
		// String phoneNumber = "00 32 496139977";
		String phoneNumber = "0040724252195";
		assertEquals("+40 72 425 2195", PhoneUtil.getFormattedNumber(phoneNumber, null));
	}

	public void test14() throws Exception {
		String phoneNumber = "44 121 7115976";
		assertEquals("+44 121 711 5976", PhoneUtil.getFormattedNumber(phoneNumber, null));
	}

	public void test15() throws Exception {
		String phoneNumber = "+421(45)941 5475";
		assertEquals("+421 45/941 54 75", PhoneUtil.getFormattedNumber(phoneNumber, "Katarina.Pukancikova@shmu.sk"));
	}

	public void test16() throws Exception {
		String phoneNumber = "00324941266596";
		// assertEquals("+32 4 126 65 96", PhoneUtil.getFormattedNumber(phoneNumber, null));
	}

	public void test17() throws Exception {
		String phoneNumber = "+3222961191";
		assertEquals("+32 2 296 11 91", PhoneUtil.getFormattedNumber(phoneNumber, null));
	}

	public void test18() throws Exception {
		String phoneNumber = "+49 (30) 18305 2306";
		assertEquals("+49 30 183052306", PhoneUtil.getFormattedNumber(phoneNumber, null));
	}

	public void test19() throws Exception {
		String phoneNumber = "40724252195";
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber numberObj = phoneUtil.parse(phoneNumber, "IT");
		System.out.println(phoneUtil.isValidNumber(numberObj));
		System.out.println(phoneUtil.isPossibleNumberWithReason(numberObj));
		System.out.println(numberObj);
		System.out.println(phoneUtil.format(numberObj, PhoneNumberFormat.INTERNATIONAL));
		assertEquals("+40 72 425 2195", PhoneUtil.getFormattedNumber(phoneNumber, null));
	}

	public void test20() throws Exception {
		// too short
		String phoneNumber = "+371 7811504";
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber numberObj = phoneUtil.parse(phoneNumber, "LV");
		assertEquals(ValidationResult.TOO_SHORT, phoneUtil.isPossibleNumberWithReason(numberObj));
	}

	public void test21() throws Exception {
		// too long -> we get null
		// TODO: shoul we keep at least one ?
		String phoneNumber = "+353-1-2043209 353-1-282 6456";
		assertEquals(null, PhoneUtil.getFormattedNumber(phoneNumber, null));
	}

	public void testBase64() throws Exception {
		byte[] decoded = Base64.decodeBase64("QW5kcmVpIE1lbGnFnw==");
		String str = new String(decoded);
		assertEquals("Andrei Meliş", str);
		CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
		assertEquals(false, asciiEncoder.canEncode(str));
		assertEquals(true, asciiEncoder.canEncode("test"));
	}
}
