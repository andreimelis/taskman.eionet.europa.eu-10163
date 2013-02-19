package taskman.ticket10163;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

	static Logger logger = LogManager.getLogger(Main.class.getName());

	public static final String ATTR_EMAIL = "mail";
	public static final String ATTR_PHONE = "telephoneNumber";
	public static final String ATTR_FAX = "facsimileTelephoneNumber";

	public static File getInputFile(String[] args) throws ParseException {
		// parse input file parameter
		Options options = new Options();

		Option inputFileOption = OptionBuilder.withArgName("file").hasArg().withDescription("use sqlite3 input file")
				.create("i");
		options.addOption(inputFileOption);

		CommandLineParser parser = new GnuParser();
		CommandLine line = parser.parse(options, args);
		if (!line.hasOption("i")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("main", options);
			return null;
		}
		String input = line.getOptionValue("i");
		File inputFile = new File(input);
		if (!inputFile.exists()) {
			System.out.printf("Input file does not exist %s", inputFile.getAbsolutePath());
			return null;
		}
		return inputFile;
	}

	/**
	 * Return a map with all found attributes for the specified dn
	 */
	public static Map<String, String> loadAttributes(String dn, Connection conn) throws SQLException {
		Map<String, String> attrMap = new HashMap<String, String>();
		PreparedStatement ps = conn.prepareStatement("select attr, value from ldapmapping where dn = ?");
		ps.setString(1, dn);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			String attr = rs.getString(1);
			String value = rs.getString(2);
			attrMap.put(attr, value);
		}
		rs.close();
		ps.close();
		return attrMap;
	}

	public static void replaceOrDelete(Map<String, String> attrMap, String attrName, String dn) {
		String email = attrMap.get(ATTR_EMAIL);
		String number = attrMap.get(attrName);
		if (number != null) {
			String validTelephoneNumber = PhoneUtil.getFormattedNumber(number, email);
			if (validTelephoneNumber != null) {
				// write to LDIF file
				writerValid.replaceAttribute(dn, attrName, validTelephoneNumber);
				// and also write to TSV, for logging
				writerValidTsv.writeLine(dn, attrName, number, validTelephoneNumber, email);
			} else {
				if (PhoneUtil.isDefinitelyInvalid(number)) {
					writerDeleted.deleteAttribute(dn, attrName);
				} else {
					writerInvalid.writeLine(dn, attrName, number, email);
				}
			}
		}
	}

	static LDIFWriter writerValid;
	static LDIFWriter writerDeleted;
	static TsvWriter writerInvalid;
	static TsvWriter writerValidTsv;

	public static void main(String[] args) throws Exception {

		File inputFile = getInputFile(args);
		if (inputFile == null) {
			System.exit(1);
		}

		writerValid = new LDIFWriter("replace.ldif");
		writerDeleted = new LDIFWriter("delete.ldif");
		writerInvalid = new TsvWriter("invalid.tsv");
		writerValidTsv = new TsvWriter("valid.tsv");

		// load jdbc
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:" + inputFile.getAbsolutePath());

		// iterate for each DN
		Statement stat = conn.createStatement();
		ResultSet rs = stat.executeQuery("select dn, attr, value from ldapmapping order by dn, attr");
		// iterate it in a weird way in order to avoid subselects
		String currentDN = null;
		Map<String, String> attrMap = new HashMap<String, String>();
		while (rs.next()) {
			String dn = rs.getString(1);
			if ( currentDN == null ) {
				currentDN = dn;
			}
			String attr = rs.getString(2);
			String value = rs.getString(3);
			if ( currentDN.equals(dn)) {
				attrMap.put(attr, value);
				continue;
				// and continue;
			} else {
				// since the result set is ordered by dn, this means we have a new dn
				replaceOrDelete(attrMap, ATTR_PHONE, currentDN);
				replaceOrDelete(attrMap, ATTR_FAX, currentDN);
				currentDN = dn;
				attrMap.clear();
				attrMap.put(attr, value);
			}
		}
		// write the last entry
		if ( currentDN != null ) {
			replaceOrDelete(attrMap, ATTR_PHONE, currentDN);
			replaceOrDelete(attrMap, ATTR_FAX, currentDN);
		}
		rs.close();
		conn.close();
		writerValid.close();
		writerDeleted.close();
		writerInvalid.close();
		writerValidTsv.close();
	}
}
