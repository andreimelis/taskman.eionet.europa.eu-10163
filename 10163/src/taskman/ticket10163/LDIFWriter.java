package taskman.ticket10163;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class LDIFWriter {

	PrintWriter writer;
	static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();

	public LDIFWriter(String filename) throws IOException {
		File f = new File(filename);
		writer = new PrintWriter(new FileWriter(f));
	}

	public void deleteAttribute(String dn, String attr) {
		writer.print("dn: ");
		writer.println(dn);
		writer.println("changetype: modify");
		writer.println("delete:" + attr);
		writer.println();
	}

	public void replaceAttribute(String dn, String attr, String value) {
		writer.print("dn: ");
		writer.println(dn);
		writer.println("changetype: modify");
		writer.println("replace:" + attr);
		writer.println(attr + ": " + value);
		writer.println();
	}

	public static boolean isEncodingRequired(String value) {
		return !asciiEncoder.canEncode(value);
	}

	public void close() {
		writer.close();
	}
}
