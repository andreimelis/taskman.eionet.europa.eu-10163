package taskman.ticket10163;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TsvWriter {

	PrintWriter writer;

	public TsvWriter(String filename) throws IOException {
		File f = new File(filename);
		writer = new PrintWriter(new FileWriter(f));
	}

	public void writeLine(String... values) {
		for (String value : values) {
			if ( value != null ) {
				writer.write(value);
			}
			writer.write("\t");
		}
		writer.println();
	}

	public void close() {
		writer.close();
	}
}
